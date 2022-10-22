package ru.overwrite.protect;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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

import ru.overwrite.protect.listeners.AdditionalListener;
import ru.overwrite.protect.listeners.ChatListener;
import ru.overwrite.protect.listeners.ConnectionListener;
import ru.overwrite.protect.listeners.InteractionsListener;
import ru.overwrite.protect.utils.Config;
import ru.overwrite.protect.utils.Utils;

public class ServerProtectorManager extends JavaPlugin {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("[dd-MM-yyy] HH:mm:ss -");
	
	public static FileConfiguration message;
    public static String prefix;

    public final Set<String> ips = new HashSet<>();
    public final Map<Player, Integer> login = new HashMap<>();
    public final Map<Player, Integer> time = new HashMap<>();
    
    public static boolean fullpath = false;
    
    PluginManager pluginManager = Bukkit.getPluginManager();
    
    public void checkPaper() {
    	if (getServer().getName().equals("CraftBukkit")) {
        	getLogger().info("§6=============§c! WARNING ! §c=============");
  		    getLogger().info("§eYou are using an unstable core for your MC server! It's recomended to use §aPaper");
  		    getLogger().info("§eDownload Paper for newest version: §ahttps://papermc.io/downloads");
  		    getLogger().info("§eDownload Paper for older versions: §ahttps://papermc.io/legacy");
  		    getLogger().info("§6=============§c! WARNING ! §c=============");
            return;
        }    	
    }
    
    public void saveConfigs() {
    	saveDefaultConfig();
        if (getConfig().getBoolean("file-settings.use-full-path")) {
        	fullpath = true;
        }
        if (fullpath) {
        	FileConfiguration data = Config.getFileFullPath(getConfig().getString("file-settings.data-file"));
        	Config.saveFullPath(data, getConfig().getString("file-settings.data-file"));
        } else {
        	FileConfiguration data = Config.getFile(getConfig().getString("file-settings.data-file"));
        	Config.save(data, getConfig().getString("file-settings.data-file"));
        }
        message = Config.getFile("message.yml");
        Config.save(message, "message.yml");
        prefix = Utils.colorize(getConfig().getString("main-settings.prefix"));
    }
    
    public void registerListeners() {
    	pluginManager.registerEvents(new ChatListener(), this);
        pluginManager.registerEvents(new ConnectionListener(), this);
        pluginManager.registerEvents(new InteractionsListener(), this);
        pluginManager.registerEvents(new AdditionalListener(), this);
    }
    
    public void registerCommands(ServerProtector plugin) {
    	CommandClass commands = new CommandClass(plugin);
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
                command.setExecutor(commands);
            } catch (Exception e) {
                getLogger().info("Can't register command.");
                e.printStackTrace();
                pluginManager.disablePlugin(this);
            }
        } else {
            getLogger().info("For entering admin-password you need to write it into the chat!");
        }
        Objects.requireNonNull(getCommand("ultimateserverprotector")).setExecutor(commands);
    }
    
    public void startRunners() {
    	Runner runner = new Runner();
    	runner.runTaskTimerAsynchronously((Plugin)this, 20L, 40L);
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
       if (getConfig().getBoolean("main-settings.update-checker")) {
           Utils.checkUpdates(this, 105237, version -> {
        	   getLogger().info("§6========================================");
               if (this.getDescription().getVersion().equals(version)) {
                 getLogger().info("§aYou are using latest version of the plugin!");
               } else {
               	 getLogger().info("§aYou are using outdated version of the plugin!");
   	             getLogger().info("§aYou can download new version here:");
   	             getLogger().info("§bgithub.com/Overwrite987/UltimateServerProtector/releases/");
               }
               getLogger().info("§6========================================");
           });
        }
    }
    
    public void logEnableDisable(String msg, Date date) {
    	if (getConfig().getBoolean("logging-settings.logging-enable-disable")) {
        	logToFile(message.getString("log-format.disabled").replace("%date%", DATE_FORMAT.format(date)));
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
    
    public static String getMessagePrefixed(String key) {
        return prefix + getMessage(key);
    }

    public static String getMessagePrefixed(String key, UnaryOperator<String> preprocess) {
        return prefix + getMessage(key, preprocess);
    }

    public static String getMessage(String key) {
        return Utils.colorize(message.getString(key, "&4&lERROR&r"));
    }

    public static String getMessage(String key, UnaryOperator<String> preprocess) {
        return Utils.colorize(preprocess.apply(message.getString(key, "&4&lERROR&r")));
    }

    public static String getPrefix() {
        return prefix;
    }

    public boolean isPermissions(Player p) {
        if (p.isOp() || p.hasPermission("serverprotector.protect")) return true;
        for (String s : getConfig().getStringList("permissions")) {
            if (p.hasPermission(s)) return true;
        }
        return false;
    }

    public boolean isAdmin(String nick) {
    	FileConfiguration data;
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
        try {
            File dataFolder = getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdir();
            }
            File saveTo; 
            if (fullpath) {
            	saveTo = new File(getConfig().getString("file-settings.log-file-path"), "log.yml");
            } else {
            	saveTo = new File(getDataFolder(), "log.yml");
            }
            if (!saveTo.exists()) {
                saveTo.createNewFile();
            }
            FileWriter fw = new FileWriter(saveTo, true);
            PrintWriter pw = new PrintWriter(fw);
            pw.println(message);
            pw.flush();
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
