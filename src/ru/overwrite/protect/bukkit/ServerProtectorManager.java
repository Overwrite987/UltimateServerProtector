package ru.overwrite.protect.bukkit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import ru.overwrite.protect.bukkit.api.*;
import ru.overwrite.protect.bukkit.commands.*;
import ru.overwrite.protect.bukkit.checker.*;
import ru.overwrite.protect.bukkit.listeners.*;
import ru.overwrite.protect.bukkit.utils.Config;
import ru.overwrite.protect.bukkit.utils.Utils;

public class ServerProtectorManager extends JavaPlugin {
	
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("[dd-MM-yyy] HH:mm:ss -");
    public static String serialiser;
    public boolean proxy = false;
	
	public FileConfiguration message;
	public FileConfiguration data;
	
	public String path;
    
    public Set<String> ips = new HashSet<>();
    public Set<String> login = new HashSet<>();
    public Set<String> saved = new HashSet<>();
    
    public Map<Player, Integer> time = new ConcurrentHashMap<>();
    
    private final Config pluginConfig = new Config(this);
    private final ServerProtectorAPI api = new ServerProtectorAPI(this);
    private final PasswordHandler passwordHandler = new PasswordHandler(this);
    private PluginMessage pluginMessage;
    
    public final Server server = getServer();
    
    public final Logger logger = getLogger();
    
    private BufferedWriter bufferedWriter;
    
    public void checkPaper(Logger logger) {
    	if (server.getName().equals("CraftBukkit")) {
    		logger.info("§6============= §c! WARNING ! §c=============");
    		logger.info("§eYou are using an unstable core for your MC server! It's recomended to use §aPaper");
    		logger.info("§eDownload Paper: §ahttps://papermc.io/downloads/all");
    		logger.info("§6============= §c! WARNING ! §c=============");
            setEnabled(false);
            return;
        }    	
    }
    
    public void setupProxy(FileConfiguration config) {
    	if (config.getBoolean("main-settings.proxy")) {
    		server.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    		pluginMessage = new PluginMessage(this);
    		server.getMessenger().registerIncomingPluginChannel(this, "BungeeCord", pluginMessage);
    		proxy = true;
    	}
    }
    
    public void loadConfigs(FileConfiguration config) {
    	serialiser = config.getString("main-settings.serialiser");
        Boolean fullpath = config.getBoolean("file-settings.use-full-path");
        path = fullpath ? config.getString("file-settings.data-file-path") : getDataFolder().getAbsolutePath();
        data = pluginConfig.getFile(path, config.getString("file-settings.data-file"));
        pluginConfig.save(path, data, config.getString("file-settings.data-file"));
        message = pluginConfig.getFile(getDataFolder().getAbsolutePath(), "message.yml");
        pluginConfig.save(getDataFolder().getAbsolutePath(), message, "message.yml");
        pluginConfig.loadPerms(config);
        pluginConfig.loadLists(config);
        pluginConfig.loadMainSettings(config);
        pluginConfig.loadSecureSettings(config);
        pluginConfig.loadAdditionalChecks(config);
        pluginConfig.loadAttempts(config);
        pluginConfig.loadTime(config);
        pluginConfig.loadSessionSettings(config);
        pluginConfig.loadMessageSettings(config);
        pluginConfig.loadSoundSettings(config);
        pluginConfig.loadEffects(config);
        pluginConfig.loadLoggingSettings(config);
        pluginConfig.loadMsgMessages(message);
        if (config.getBoolean("message-settings.send-titles")) {
        	pluginConfig.loadTitleMessages(message);
        }
        if (config.getBoolean("bossbar-settings.enable-bossbar")) {
        	pluginConfig.loadBossbar(config);
        }
        if (config.getBoolean("message-settings.enable-broadcasts")) {
        	pluginConfig.loadBroadcastMessages(message);
        }
        pluginConfig.loadUspMessages(message);
    }
    
    public void reloadConfigs(FileConfiguration config) {
    	serialiser = config.getString("main-settings.serialiser");
    	reloadConfig();
        message = pluginConfig.getFile(getDataFolder().getAbsolutePath(), "message.yml");
        Boolean fullpath = config.getBoolean("file-settings.use-full-path");
        String path = fullpath ? config.getString("file-settings.data-file-path") : getDataFolder().getAbsolutePath();
        data = pluginConfig.getFile(path, config.getString("file-settings.data-file"));
        pluginConfig.loadPerms(config);
        pluginConfig.loadLists(config);
        pluginConfig.loadMainSettings(config);
        pluginConfig.loadSecureSettings(config);
        pluginConfig.loadAdditionalChecks(config);
        pluginConfig.loadAttempts(config);
        pluginConfig.loadTime(config);
        pluginConfig.loadSessionSettings(config);
        pluginConfig.loadMessageSettings(config);
        pluginConfig.loadSoundSettings(config);
        pluginConfig.loadEffects(config);
        pluginConfig.loadLoggingSettings(config);
        pluginConfig.loadMsgMessages(message);
        if (config.getBoolean("message-settings.send-titles")) {
        	pluginConfig.loadTitleMessages(message);
        }
        if (config.getBoolean("bossbar-settings.enable-bossbar")) {
        	pluginConfig.loadBossbar(config);
        }
        if (config.getBoolean("message-settings.enable-broadcasts")) {
        	pluginConfig.loadBroadcastMessages(message);
        }
        pluginConfig.loadUspMessages(message);
    }
    
    public void registerListeners(PluginManager pluginManager) {
    	pluginManager.registerEvents(new ChatListener(this), this);
        pluginManager.registerEvents(new ConnectionListener(this), this);
        pluginManager.registerEvents(new InteractionsListener(this), this);
        pluginManager.registerEvents(new AdditionalListener(this), this);
    }
    
    public void registerCommands(PluginManager pluginManager, FileConfiguration config) {
        if (config.getBoolean("main-settings.use-command")) {
        	try {
        	    CommandMap commandMap = server.getCommandMap();
        	    Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
        	    constructor.setAccessible(true);
        	    PluginCommand command = constructor.newInstance(config.getString("main-settings.pas-command"), this);
        	    command.setExecutor(new PasCommand(this));
        	    if (commandMap != null) {
        	        commandMap.register(getDescription().getName(), command);
        	    }
        	} catch (Exception e) {
        	    logger.info("Unable to register password command!");
        	    e.printStackTrace();
        	    pluginManager.disablePlugin(this);
        	}
        } else {
            logger.info("Using chat for password entering!");
        }
        PluginCommand uspCommand = getCommand("ultimateserverprotector");
        uspCommand.setExecutor(new UspCommand(this));
        uspCommand.setTabCompleter(new UspCommand(this));
    }
    
    public void startRunners(FileConfiguration config) {
    	Runner runner = Utils.FOLIA ? new PaperRunner(this) : new BukkitRunner(this);
    	runner.mainCheck();
    	runner.startMSG(config);
    	if (pluginConfig.punish_settings_enable_time) {
    		runner.startTimer(config);
    	}
    	if (pluginConfig.secure_settings_enable_notadmin_punish) {
    		runner.adminCheck(config);
    	}
    	if (pluginConfig.secure_settings_enable_op_whitelist) {
    		runner.startOpCheck(config);
    	}
    	if (pluginConfig.secure_settings_enable_permission_blacklist) {
    		runner.startPermsCheck(config);
    	}
    }

	public void setupLogger(FileConfiguration config) {
		try {
			File dataFolder = getDataFolder();
			if (!dataFolder.exists() && !dataFolder.mkdirs()) {
				throw new RuntimeException("Unable to create data folder");
			}
			Boolean fullpath = config.getBoolean("file-settings.use-full-path");
			String logFilePath = fullpath ? config.getString("file-settings.log-file-path") : dataFolder.getPath();
			File logFile = new File(logFilePath, config.getString("file-settings.log-file"));
			FileWriter fileWriter = new FileWriter(logFile, true);
			bufferedWriter = new BufferedWriter(fileWriter);
		} catch (IOException e) {
	        e.printStackTrace();
	    }
	}
    
    public void checkForUpdates(FileConfiguration config, Logger logger) {
        if (!config.getBoolean("main-settings.update-checker")) {
            return;
        }
        Utils.checkUpdates(this, version -> {
        	logger.info("§6========================================");
            if (getDescription().getVersion().equals(version)) {
            	logger.info("§aYou are using latest version of the plugin!");
            } else {
            	logger.info("§aYou are using outdated version of the plugin!");
            	logger.info("§aYou can download new version here:");
            	logger.info("§bgithub.com/Overwrite987/UltimateServerProtector/releases/");
            }
            logger.info("§6========================================");
        });
    }
    
    public void checkFail(String pName, List<String> command) {
    	Runnable run = () -> {
    		for (String c : command) {
				server.dispatchCommand(server.getConsoleSender(), c.replace("%player%", pName));
			}
    	};
    	runSyncTask(run);
    }
    
    public void giveEffect(ServerProtectorManager plugin, Player p) {
    	Runnable run = () -> {
    		for (String s : pluginConfig.effect_settings_effects) {
                PotionEffectType types = PotionEffectType.getByName(s.split(":")[0].toUpperCase());
                int level = Integer.parseInt(s.split(":")[1]) - 1;
                p.addPotionEffect(new PotionEffect(types, 99999, level));
            }
    	};
    	if (Utils.FOLIA) {
    		p.getScheduler().run(plugin, (r) -> run.run(), null);
    	} else {
    		server.getScheduler().runTask(plugin, run);
    	}
    }
    
    public void runSyncTask(Runnable run) {
    	if (Utils.FOLIA) {
    		server.getGlobalRegionScheduler().run(this, (sp) -> run.run());
    		return;
    	} else {
    		server.getScheduler().runTask(this, run);
    		return;
    	}
    }
    
    public void runAsyncTask(Runnable run) {
    	if (Utils.FOLIA) {
    		server.getAsyncScheduler().runNow(this, (sp) -> run.run());
    		return;
    	} else {
    		server.getScheduler().runTaskAsynchronously(this, run);
    		return;
    	}
    }
    
    public void runAsyncDelayedTask(Runnable run) {
    	if (Utils.FOLIA) {
    		Bukkit.getAsyncScheduler().runDelayed(this, (s) -> run.run(), pluginConfig.session_settings_session_time * 20L * 50L, TimeUnit.MILLISECONDS);
    		return;
    	} else {
    		Bukkit.getScheduler().runTaskLaterAsynchronously(this, run, pluginConfig.session_settings_session_time * 20L);
    		return;
    	}
    }
    
    public void logEnableDisable(String msg, Date date) {
    	if (getConfig().getBoolean("logging-settings.logging-enable-disable")) {
        	logToFile(msg.replace("%date%", DATE_FORMAT.format(date)));
        }	
    }
    
    public Config getPluginConfig() {
    	return pluginConfig;
    }
    
    public ServerProtectorAPI getPluginAPI() {
    	return api;
    }
    
    public PasswordHandler getPasswordHandler() {
    	return passwordHandler;
    }
    
    public PluginMessage getPluginMessage() {
    	return pluginMessage;
    }
    
    public String getMessage(ConfigurationSection selection, String key) {
        return Utils.colorize(selection.getString(key, "&4&lERROR&r").replace("%prefix%", pluginConfig.main_settings_prefix));
    }

    public String getMessage(ConfigurationSection selection, String key, UnaryOperator<String> preprocess) {
        return Utils.colorize(preprocess.apply(selection.getString(key, "&4&lERROR&r")).replace("%prefix%", pluginConfig.main_settings_prefix));
    }
    
    public boolean isPermissions(Player p) {
        if (p.isOp() || p.hasPermission("serverprotector.protect")) return true;
        for (String s : pluginConfig.perms) {
            if (p.hasPermission(s)) {
            	return true;
            }
        }
        return false;
    }
    
    public boolean isExcluded(Player p) {
    	return pluginConfig.secure_settings_enable_excluded_players && pluginConfig.excluded_players.contains(p.getName());
    }
    
    public boolean isAuthorised(Player p) {
    	return !pluginConfig.session_settings_session && saved.contains(p.getName());
    }

    public boolean isAdmin(String nick) {
        return data.contains("data." + nick);
    }
    
    public void sendAlert(Player p, String msg) {
    	for (Player ps : server.getOnlinePlayers()) {
    		if (ps.hasPermission("serverprotector.admin")) {
    			ps.sendMessage(msg);
    		}
    	}
    	if (proxy) {
    		pluginMessage.sendCrossProxy(p, msg);
    	}
    }
    
    public void loggerInfo(String logMessage) {
    	if (Utils.FOLIA) {
    		getComponentLogger().info(LegacyComponentSerializer.legacySection().deserialize(logMessage));
    	} else {
    		getLogger().info(logMessage);
    	}
    }
	
	public void logAction(String key, Player player, Date date) {
        logToFile(
                message.getString(key, "ERROR")
                        .replace("%player%", player.getName())
                        .replace("%ip%", Utils.getIp(player))
                        .replace("%date%", DATE_FORMAT.format(date))
        );
    }

	public void logToFile(String message) {
		try {
	        bufferedWriter.write(message);
	        bufferedWriter.newLine();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
}