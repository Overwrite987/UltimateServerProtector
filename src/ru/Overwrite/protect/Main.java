package ru.Overwrite.protect;

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
import ru.Overwrite.protect.listeners.AdditionalListener;
import ru.Overwrite.protect.listeners.ChatListener;
import ru.Overwrite.protect.listeners.ConnectionListener;
import ru.Overwrite.protect.listeners.InteractionsListener;
import ru.Overwrite.protect.utils.Config;
import ru.Overwrite.protect.utils.Utils;
import ru.Overwrite.protect.utils.Metrics;

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

public final class Main extends JavaPlugin {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("[dd-MM-yyy] HH:mm:ss -");

    private static FileConfiguration message;
    private static String prefix;

    public final PasswordHandler passwordHandler = new PasswordHandler(this);
    public final Set<String> ips = new HashSet<>();
    public final Map<Player, Integer> login = new HashMap<>();
    public final Map<Player, Integer> time = new HashMap<>();
    private static Main instance;
    
    public static boolean fullpath = false;

    public static Main getInstance() {
        return instance;
    }

    public static void handleInteraction(Player player, Cancellable event) {
        if (getInstance().login.containsKey(player)) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onEnable() {
        if (getServer().getName().equals("CraftBukkit")) {
        	getLogger().info("§6=============§6! WARNING ! §c=============");
  		    getLogger().info("§eYou are using an unstable core for your MC server! It's recomended to use Paper");
  		    getLogger().info("§eDownload Paper for newest versions: §ahttps://papermc.io/downloads");
  		    getLogger().info("§eDownload Paper for older versions: §ahttps://papermc.io/legacy");
  		    getLogger().info("§6=============§6! WARNING ! §c=============");
            setEnabled(false);
            return;
        }
        long startTime = System.currentTimeMillis();
        Date date = new Date(startTime);
        instance = this;
        saveDefaultConfig();
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new ChatListener(), this);
        pluginManager.registerEvents(new ConnectionListener(), this);
        pluginManager.registerEvents(new InteractionsListener(), this);
        pluginManager.registerEvents(new AdditionalListener(), this);
        if (fullpath) {
        	FileConfiguration data = Config.getFileFullPath(getConfig().getString("file-settings.data-file"));
        	Config.saveFullPath(data, getConfig().getString("file-settings.data-file"));
        } else {
        	FileConfiguration data = Config.getFile(getConfig().getString("file-settings.data-file"));
        	Config.save(data, getConfig().getString("file-settings.data-file"));
        }
        message = Config.getFile("message.yml");
        Config.save(message, "message.yml");
        CommandClass commands = new CommandClass(this);
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
        if (getConfig().getBoolean("file-settings.use-full-path")) {
        	fullpath = true;
        }
        Objects.requireNonNull(getCommand("ultimateserverprotector")).setExecutor(commands);
        Bukkit.getScheduler().runTaskTimerAsynchronously(instance, Runner::run, 20L, 40L);
        Runner.startMSG();
        if (getConfig().getBoolean("punish-settings.enable-time")) {
            Runner.startTimer();
        }
        if (getConfig().getBoolean("punish-settings.notadmin-punish")) {
            Runner.adminCheck();
        }
        if (getConfig().getBoolean("secure-settings.enable-op-whitelist")) {
            Runner.startOpCheck();
        }
        if (getConfig().getBoolean("secure-settings.enable-permission-blacklist")) {
            Runner.startPermsCheck();
        }
        if (getConfig().getBoolean("logging-settings.logging-enable-disable")) {
            logToFile(message.getString("log-format.enabled").replace("%date%", DATE_FORMAT.format(date)));
        }
        if (getConfig().getBoolean("main-settings.enable-metrics")) {
            new Metrics(this, 13347);
        }
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
        long endTime = System.currentTimeMillis();
        getLogger().info("Plugin started in " + (endTime - startTime) + " ms");
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

        message = Config.getFile("message.yml");
        prefix = Utils.colorize(getConfig().getString("main-settings.prefix"));
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
    	if (Main.fullpath) {
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

    @Override
    public void onDisable() {
        Date date = new Date();
        FileConfiguration message = Config.getFile("message.yml");
        Bukkit.getScheduler().cancelTasks(this);
        login.clear();
        ips.clear();
        time.clear();
        passwordHandler.clearAttempts();
        if (getConfig().getBoolean("logging-settings.logging-enable-disable")) {
            logToFile(message.getString("log-format.disabled").replace("%date%", DATE_FORMAT.format(date)));
        }
        if (getConfig().getBoolean("message-settings.enable-broadcasts")) {
            Bukkit.broadcast(Main.getMessagePrefixed("broadcasts.disabled"), "serverprotector.admin");
        }
        if (getConfig().getBoolean("secure-settings.shutdown-on-disable")) {
            Bukkit.shutdown();
        }
    }
}
  
