package ru.overwrite.protect.bukkit;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
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
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.commands.PasCommand;
import ru.overwrite.protect.bukkit.commands.UspCommand;
import ru.overwrite.protect.bukkit.listeners.AdditionalListener;
import ru.overwrite.protect.bukkit.listeners.ChatListener;
import ru.overwrite.protect.bukkit.listeners.ConnectionListener;
import ru.overwrite.protect.bukkit.listeners.InteractionsListener;
import ru.overwrite.protect.bukkit.task.BukkitRunner;
import ru.overwrite.protect.bukkit.task.PaperRunner;
import ru.overwrite.protect.bukkit.task.Runner;
import ru.overwrite.protect.bukkit.task.TaskManager;
import ru.overwrite.protect.bukkit.utils.Config;
import ru.overwrite.protect.bukkit.utils.PluginMessage;
import ru.overwrite.protect.bukkit.utils.Utils;
import ru.overwrite.protect.bukkit.utils.logging.BukkitLogger;
import ru.overwrite.protect.bukkit.utils.logging.PaperLogger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.*;

public class ServerProtectorManager extends JavaPlugin {

	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("[dd-MM-yyy] HH:mm:ss -");
	private final Logger logger = Utils.FOLIA ? new PaperLogger(this) : new BukkitLogger(this);
	
	public static String serialiser; // this is bullshit
	public boolean proxy = false;

	public FileConfiguration messageFile;
	public FileConfiguration dataFile;

	public String path;

	public Set<String> login = new HashSet<>();

	public Map<String, Integer> time;

	private final Config pluginConfig = new Config(this);
	private final ServerProtectorAPI api = new ServerProtectorAPI(this);
	private final PasswordHandler passwordHandler = new PasswordHandler(this);
	private final Runner runner = Utils.FOLIA ? new PaperRunner(this) : new BukkitRunner(this);
	private PluginMessage pluginMessage;

	private File logFile;

	public final Server server = getServer();

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

	public Logger getPluginLogger() {
		return logger;
	}

	public Runner getRunner() {
		return runner;
	}

	public boolean isPaper() {
		if (server.getName().equals("CraftBukkit")) {
			loggerInfo("§6============= §c! WARNING ! §c=============");
			loggerInfo("§eYou are using an unstable core for your MC server! It's recomended to use §aPaper");
			loggerInfo("§eDownload Paper: §ahttps://papermc.io/downloads/all");
			loggerInfo("§6============= §c! WARNING ! §c=============");
			this.setEnabled(false);
			return false;
		}
		return true;
	}

	public boolean isSafe(PluginManager pluginManager) {
		if (getServer().spigot().getConfig().getBoolean("settings.bungeecord")) {
			if (pluginManager.isPluginEnabled("BungeeGuard")) {
				return true;
			} else {
				loggerInfo("§c============= §6! WARNING ! §c=============");
				loggerInfo("§eYou have the §6bungeecord setting §aenabled§e, but the §6BungeeGuard §eplugin is not installed!");
				loggerInfo("§eWithout this plugin, you are exposed to §csecurity risks! §eInstall it for further safe operation.");
				loggerInfo("§eDownload BungeeGuard: §ahttps://www.spigotmc.org/resources/bungeeguard.79601/");
				loggerInfo("§c============= §6! WARNING ! §c=============");
				server.shutdown();
				return false;
			}
		}
		return true;
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
		ConfigurationSection file_settings = config.getConfigurationSection("file-settings");
		boolean fullpath = file_settings.getBoolean("use-full-path");
		path = fullpath ? file_settings.getString("data-file-path") : getDataFolder().getAbsolutePath();
		dataFile = pluginConfig.getFile(path, file_settings.getString("data-file"));
		pluginConfig.save(path, dataFile, file_settings.getString("data-file"));
		pluginConfig.setupPasswords(dataFile);
		messageFile = pluginConfig.getFile(getDataFolder().getAbsolutePath(), "message.yml");
		pluginConfig.save(getDataFolder().getAbsolutePath(), messageFile, "message.yml");
		pluginConfig.loadPerms(config);
		pluginConfig.loadLists(config);
		pluginConfig.setupExcluded(config);
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
		pluginConfig.loadMsgMessages(messageFile);
		if (config.getBoolean("message-settings.send-titles")) {
			pluginConfig.loadTitleMessages(messageFile);
		}
		if (config.getBoolean("bossbar-settings.enable-bossbar")) {
			pluginConfig.loadBossbar(config);
		}
		if (config.getBoolean("message-settings.enable-broadcasts")
				|| config.getBoolean("message-settings.enable-console-broadcasts")) {
			pluginConfig.loadBroadcastMessages(messageFile);
		}
		pluginConfig.loadUspMessages(messageFile);
	}

	public void reloadConfigs(FileConfiguration config) {
		reloadConfig();
		serialiser = config.getString("main-settings.serialiser");
		messageFile = pluginConfig.getFile(getDataFolder().getAbsolutePath(), "message.yml");
		ConfigurationSection file_settings = config.getConfigurationSection("file-settings");
		boolean fullpath = file_settings.getBoolean("use-full-path");
		path = fullpath ? file_settings.getString("data-file-path") : getDataFolder().getAbsolutePath();
		dataFile = pluginConfig.getFile(path, file_settings.getString("data-file"));
		pluginConfig.setupPasswords(dataFile);
		pluginConfig.loadPerms(config);
		pluginConfig.loadLists(config);
		pluginConfig.setupExcluded(config);
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
		pluginConfig.loadMsgMessages(messageFile);
		if (config.getBoolean("message-settings.send-titles")) {
			pluginConfig.loadTitleMessages(messageFile);
		}
		if (config.getBoolean("bossbar-settings.enable-bossbar")) {
			pluginConfig.loadBossbar(config);
		}
		if (config.getBoolean("message-settings.enable-broadcasts")) {
			pluginConfig.loadBroadcastMessages(messageFile);
		}
		pluginConfig.loadUspMessages(messageFile);
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
				commandMap.register(getDescription().getName(), command);
			} catch (Exception e) {
				loggerInfo("Unable to register password command!");
				e.printStackTrace();
				pluginManager.disablePlugin(this);
			}
		} else {
			loggerInfo("Using chat for password entering!");
		}
		PluginCommand uspCommand = getCommand("ultimateserverprotector");
		UspCommand uspCommandClass = new UspCommand(this);
		uspCommand.setExecutor(uspCommandClass);
		uspCommand.setTabCompleter(uspCommandClass);
	}

	public void startTasks(FileConfiguration config) {
		TaskManager taskManager = new TaskManager(this);
		taskManager.startMainCheck();
		taskManager.startCapturesMessages(config);
		if (pluginConfig.punish_settings_enable_time) {
			time = new Object2ObjectOpenHashMap<>();
			taskManager.startCapturesTimer(config);
		}
		if (pluginConfig.secure_settings_enable_notadmin_punish) {
			taskManager.startAdminCheck(config);
		}
		if (pluginConfig.secure_settings_enable_op_whitelist) {
			taskManager.startOpCheck(config);
		}
		if (pluginConfig.secure_settings_enable_permission_blacklist) {
			taskManager.startPermsCheck(config);
		}
	}

	public void setupLogger(FileConfiguration config) {
		File dataFolder = getDataFolder();
		if (!dataFolder.exists() && !dataFolder.mkdirs()) {
			throw new RuntimeException("Unable to create data folder");
		}
		ConfigurationSection file_settings = config.getConfigurationSection("file-settings");
		boolean fullpath = file_settings.getBoolean("use-full-path");
		String logFilePath = fullpath ? file_settings.getString("log-file-path") : dataFolder.getPath();
		logFile = new File(logFilePath, file_settings.getString("log-file"));
	}

	public void checkForUpdates(FileConfiguration config) {
		if (!config.getBoolean("main-settings.update-checker")) {
			return;
		}
		Utils.checkUpdates(this, version -> {
			loggerInfo("§6========================================");
			if (getDescription().getVersion().equals(version)) {
				loggerInfo("§aYou are using latest version of the plugin!");
			} else {
				loggerInfo("§aYou are using outdated version of the plugin!");
				loggerInfo("§aYou can download new version here:");
				loggerInfo("§bgithub.com/Overwrite987/UltimateServerProtector/releases/");
			}
			loggerInfo("§6========================================");
		});
	}

	public void checkFail(String playerName, List<String> command) {
		runner.run(() -> {
			for (String c : command) {
				server.dispatchCommand(server.getConsoleSender(), c.replace("%player%", playerName));
			}
		});
	}

	public void giveEffect(Player player) {
		runner.runPlayer(() -> {
			for (String s : pluginConfig.effect_settings_effects) {
				String[] splittedEffect = s.split(";");
				PotionEffectType types = PotionEffectType.getByName(splittedEffect[0].toUpperCase());
				int level = Integer.parseInt(splittedEffect[1]) - 1;
				player.addPotionEffect(new PotionEffect(types, 99999, level));
			}
		}, player);
	}

	public void logEnableDisable(String msg, Date date) {
		if (getConfig().getBoolean("logging-settings.logging-enable-disable")) {
			logToFile(msg.replace("%date%", DATE_FORMAT.format(date)));
		}
	}

	public boolean isPermissions(Player p) {
		if (p.isOp() || p.hasPermission("serverprotector.protect"))
			return true;
		for (String s : pluginConfig.perms) {
			if (p.hasPermission(s)) {
				return true;
			}
		}
		return false;
	}

	public boolean isExcluded(Player p, List<String> list) {
		return pluginConfig.secure_settings_enable_excluded_players && list.contains(p.getName());
	}

	public boolean isAdmin(String nick) {
		return pluginConfig.per_player_passwords.containsKey(nick);
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
		logger.info(logMessage);
	}

	public void logAction(String key, Player player, Date date) {
		runner.runAsync(() ->
				logToFile(messageFile.getString(key, "ERROR")
						.replace("%player%", player.getName())
                		.replace("%ip%", Utils.getIp(player))
						.replace("%date%", DATE_FORMAT.format(date)))
		);
	}

	public void logToFile(String message) {
		try {
			FileWriter fileWriter = new FileWriter(logFile, true);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(message);
			bufferedWriter.newLine();
			bufferedWriter.flush();
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}