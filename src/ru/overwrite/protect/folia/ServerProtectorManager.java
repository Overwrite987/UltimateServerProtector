package ru.overwrite.protect.folia;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.function.UnaryOperator;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import ru.overwrite.protect.folia.listeners.AdditionalListener;
import ru.overwrite.protect.folia.listeners.ChatListener;
import ru.overwrite.protect.folia.listeners.ConnectionListener;
import ru.overwrite.protect.folia.listeners.InteractionsListener;
import ru.overwrite.protect.folia.commands.*;
import ru.overwrite.protect.folia.utils.Config;
import ru.overwrite.protect.folia.utils.Utils;

public class ServerProtectorManager extends JavaPlugin {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("[dd-MM-yyy] HH:mm:ss -");
	
	public static FileConfiguration message;
	public static FileConfiguration data;
    
    public Set<String> perms;
    public Set<String> ips = Collections.newSetFromMap(new ConcurrentHashMap<>()); 
    public Set<String> login = Collections.newSetFromMap(new ConcurrentHashMap<>()); 
    public Set<String> saved = Collections.newSetFromMap(new ConcurrentHashMap<>()); 
    public Map<Player, Integer> time = new ConcurrentHashMap<>();
    
    public static boolean fullpath = false;
    
    private final PluginManager pluginManager = getServer().getPluginManager();
    
    public final Logger logger = getLogger();
    
    public void checkPaper() {
    	if (getServer().getName().equals("CraftBukkit")) {
            logger.info("§6============= §6! WARNING ! §c=============");
            logger.info("§eЭтот плагин работает только на Paper и его форках!");
            logger.info("§eАвтор плагина §cкатегорически §eвыступает за отказ от использования устаревшего и уязвимого софта!");
            logger.info("§eСкачать Paper: §ahttps://papermc.io/downloads/all");
            logger.info("§6============= §6! WARNING ! §c=============");
            setEnabled(false);
            return;
        }    	
    }
    
    public void saveConfigs() {
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        fullpath = config.getBoolean("file-settings.use-full-path");
        data = fullpath ? Config.getFileFullPath(config.getString("file-settings.data-file")) : Config.getFile(config.getString("file-settings.data-file"));
        Config.save(data, config.getString("file-settings.data-file"));
        message = Config.getFile("message.yml");
        Config.save(message, "message.yml");
        perms = new HashSet<>(config.getStringList("permissions"));
        Config.loadMainSettings(config);
        Config.loadSecureSettings(config);
        Config.loadAdditionalChecks(config);
        Config.loadAttempts(config);
        Config.loadTime(config);
        Config.loadSessionSettings(config);
        Config.loadMessageSettings(config);
        Config.loadSoundSettings(config);
        Config.loadEffects(config);
        Config.loadMsgMessages();
        if (config.getBoolean("message-settings.send-titles")) {
        	Config.loadTitleMessages();
        }
        if (config.getBoolean("bossbar-settings.enable-bossbar")) {
        	Config.loadBossbar(config);
        }
        if (config.getBoolean("message-settings.enable-broadcasts")) {
        	Config.loadBroadcastMessages();
        }
        Config.loadUspMessages();
    }
    
    public void reloadConfigs() {
    	reloadConfig();
		FileConfiguration config = getConfig();
        message = Config.getFile("message.yml");
        data = fullpath ? Config.getFileFullPath(config.getString("file-settings.data-file")) : Config.getFile(config.getString("file-settings.data-file"));
        perms = new HashSet<>(config.getStringList("permissions"));
        Config.loadMainSettings(config);
        Config.loadSecureSettings(config);
        Config.loadAdditionalChecks(config);
        Config.loadAttempts(config);
        Config.loadTime(config);
        Config.loadSessionSettings(config);
        Config.loadMessageSettings(config);
        Config.loadSoundSettings(config);
        Config.loadEffects(config);
        Config.loadMsgMessages();
        if (config.getBoolean("message-settings.send-broadcasts")) {
        	Config.loadTitleMessages();
        }
        if (config.getBoolean("bossbar-settings.enable-bossbar")) {
        	Config.loadBossbar(config);
        }
        if (config.getBoolean("message-settings.enable-broadcasts")) {
        	Config.loadBroadcastMessages();
        }
        Config.loadUspMessages();
    }
    
    public void registerListeners() {
    	pluginManager.registerEvents(new ChatListener(), this);
        pluginManager.registerEvents(new ConnectionListener(), this);
        pluginManager.registerEvents(new InteractionsListener(), this);
        pluginManager.registerEvents(new AdditionalListener(), this);
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
                command.setExecutor(new PasCommand());
            } catch (Exception e) {
                logger.info("Невозможно определить команду. Вероятно поле pas-command пусто.");
                e.printStackTrace();
                pluginManager.disablePlugin(this);
            }
        } else {
            logger.info("Для ввода пароля используется чат!");
        }
        Objects.requireNonNull(getCommand("ultimateserverprotector")).setExecutor(new UspCommand());
        Objects.requireNonNull(getCommand("ultimateserverprotector")).setTabCompleter(new UspTabCompleter());
    }
    
    public void startRunners() {
    	Runner runner = new Runner();
    	runner.run();
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
                logger.info("§aВы используете последнюю версию плагина!");
            } else {
                logger.info("§aВы используете устаревшую или некорректную версию плагина!");
                logger.info("§aВы можете загрузить последнюю версию плагина здесь:");
                logger.info("§bhttps://github.com/Overwrite987/UltimateServerProtector/releases/");
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
    
    public static String getMessage(String key) {
        return Utils.colorize(message.getString(key, "&4&lERROR&r").replace("%prefix%", Config.main_settings_prefix));
    }

    public static String getMessage(String key, UnaryOperator<String> preprocess) {
        return Utils.colorize(preprocess.apply(message.getString(key, "&4&lERROR&r")).replace("%prefix%", Config.main_settings_prefix));
    }
    
    public boolean isPermissions(Player p) {
        if (p.isOp() || p.hasPermission("serverprotector.protect")) return true;
        for (String s : perms) {
            if (p.hasPermission(s)) {
            	return true;
            }
        }
        return false;
    }
    
    public boolean isExcluded(Player p) {
    	return Config.secure_settings_enable_excluded_players && getConfig().getStringList("excluded-players").contains(p.getName());
    }

    public boolean isAdmin(String nick) {
    	FileConfiguration config = getConfig();
    	data = fullpath ? Config.getFileFullPath(config.getString("file-settings.data-file")) : Config.getFile(config.getString("file-settings.data-file"));
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
