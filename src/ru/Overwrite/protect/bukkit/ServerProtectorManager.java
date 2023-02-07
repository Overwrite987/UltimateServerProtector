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
import java.util.Objects;
import java.util.Set;
import java.util.function.UnaryOperator;

import org.bukkit.Bukkit;
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
import ru.overwrite.protect.bukkit.commands.UspCommand;
import ru.overwrite.protect.bukkit.commands.PasCommand;
import ru.overwrite.protect.bukkit.utils.Config;
import ru.overwrite.protect.bukkit.utils.Utils;

public class ServerProtectorManager extends JavaPlugin {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("[dd-MM-yyy] HH:mm:ss -");
	
	public static FileConfiguration message;
	public static FileConfiguration data;
    public static String prefix;
    
    public Set<String> perms;
    public final Set<String> ips = new HashSet<>();
    public final Map<Player, Integer> login = new HashMap<>();
    public final Map<Player, Integer> time = new HashMap<>();
    
    public static boolean fullpath = false;
    
    PluginManager pluginManager = Bukkit.getPluginManager();
    
    public void checkPaper() {
    	if (getServer().getName().equals("CraftBukkit")) {
            getLogger().info("§6============= §6! WARNING ! §c=============");
            getLogger().info("§eЭтот плагин работает только на Paper и его форках!");
            getLogger().info("§eАвтор плагина §cкатегорически §eвыступает за отказ от использования устаревшего и уязвимого софта!");
            getLogger().info("§eСкачать Paper для новых версий: §ahttps://papermc.io/downloads");
            getLogger().info("§eСкачать Paper для старых версий: §ahttps://papermc.io/legacy §7((в тесте выбирайте 2 вариант ответа))");
            getLogger().info("§6============= §6! WARNING ! §c=============");
            setEnabled(false);
            return;
        }    	
    }
    
    public void saveConfigs() {
    	saveDefaultConfig();
        if (getConfig().getBoolean("file-settings.use-full-path")) {
        	fullpath = true;
        }
        if (fullpath) {
            data = Config.getFileFullPath(getConfig().getString("file-settings.data-file"));
        	Config.saveFullPath(data, getConfig().getString("file-settings.data-file"));
        } else {
        	data = Config.getFile(getConfig().getString("file-settings.data-file"));
        	Config.save(data, getConfig().getString("file-settings.data-file"));
        }
        message = Config.getFile("message.yml");
        Config.save(message, "message.yml");
        prefix = Utils.colorize(getConfig().getString("main-settings.prefix"));
        perms = new HashSet<String>(getConfig().getStringList("permissions"));
    }
    
    public void registerListeners() {
    	pluginManager.registerEvents(new ChatListener(), this);
        pluginManager.registerEvents(new ConnectionListener(), this);
        pluginManager.registerEvents(new InteractionsListener(), this);
        pluginManager.registerEvents(new AdditionalListener(), this);
    }
    
    public void registerCommands(ServerProtector plugin) {
    	PasCommand pascommand = new PasCommand();
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
                command.setExecutor(pascommand);
            } catch (Exception e) {
                getLogger().info("Невозможно определить команду. Вероятно поле pas-command пусто.");
                e.printStackTrace();
                pluginManager.disablePlugin(this);
            }
        } else {
            getLogger().info("Для ввода пароля используется чат!");
        }
        UspCommand uspcommand = new UspCommand();
        Objects.requireNonNull(getCommand("ultimateserverprotector")).setExecutor(uspcommand);
    }
    
    public void startRunners() {
    	Runner runner = new Runner();
    	runner.runTaskTimerAsynchronously((Plugin)this, 5L, 40L);
    	runner.startMSG();
        if (getConfig().getBoolean("punish-settings.enable-time")) {
        	runner.startTimer();
        }
        if (getConfig().getBoolean("punish-settings.notadmin-punish")) {
        	runner.adminCheck();
        }
        if (getConfig().getBoolean("secure-settings.enable-op-whitelist")) {
        	runner.startOpCheck();
        }
        if (getConfig().getBoolean("secure-settings.enable-permission-blacklist")) {
        	runner.startPermsCheck();
        }
    }
    
    public void checkForUpdates() {
        if (!getConfig().getBoolean("main-settings.update-checker")) {
            return;
        }

        Utils.checkUpdates(this, version -> {
            getLogger().info("§6========================================");
            if (getDescription().getVersion().equals(version)) {
                getLogger().info("§aВы используете последнюю версию плагина!");
            } else {
                getLogger().info("§aВы используете устаревшую или некорректную версию плагина!");
                getLogger().info("§aВы можете загрузить последнюю версию плагина здесь:");
                getLogger().info("§bhttps://github.com/Overwrite987/UltimateServerProtector/releases/");
            }
            getLogger().info("§6========================================");
        });
    }
    
    public void logEnableDisable(String msg, Date date) {
    	if (getConfig().getBoolean("logging-settings.logging-enable-disable")) {
        	logToFile(msg.replace("%date%", DATE_FORMAT.format(date)));
        }	
    }
    
    public void reloadConfigs() {
		reloadConfig();
        message = Config.getFile("message.yml");
    }
    
    public void handleInteraction(Player player, Cancellable event) {
        if (login.containsKey(player)) {
            event.setCancelled(true);
        }
    }

    public static String getMessage(String key) {
        return Utils.colorize(message.getString(key, "&4&lERROR&r").replace("%prefix%", prefix));
    }

    public static String getMessage(String key, UnaryOperator<String> preprocess) {
        return Utils.colorize(preprocess.apply(message.getString(key, "&4&lERROR&r")).replace("%prefix%", prefix));
    }

    public static String getPrefix() {
        return prefix;
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

    public boolean isAdmin(String nick) {
    	if (fullpath) {
            data = Config.getFileFullPath(getConfig().getString("file-settings.data-file"));
        } else {
        	data = Config.getFile(getConfig().getString("file-settings.data-file"));
        }
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
