package ru.overwrite.protect.bukkit;

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

import java.io.*;
import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.*;

public class ServerProtectorManager extends JavaPlugin {

	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("[dd-MM-yyy] HH:mm:ss -");
	private final Logger logger = Utils.FOLIA ? new PaperLogger(this) : new BukkitLogger(this);

	public boolean proxy = false;

	public boolean paper;

	public FileConfiguration messageFile;
	public FileConfiguration dataFile;

	public String dataFileName;

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

	public boolean checkPaper(FileConfiguration messageFile) {
		if (server.getName().equals("CraftBukkit")) {
			loggerInfo(messageFile.getString("system.baseline-warn", "§6============= §c! WARNING ! §c============="));
			loggerInfo(messageFile.getString("system.paper-1", "§eYou are using an unstable core for your MC server! It's recommended to use §aPaper"));
			loggerInfo(messageFile.getString("system.paper-2", "§eDownload Paper: §ahttps://papermc.io/downloads/all"));
			loggerInfo(messageFile.getString("system.baseline-warn", "§6============= §c! WARNING ! §c============="));
			return false;
		}
		return  true;
	}

	public boolean isSafe(FileConfiguration messageFile, PluginManager pluginManager) {
		if (getServer().spigot().getConfig().getBoolean("settings.bungeecord")) {
			if (pluginManager.isPluginEnabled("BungeeGuard")) {
				return true;
			}
			loggerInfo(messageFile.getString("system.baseline-warn", "§6============= §c! WARNING ! §c============="));
			loggerInfo(messageFile.getString("system.bungeecord-1", "§eYou have the §6bungeecord setting §aenabled§e, but the §6BungeeGuard §eplugin is not installed!"));
			loggerInfo(messageFile.getString("system.bungeecord-2", "§eWithout this plugin, you are exposed to §csecurity risks! §eInstall it for further safe operation."));
			loggerInfo(messageFile.getString("system.bungeecord-3", "§eDownload BungeeGuard: §ahttps://www.spigotmc.org/resources/bungeeguard.79601/"));
			loggerInfo(messageFile.getString("system.baseline-warn", "§6============= §c! WARNING ! §c============="));
			server.shutdown();
			return false;
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
		ConfigurationSection file_settings = config.getConfigurationSection("file-settings");
		boolean fullPath = file_settings.getBoolean("use-full-path");
		path = fullPath ? file_settings.getString("data-file-path") : getDataFolder().getAbsolutePath();
		dataFileName = file_settings.getString("data-file");
		dataFile = pluginConfig.getFile(path, dataFileName);
		pluginConfig.save(path, dataFile, dataFileName);
		messageFile = pluginConfig.getFile(getDataFolder().getAbsolutePath(), "message.yml");
		pluginConfig.save(getDataFolder().getAbsolutePath(), messageFile, "message.yml");
		pluginConfig.loadPerms(config);
		pluginConfig.loadLists(config);
		pluginConfig.setupExcluded(config);
		pluginConfig.loadMainSettings(config);
		pluginConfig.loadEncryptionSettings(config);
		pluginConfig.loadSecureSettings(config);
		pluginConfig.loadAdditionalChecks(config);
		pluginConfig.loadAttempts(config);
		pluginConfig.loadTime(config);
		pluginConfig.loadSessionSettings(config);
		pluginConfig.loadMessageSettings(config);
		pluginConfig.loadSoundSettings(config);
		pluginConfig.loadEffects(config);
		pluginConfig.loadLoggingSettings(config);
		pluginConfig.loadBossbar(config);
		ConfigurationSection message_settings = config.getConfigurationSection("message-settings");
		if (message_settings.getBoolean("send-titles")) {
			pluginConfig.loadTitleMessages(messageFile);
		}
		if (message_settings.getBoolean("enable-broadcasts")
				|| message_settings.getBoolean("enable-console-broadcasts")) {
			pluginConfig.loadBroadcastMessages(messageFile);
		}
		pluginConfig.loadMsgMessages(messageFile);
		pluginConfig.loadUspMessages(messageFile);
		pluginConfig.setupPasswords(dataFile);
	}

	public void reloadConfigs() {
		runner.runAsync(() -> {
			reloadConfig();
			FileConfiguration config = getConfig();
			messageFile = pluginConfig.getFile(getDataFolder().getAbsolutePath(), "message.yml");
			ConfigurationSection file_settings = config.getConfigurationSection("file-settings");
			boolean fullPath = file_settings.getBoolean("use-full-path");
			path = fullPath ? file_settings.getString("data-file-path") : getDataFolder().getAbsolutePath();
			dataFileName = file_settings.getString("data-file");
			dataFile = pluginConfig.getFile(path, dataFileName);
			pluginConfig.loadPerms(config);
			pluginConfig.loadLists(config);
			pluginConfig.setupExcluded(config);
			pluginConfig.loadMainSettings(config);
			pluginConfig.loadEncryptionSettings(config);
			pluginConfig.loadSecureSettings(config);
			pluginConfig.loadAdditionalChecks(config);
			pluginConfig.loadAttempts(config);
			pluginConfig.loadTime(config);
			pluginConfig.loadSessionSettings(config);
			pluginConfig.loadMessageSettings(config);
			pluginConfig.loadSoundSettings(config);
			pluginConfig.loadEffects(config);
			pluginConfig.loadLoggingSettings(config);
			pluginConfig.loadBossbar(config);
			ConfigurationSection message_settings = config.getConfigurationSection("message-settings");
			if (message_settings.getBoolean("send-titles")) {
				pluginConfig.loadTitleMessages(messageFile);
			}
			if (message_settings.getBoolean("enable-broadcasts")
					|| message_settings.getBoolean("enable-console-broadcasts")) {
				pluginConfig.loadBroadcastMessages(messageFile);
			}
			pluginConfig.loadMsgMessages(messageFile);
			pluginConfig.loadUspMessages(messageFile);
			pluginConfig.setupPasswords(dataFile);
		});
	}

	public void registerListeners(PluginManager pluginManager) {
		pluginManager.registerEvents(new ChatListener(this), this);
		pluginManager.registerEvents(new ConnectionListener(this), this);
		pluginManager.registerEvents(new InteractionsListener(this), this);
		if (paper) { pluginManager.registerEvents(new AdditionalListener(this), this); }
	}

	public void registerCommands(PluginManager pluginManager, FileConfiguration config) {
		if (config.getBoolean("main-settings.use-command") && paper) {
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
			loggerInfo("Command for password entering will not be registered.");
		}
		PluginCommand uspCommand = getCommand("ultimateserverprotector");
		UspCommand uspCommandClass = new UspCommand(this);
		uspCommand.setExecutor(uspCommandClass);
		uspCommand.setTabCompleter(uspCommandClass);
	}

	public void startTasks(FileConfiguration config) {
		TaskManager taskManager = new TaskManager(this);
		taskManager.startMainCheck(pluginConfig.main_settings_check_interval);
		taskManager.startCapturesMessages(config);
		if (pluginConfig.punish_settings_enable_time) {
			time = new HashMap<>();
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

	public void checkForUpdates(FileConfiguration config, FileConfiguration messageFile) {
		if (!config.getBoolean("main-settings.update-checker")) {
			return;
		}
		Utils.checkUpdates(this, version -> {
			loggerInfo(messageFile.getString("system.baseline-default", "§6========================================"));
			if (getDescription().getVersion().equals(version)) {
				loggerInfo(messageFile.getString("system.update-latest", "§aYou are using latest version of the plugin!"));
			} else {
				loggerInfo(messageFile.getString("system.update-outdated-1", "§aYou are using outdated version of the plugin!"));
				loggerInfo(messageFile.getString("system.update-outdated-2", "§aYou can download new version here:"));
				loggerInfo(messageFile.getString("system.update-outdated-3", "§bgithub.com/Overwrite987/UltimateServerProtector/releases/"));
			}
			loggerInfo(messageFile.getString("system.baseline-default", "§6========================================"));
		});
	}

	public void checkFail(String playerName, List<String> command) {
		if (command.isEmpty()) {
			return;
		}
		runner.run(() -> {
			for (String c : command) {
				server.dispatchCommand(server.getConsoleSender(), c.replace("%player%", playerName));
				if (pluginConfig.logging_settings_logging_command_execution) {
					Date date = new Date();
					logToFile(messageFile.getString("log-format.command", "ERROR")
							.replace("%player%", playerName)
							.replace("%cmd%", c)
							.replace("%date%", DATE_FORMAT.format(date)));
				}
			}
		});
	}

	public void giveEffect(Player player) {
		runner.runPlayer(() -> {
			for (String s : pluginConfig.effect_settings_effects) {
				String[] splittedEffect = s.split(";");
				PotionEffectType types = PotionEffectType.getByName(splittedEffect[0].toUpperCase());
				int level = splittedEffect.length > 1 ? Integer.parseInt(splittedEffect[1]) - 1 : 0;
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
				logToFile(messageFile.getString(key, "ERROR: " + key + " does not exist!")
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