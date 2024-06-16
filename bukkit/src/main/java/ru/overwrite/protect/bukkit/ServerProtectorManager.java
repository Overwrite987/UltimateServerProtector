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
import ru.overwrite.protect.bukkit.api.CaptureReason;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.commands.PasCommand;
import ru.overwrite.protect.bukkit.commands.UspCommand;
import ru.overwrite.protect.bukkit.listeners.MainListener;
import ru.overwrite.protect.bukkit.listeners.ChatListener;
import ru.overwrite.protect.bukkit.listeners.ConnectionListener;
import ru.overwrite.protect.bukkit.listeners.TabCompleteListener;
import ru.overwrite.protect.bukkit.task.BukkitRunner;
import ru.overwrite.protect.bukkit.task.PaperRunner;
import ru.overwrite.protect.bukkit.task.Runner;
import ru.overwrite.protect.bukkit.task.TaskManager;
import ru.overwrite.protect.bukkit.utils.Config;
import ru.overwrite.protect.bukkit.utils.PAPIUtils;
import ru.overwrite.protect.bukkit.utils.PluginMessage;
import ru.overwrite.protect.bukkit.utils.Utils;
import ru.overwrite.protect.bukkit.utils.logging.BukkitLogger;
import ru.overwrite.protect.bukkit.utils.logging.PaperLogger;

import java.io.*;
import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServerProtectorManager extends JavaPlugin {

	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("[dd-MM-yyy] HH:mm:ss -");
	private final Logger pluginLogger = Utils.FOLIA ? new PaperLogger(this) : new BukkitLogger(this);

	public boolean proxy = false;

	public boolean paper;

	public FileConfiguration messageFile;
	public FileConfiguration dataFile;

	public String dataFileName;

	public String path;

	private final Config pluginConfig = new Config(this);
	private final ServerProtectorAPI api = new ServerProtectorAPI(this);
	private final PasswordHandler passwordHandler = new PasswordHandler(this);
	private final Runner runner = Utils.FOLIA ? new PaperRunner(this) : new BukkitRunner(this);
	private PluginMessage pluginMessage;

	public Map<String, Integer> time;

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
		return pluginLogger;
	}

	public Runner getRunner() {
		return runner;
	}

	public boolean checkPaper(FileConfiguration messageFile) {
		if (server.getName().equals("CraftBukkit")) {
			pluginLogger.info(messageFile.getString("system.baseline-warn", "§6============= §c! WARNING ! §c============="));
			pluginLogger.info(messageFile.getString("system.paper-1", "§eYou are using an unstable core for your MC server! It's recommended to use §aPaper"));
			pluginLogger.info(messageFile.getString("system.paper-2", "§eDownload Paper: §ahttps://papermc.io/downloads/all"));
			pluginLogger.info(messageFile.getString("system.baseline-warn", "§6============= §c! WARNING ! §c============="));
			return false;
		}
		return  true;
	}

	public boolean isSafe(FileConfiguration messageFile, PluginManager pluginManager) {
		if (getServer().spigot().getConfig().getBoolean("settings.bungeecord")) {
			if (pluginManager.isPluginEnabled("BungeeGuard")) {
				return true;
			}
			pluginLogger.info(messageFile.getString("system.baseline-warn", "§6============= §c! WARNING ! §c============="));
			pluginLogger.info(messageFile.getString("system.bungeecord-1", "§eYou have the §6bungeecord setting §aenabled§e, but the §6BungeeGuard §eplugin is not installed!"));
			pluginLogger.info(messageFile.getString("system.bungeecord-2", "§eWithout this plugin, you are exposed to §csecurity risks! §eInstall it for further safe operation."));
			pluginLogger.info(messageFile.getString("system.bungeecord-3", "§eDownload BungeeGuard: §ahttps://www.spigotmc.org/resources/bungeeguard.79601/"));
			pluginLogger.info(messageFile.getString("system.baseline-warn", "§6============= §c! WARNING ! §c============="));
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
		ConfigurationSection fileSettings = config.getConfigurationSection("file-settings");
		boolean fullPath = fileSettings.getBoolean("use-full-path", false);
		path = fullPath ? fileSettings.getString("data-file-path") : getDataFolder().getAbsolutePath();
		dataFileName = fileSettings.getString("data-file");
		dataFile = pluginConfig.getFile(path, dataFileName);
		pluginConfig.save(path, dataFile, dataFileName);
		messageFile = pluginConfig.getFile(getDataFolder().getAbsolutePath(), "message.yml");
		pluginConfig.save(getDataFolder().getAbsolutePath(), messageFile, "message.yml");
		setupPluginConfig(config);
		pluginConfig.setupPasswords(dataFile);
	}

	public void reloadConfigs() {
		runner.runAsync(() -> {
			reloadConfig();
			FileConfiguration config = getConfig();
			messageFile = pluginConfig.getFile(getDataFolder().getAbsolutePath(), "message.yml");
			ConfigurationSection fileSettings = config.getConfigurationSection("file-settings");
			boolean fullPath = fileSettings.getBoolean("use-full-path", false);
			path = fullPath ? fileSettings.getString("data-file-path") : getDataFolder().getAbsolutePath();
			dataFileName = fileSettings.getString("data-file");
			dataFile = pluginConfig.getFile(path, dataFileName);
			setupPluginConfig(config);
			pluginConfig.setupPasswords(dataFile);
		});
	}

	private void setupPluginConfig(FileConfiguration config) {
		pluginConfig.loadPerms(config);
		pluginConfig.loadLists(config);
		pluginConfig.setupExcluded(config);
		FileConfiguration configFile = pluginConfig.getFile(path, "config.yml");
		pluginConfig.loadMainSettings(config, configFile);
		pluginConfig.loadEncryptionSettings(config, configFile);
		pluginConfig.loadSecureSettings(config, configFile);
		pluginConfig.loadGeyserSettings(config, configFile);
		pluginConfig.loadAdditionalChecks(config, configFile);
		pluginConfig.loadPunishSettings(config, configFile);
		pluginConfig.loadSessionSettings(config, configFile);
		pluginConfig.loadMessageSettings(config, configFile);
		pluginConfig.loadBossbarSettings(config, configFile);
		pluginConfig.loadSoundSettings(config, configFile);
		pluginConfig.loadEffects(config, configFile);
		pluginConfig.loadLoggingSettings(config, configFile);
		pluginConfig.loadMsgMessages(messageFile);
		pluginConfig.loadUspMessages(messageFile);
		ConfigurationSection messageSettings = config.getConfigurationSection("message-settings");
		if (messageSettings.getBoolean("send-titles")) {
			pluginConfig.loadTitleMessages(messageFile);
		}
		if (messageSettings.getBoolean("enable-broadcasts")
				|| messageSettings.getBoolean("enable-console-broadcasts")) {
			pluginConfig.loadBroadcastMessages(messageFile);
		}
	}

	public void registerListeners(PluginManager pluginManager) {
		pluginManager.registerEvents(new ChatListener(this), this);
		pluginManager.registerEvents(new ConnectionListener(this), this);
		pluginManager.registerEvents(new MainListener(this), this);
		if (paper && pluginConfig.blocking_settings_block_tab_complete) {
			pluginManager.registerEvents(new TabCompleteListener(this), this);
		}
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
				pluginLogger.info("Unable to register password command!");
				e.printStackTrace();
				pluginManager.disablePlugin(this);
			}
		} else {
			pluginLogger.info("Command for password entering will not be registered.");
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
			time = new ConcurrentHashMap<>();
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
		boolean fullPath = file_settings.getBoolean("use-full-path");
		String logFilePath = fullPath ? file_settings.getString("log-file-path") : dataFolder.getPath();
		logFile = new File(logFilePath, file_settings.getString("log-file"));
	}

	public void checkForUpdates(FileConfiguration config, FileConfiguration messageFile) {
		if (!config.getBoolean("main-settings.update-checker")) {
			return;
		}
		Utils.checkUpdates(this, version -> {
			pluginLogger.info(messageFile.getString("system.baseline-default", "§6========================================"));
			if (getDescription().getVersion().equals(version)) {
				pluginLogger.info(messageFile.getString("system.update-latest", "§aYou are using latest version of the plugin!"));
			} else {
				pluginLogger.info(messageFile.getString("system.update-outdated-1", "§aYou are using outdated version of the plugin!"));
				pluginLogger.info(messageFile.getString("system.update-outdated-2", "§aYou can download new version here:"));
				pluginLogger.info(messageFile.getString("system.update-outdated-3", "§bgithub.com/Overwrite987/UltimateServerProtector/releases/"));
			}
			pluginLogger.info(messageFile.getString("system.baseline-default", "§6========================================"));
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

	public void applyHide(Player p) {
		if (pluginConfig.blocking_settings_hide_on_entering) {
			runner.runPlayer(() -> {
				for (Player onlinePlayer : server.getOnlinePlayers()) {
					if (!onlinePlayer.equals(p)) {
						onlinePlayer.hidePlayer(this, p);
					}
				}
			}, p);
		}
		if (pluginConfig.blocking_settings_hide_other_on_entering) {
			runner.runPlayer(() -> {
				for (Player onlinePlayer : server.getOnlinePlayers()) {
					p.hidePlayer(this, onlinePlayer);
				}
			}, p);
		}
	}

	public void logEnableDisable(String msg, Date date) {
		if (getConfig().getBoolean("logging-settings.logging-enable-disable")) {
			logToFile(msg.replace("%date%", DATE_FORMAT.format(date)));
		}
	}

	public CaptureReason checkPermissions(Player p) {
		if (p.isOp()) {
			return new CaptureReason(CaptureReason.Reason.OPERATOR, null);
		}
		if (p.hasPermission("serverprotector.protect")) {
			return new CaptureReason(CaptureReason.Reason.PERMISSION, "serverprotector.protect");
		}
		for (String s : pluginConfig.perms) {
			if (p.hasPermission(s)) {
				return new CaptureReason(CaptureReason.Reason.PERMISSION, s);
			}
		}
		return null;
	}

	public boolean isExcluded(Player p, List<String> list) {
		return pluginConfig.secure_settings_enable_excluded_players && list.contains(p.getName());
	}

	public boolean isAdmin(String nick) {
		return pluginConfig.per_player_passwords.containsKey(nick);
	}

	public void sendAlert(Player p, String msg) {
		if (pluginConfig.message_settings_enable_broadcasts) {
			msg = msg.replace("%player%", p.getName()).replace("%ip%", Utils.getIp(p));
			if (pluginConfig.main_settings_papi_support) {
				msg = PAPIUtils.parsePlaceholders(p, msg, pluginConfig.serializer);
			}
			for (Player ps : server.getOnlinePlayers()) {
				if (ps.hasPermission("serverprotector.admin")) {
					ps.sendMessage(msg);
				}
			}
			if (proxy) {
				pluginMessage.sendCrossProxy(p, msg);
			}
		}
		if (pluginConfig.message_settings_enable_console_broadcasts) {
			msg = msg.replace("%player%", p.getName()).replace("%ip%", Utils.getIp(p));
			if (pluginConfig.main_settings_papi_support) {
				msg = PAPIUtils.parsePlaceholders(p, msg, pluginConfig.serializer);
			}
			server.getConsoleSender().sendMessage(msg);
		}
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
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}