package ru.Overwrite.protect;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import ru.Overwrite.protect.listeners.AdditionalListener;
import ru.Overwrite.protect.listeners.ChatListener;
import ru.Overwrite.protect.listeners.JoinListener;
import ru.Overwrite.protect.listeners.LeaveListener;
import ru.Overwrite.protect.listeners.MainListener;
import ru.Overwrite.protect.utils.Config;
import ru.Overwrite.protect.utils.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.io.InputStream;
import java.net.URL;

public final class Main extends JavaPlugin {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("[dd-MM-yyy] HH:mm:ss -");

    private static FileConfiguration message;
    private static String prefix;

    public final Map<String, String> ips = new HashMap<>();

    public final Map<Player, Integer> login = new HashMap<>();

    public final Map<Player, Integer> time = new HashMap<>();

    public List<String> permissions;

    private static Main instance;

    public static Main getInstance() {
        return instance;
    }

    private CommandClass commands;


    public void onEnable() {
        if (getServer().getName().equals("CraftBukkit") || getServer().getName().equals("Spigot")) {
            getLogger().info("§6============= §6! WARNING ! §c=============");
            getLogger().info("§eЭтот плагин работает только на Paper и его форках!");
            getLogger().info("§eСкачать Paper для новых версий: §ahttps://papermc.io/downloads");
            getLogger().info("§eСкачать Paper для старых версий: §ahttps://papermc.io/legacy §7((в тесте выбирайте 2 вариант ответа))");
            getLogger().info("§6============= §6! WARNING ! §c=============");
            setEnabled(false);
            return;
        }
        long startTime = System.currentTimeMillis();
        Date date = new Date(startTime);
        instance = this;
        saveDefaultConfig();
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new ChatListener(), this);
        pluginManager.registerEvents(new JoinListener(), this);
        pluginManager.registerEvents(new LeaveListener(), this);
        pluginManager.registerEvents(new MainListener(), this);
        pluginManager.registerEvents(new AdditionalListener(), this);
        FileConfiguration data = Config.getFile(getConfig().getString("main-settings.data-file"));
        Config.save(data, getConfig().getString("main-settings.data-file"));
        message = Config.getFile("message.yml");
        Config.save(message, "message.yml");
        commands = new CommandClass(this);
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
                getLogger().info("Невозможно определить команду. Вероятно поле pas-command пусто.");
                pluginManager.disablePlugin(this);
            }
        } else {
            getLogger().info("Для ввода пароля используется чат!");
        }
        Objects.requireNonNull(getCommand("ultimateserverprotector")).setExecutor(commands);
        permissions = getConfig().getStringList("permissions");
        Bukkit.getScheduler().runTaskTimerAsynchronously(instance, Runner::run, 20L, 40L);
        Runner.startMSG();
        if (getConfig().getBoolean("main-settings.enable-metrics")) {
            new Metrics(this, 13347);
        }
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
	      new UpdateChecker(this, 105237).getVersion(version -> {
	        if (this.getDescription().getVersion().equals(version)) {
	           getLogger().info("§6========================================");
	           getLogger().info("§aYou are using latest version of the plugin!");
	           getLogger().info("§6========================================");
	       } else {
	           getLogger().info("§6========================================");
	           getLogger().info("§aYou are using outdated version of the plugin!");
	           getLogger().info("§aYou can download new version here:");
	           getLogger().info("§bgithub.com/Overwrite987/UltimateServerProtector/releases/");
	           getLogger().info("§6========================================");
	            }
	          });
	        }
        long endTime = System.currentTimeMillis();
        getLogger().info("Плагин включен за " + (endTime - startTime) + " милисекунд(ы)");
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        message = Config.getFile("message.yml");
        prefix = Utils.colorize(getConfig().getString("main-settings.prefix"));
    }

    public static String getMessageFull(String key) {
        return prefix + getMessage(key);
    }

    public static String getMessageFull(String key, UnaryOperator<String> preprocess) {
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
        for (String s : Main.getInstance().permissions) {
            if (p.hasPermission(s)) return true;
        }
        return false;
    }

    public boolean isAdmin(String nick) {
        FileConfiguration data = Config.getFile(getConfig().getString("main-settings.data-file"));
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
            File saveTo = new File(getDataFolder(), "log.yml");
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

    public void onDisable() {
        Date date = new Date();
        FileConfiguration message = Config.getFile("message.yml");
        Bukkit.getScheduler().cancelTasks(this);
        login.clear();
        ips.clear();
        time.clear();
        permissions.clear();
        commands.attempts.clear();
        if (getConfig().getBoolean("logging-settings.logging-enable-disable")) {
            logToFile(message.getString("log-format.disabled").replace("%date%", DATE_FORMAT.format(date)));
        }
        if (getConfig().getBoolean("message-settings.enable-broadcasts")) {
            Bukkit.broadcast(Main.getMessageFull("broadcasts.disabled"), "serverprotector.admin");
        }
        if (getConfig().getBoolean("secure-settings.shutdown-on-disable")) {
            Bukkit.shutdown();
        }
    }
}
  
