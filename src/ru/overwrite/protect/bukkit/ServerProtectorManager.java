package ru.overwrite.protect.bukkit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import ru.overwrite.protect.bukkit.api.*;
import ru.overwrite.protect.bukkit.commands.*;
import ru.overwrite.protect.bukkit.listeners.*;
import ru.overwrite.protect.bukkit.utils.Config;
import ru.overwrite.protect.bukkit.utils.Utils;

public class ServerProtectorManager extends JavaPlugin {
	
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("[dd-MM-yyy] HH:mm:ss -");
	
	public FileConfiguration message;
	public FileConfiguration data;
    
    public Set<String> ips = new HashSet<>();
    public Set<String> login = new HashSet<>();
    public Set<String> saved = new HashSet<>();
    public Map<Player, Integer> time = new HashMap<>();
    
    public boolean fullpath = false;
    
    private final Config pluginConfig = new Config(this);
    private final ServerProtectorAPI api = new ServerProtectorAPI(this);
    private final PasswordHandler passwordHandler = new PasswordHandler(this);
    
    public final Server server = getServer();
    
    public final Logger logger = getLogger();
    
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
    
    public void loadConfigs(FileConfiguration config) {
        fullpath = config.getBoolean("file-settings.use-full-path");
        data = fullpath ? pluginConfig.getFileFullPath(config.getString("file-settings.data-file")) : pluginConfig.getFile(config.getString("file-settings.data-file"));
        pluginConfig.save(data, config.getString("file-settings.data-file"));
        message = pluginConfig.getFile("message.yml");
        pluginConfig.save(message, "message.yml");
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
    	reloadConfig();
        message = pluginConfig.getFile("message.yml");
        data = fullpath ? pluginConfig.getFileFullPath(config.getString("file-settings.data-file")) : pluginConfig.getFile(config.getString("file-settings.data-file"));
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
                PluginCommand command;
                CommandMap map = null;
                Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
                c.setAccessible(true);
                command = c.newInstance(config.getString("main-settings.pas-command"), this);
                if (pluginManager instanceof SimplePluginManager) {
                    Field f = SimplePluginManager.class.getDeclaredField("commandMap");
                    f.setAccessible(true);
                    map = (CommandMap)f.get(pluginManager);
                }
                if (map != null) {
                    map.register(getDescription().getName(), command);
                }
                command.setExecutor(new PasCommand(this));
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
    	Runner runner = new Runner(this);
    	runner.runTaskTimerAsynchronously(this, 5L, 40L);
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
    
    public void checkForUpdates(Logger logger) {
        if (!getConfig().getBoolean("main-settings.update-checker")) {
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
    
    public void checkFail(ServerProtectorManager plugin, Player p, List<String> command) {
    	server.getScheduler().runTask(plugin, () -> {
            for (String c : command) {
            	server.dispatchCommand(server.getConsoleSender(), c.replace("%player%", p.getName()));
            }
        });
    }
    
    public void giveEffect(ServerProtectorManager plugin, Player p) {
    	server.getScheduler().runTask(plugin, () -> {
            for (String s : pluginConfig.effect_settings_effects) {
                PotionEffectType types = PotionEffectType.getByName(s.split(":")[0].toUpperCase());
                int level = Integer.parseInt(s.split(":")[1]) - 1;
                p.addPotionEffect(new PotionEffect(types, 99999, level));
            }
        });
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

    public boolean isAdmin(String nick) {
        return data.contains("data." + nick);
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
	        File dataFolder = getDataFolder();
	        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
	            throw new RuntimeException("Unable to create data folder");
	        }
	        String logFilePath = fullpath ? getConfig().getString("file-settings.log-file-path") : dataFolder.getPath();
	        Path saveTo = Paths.get(logFilePath, "log.yml");
	        try (BufferedWriter writer = Files.newBufferedWriter(saveTo, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
	            writer.write(message);
	            writer.newLine();
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
}