package ru.overwrite.protect.bukkit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.function.UnaryOperator;

import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import ru.overwrite.protect.bukkit.listeners.AdditionalListener;
import ru.overwrite.protect.bukkit.listeners.ChatListener;
import ru.overwrite.protect.bukkit.listeners.ConnectionListener;
import ru.overwrite.protect.bukkit.listeners.InteractionsListener;
import ru.overwrite.protect.bukkit.commands.*;
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
    
    private final PluginManager pluginManager = getServer().getPluginManager();
    
    private final Config pluginConfig = new Config(this);
    
    public final Logger logger = getLogger();
    
    public void checkPaper() {
    	if (getServer().getName().equals("CraftBukkit")) {
    		logger.info("§6============= §c! WARNING ! §c=============");
    		logger.info("§eYou are using an unstable core for your MC server! It's recomended to use §aPaper");
    		logger.info("§eDownload Paper: §ahttps://papermc.io/downloads/all");
    		logger.info("§6============= §c! WARNING ! §c=============");
            setEnabled(false);
            return;
        }    	
    }
    
    public void saveConfigs() {
        saveDefaultConfig();
        FileConfiguration config = getConfig();
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
        pluginConfig.loadMsgMessages();
        if (config.getBoolean("message-settings.send-titles")) {
        	pluginConfig.loadTitleMessages();
        }
        if (config.getBoolean("bossbar-settings.enable-bossbar")) {
        	pluginConfig.loadBossbar(config);
        }
        if (config.getBoolean("message-settings.enable-broadcasts")) {
        	pluginConfig.loadBroadcastMessages();
        }
        pluginConfig.loadUspMessages();
    }
    
    public void reloadConfigs() {
    	reloadConfig();
		FileConfiguration config = getConfig();
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
        pluginConfig.loadMsgMessages();
        if (config.getBoolean("message-settings.send-titles")) {
        	pluginConfig.loadTitleMessages();
        }
        if (config.getBoolean("bossbar-settings.enable-bossbar")) {
        	pluginConfig.loadBossbar(config);
        }
        if (config.getBoolean("message-settings.enable-broadcasts")) {
        	pluginConfig.loadBroadcastMessages();
        }
        pluginConfig.loadUspMessages();
    }
    
    public void registerListeners() {
    	pluginManager.registerEvents(new ChatListener(this), this);
        pluginManager.registerEvents(new ConnectionListener(this), this);
        pluginManager.registerEvents(new InteractionsListener(this), this);
        pluginManager.registerEvents(new AdditionalListener(this), this);
    }
    
    public void registerCommands() {
        if (getConfig().getBoolean("main-settings.use-command")) {
            try {
                PluginCommand command;
                CommandMap map = null;
                Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
                c.setAccessible(true);
                command = c.newInstance(getConfig().getString("main-settings.pas-command"), this);
                if (pluginManager instanceof SimplePluginManager) {
                    Field f = SimplePluginManager.class.getDeclaredField("commandMap");
                    f.setAccessible(true);
                    map = (CommandMap)f.get(pluginManager);
                }
                if (map != null)
                    map.register(getDescription().getName(), command);
                command.setExecutor(new PasCommand(this));
            } catch (Exception e) {
            	logger.info("Unable to register command!");
                e.printStackTrace();
                pluginManager.disablePlugin(this);
            }
        } else {
        	logger.info("Using chat for password entering!");
        }
        getCommand("ultimateserverprotector").setExecutor(new UspCommand(this));
        getCommand("ultimateserverprotector").setTabCompleter(new UspTabCompleter(this));
    }
    
    public void startRunners() {
    	Runner runner = new Runner(this);
    	runner.runTaskTimerAsynchronously(this, 5L, 40L);
    	runner.startMSG();
    	FileConfiguration config = getConfig();
        if (config.getBoolean("punish-settings.enable-time")) {
        	runner.startTimer();
        }
        if (config.getBoolean("punish-settings.notadmin-punish")) {
        	runner.adminCheck();
        }
        if (config.getBoolean("secure-settings.enable-op-whitelist")) {
        	runner.startOpCheck();
        }
        if (config.getBoolean("secure-settings.enable-permission-blacklist")) {
        	runner.startPermsCheck();
        }
    }
    
    public void checkForUpdates() {
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
    
    public void logEnableDisable(String msg, Date date) {
    	if (getConfig().getBoolean("logging-settings.logging-enable-disable")) {
        	logToFile(msg.replace("%date%", DATE_FORMAT.format(date)));
        }	
    }
    
    public void handleInteraction(Player player, Cancellable event) {
        if (login.contains(player.getName())) {
            event.setCancelled(true);
        }
    }
    
    public Config getPluginConfig() {
    	return pluginConfig;
    }
    
    public String getMessage(String key) {
        return Utils.colorize(message.getString(key, "&4&lERROR&r").replace("%prefix%", getPluginConfig().main_settings_prefix));
    }

    public String getMessage(String key, UnaryOperator<String> preprocess) {
        return Utils.colorize(preprocess.apply(message.getString(key, "&4&lERROR&r")).replace("%prefix%", getPluginConfig().main_settings_prefix));
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
	    File dataFolder = getDataFolder();
	    if (!dataFolder.exists() && !dataFolder.mkdirs()) {
	        throw new RuntimeException("Unable to create data folder");
	    }
	    File saveTo = fullpath ? new File(getConfig().getString("file-settings.log-file-path"), "log.yml")
	                           : new File(dataFolder, "log.yml");
	    try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(saveTo, true)))) {
	        pw.println(message);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
}
