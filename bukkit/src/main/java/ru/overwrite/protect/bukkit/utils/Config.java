package ru.overwrite.protect.bukkit.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.overwrite.protect.bukkit.ServerProtectorManager;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Config {

	private final ServerProtectorManager plugin;

	public Config(ServerProtectorManager plugin) {
		this.plugin = plugin;
	}

	public String serializer;

	public Set<String> perms, blacklisted_perms, geyser_names;

	public Map<String, List<String>> ip_whitelist;

	public Map<String, String> per_player_passwords;

	public List<String> encryption_settings_encrypt_methods, effect_settings_effects, allowed_commands, op_whitelist, excluded_admin_pass, excluded_op_whitelist,
			excluded_ip_whitelist, excluded_blacklisted_perms;

	public List<List<String>> encryption_settings_old_encrypt_methods;

	public String[] titles_message, titles_incorrect, titles_correct, sound_settings_on_capture, sound_settings_on_pas_fail, sound_settings_on_pas_correct;

	public String geyser_prefix, uspmsg_consoleonly, uspmsg_reloaded, uspmsg_rebooted, uspmsg_playernotfound, uspmsg_alreadyinconfig, uspmsg_playeronly, uspmsg_logout,
			uspmsg_notinconfig, uspmsg_playeradded, uspmsg_playerremoved, uspmsg_ipadded, uspmsg_setpassusage,
			uspmsg_addopusage, uspmsg_remopusage, uspmsg_ipremoved, uspmsg_remipusage, uspmsg_addipusage,
			uspmsg_rempassusage, uspmsg_usage, uspmsg_usage_logout, uspmsg_usage_reload, uspmsg_usage_reboot, uspmsg_usage_encrypt, uspmsg_usage_setpass,
			uspmsg_usage_rempass, uspmsg_usage_addop, uspmsg_usage_remop, uspmsg_usage_addip, uspmsg_usage_remip, uspmsg_otherdisabled,
			msg_message, msg_incorrect, msg_correct, msg_noneed, msg_cantbenull, msg_playeronly, broadcasts_failed,
			broadcasts_passed, broadcasts_joined, broadcasts_captured, bossbar_message,
			bossbar_settings_bar_color, bossbar_settings_bar_style, main_settings_prefix, main_settings_pas_command;

	public boolean main_settings_papi_support, encryption_settings_enable_encryption, encryption_settings_auto_encrypt_passwords, blocking_settings_block_item_drop,
			blocking_settings_block_item_pickup, blocking_settings_block_tab_complete, blocking_settings_block_damage, blocking_settings_damaging_entity,
			blocking_settings_block_inventory_open, blocking_settings_hide_on_entering, blocking_settings_hide_other_on_entering, blocking_settings_allow_orientation_change, main_settings_use_command,
			main_settings_enable_admin_commands, punish_settings_enable_attempts, punish_settings_enable_time, punish_settings_enable_rejoin,
			bossbar_settings_enable_bossbar, secure_settings_enable_op_whitelist,
			secure_settings_enable_notadmin_punish, secure_settings_enable_permission_blacklist,
			secure_settings_enable_ip_whitelist, secure_settings_only_console_usp,
			secure_settings_enable_excluded_players, secure_settings_call_event_on_password_enter, session_settings_session, session_settings_session_time_enabled,
			message_settings_send_title, message_settings_enable_broadcasts, message_settings_enable_console_broadcasts,
			sound_settings_enable_sounds, effect_settings_enable_effects, logging_settings_logging_pas,
			logging_settings_logging_join, logging_settings_logging_enable_disable, logging_settings_logging_command_execution;

	public int encryption_settings_salt_length, punish_settings_max_attempts, punish_settings_time, punish_settings_max_rejoins, session_settings_session_time;

	public long main_settings_check_interval;

	public void setupPasswords(FileConfiguration dataFile) {
		per_player_passwords = new ConcurrentHashMap<>();
		ConfigurationSection data = dataFile.getConfigurationSection("data");
		boolean shouldSave = false;
		for (String nick : data.getKeys(false)) {
			// TODO: replace this mess with something better...
			String playerNick = nick;
			if (!geyser_prefix.isBlank() && geyser_names.contains(nick)) {
				playerNick = geyser_prefix + nick;
			}
			if (!encryption_settings_enable_encryption) {
				per_player_passwords.put(playerNick, data.getString(nick + ".pass"));
			} else {
				if (encryption_settings_auto_encrypt_passwords) {
					if (data.getString(nick + ".pass") != null) {
						String encryptedPas = Utils.encryptPassword(data.getString(nick + ".pass"), Utils.generateSalt(encryption_settings_salt_length), encryption_settings_encrypt_methods);
						dataFile.set("data." + nick + ".encrypted-pass", encryptedPas);
						dataFile.set("data." + nick + ".pass", null);
						shouldSave = true;
						plugin.dataFile = dataFile;
					}
				}
				per_player_passwords.put(playerNick, data.getString(nick + ".encrypted-pass"));
			}
		}
		if (shouldSave) {
			save(plugin.path, dataFile, plugin.dataFileName);
		}
	}

	public void loadMainSettings(FileConfiguration config) {
		ConfigurationSection mainSettings = config.getConfigurationSection("main-settings");
		serializer = mainSettings.getString("serializer", "LEGACY").toUpperCase();
		main_settings_prefix = mainSettings.getString("prefix");
		main_settings_pas_command = mainSettings.getString("pas-command");
		main_settings_use_command = mainSettings.getBoolean("use-command");
		main_settings_enable_admin_commands = mainSettings.getBoolean("enable-admin-commands");
		main_settings_check_interval = mainSettings.getLong("check-interval");
		main_settings_papi_support = mainSettings.getBoolean("papi-support");
	}

	public void loadGeyserSettings(FileConfiguration config) {
		ConfigurationSection geyserSettings = config.getConfigurationSection("geyser-settings");
		geyser_prefix = geyserSettings.getString("geyser-prefix");
		geyser_names = new HashSet<>(geyserSettings.getStringList("geyser-nicknames"));
	}

	public void loadEncryptionSettings(FileConfiguration config) {
		ConfigurationSection encryptionSettings = config.getConfigurationSection("encryption-settings");
		encryption_settings_enable_encryption = encryptionSettings.getBoolean("enable-encryption");
		String encryptionMethod = encryptionSettings.getString("encrypt-method").trim();
		encryption_settings_encrypt_methods = encryptionMethod.contains(";")
				? List.of(encryptionMethod.split(";"))
				: List.of(encryptionMethod);
		encryption_settings_old_encrypt_methods = new ArrayList<>();
		encryption_settings_salt_length = encryptionSettings.getInt("salt-length");
		encryption_settings_auto_encrypt_passwords = encryptionSettings.getBoolean("auto-encrypt-passwords");
		if (!encryption_settings_enable_encryption) {
			return;
		}
		List<String> oldEncryptionMethods = encryptionSettings.getStringList("old-encrypt-methods");
		for (String oldEncryptionMethod : oldEncryptionMethods) {
			if (oldEncryptionMethod.contains(";")) {
				encryption_settings_old_encrypt_methods.add(List.of(oldEncryptionMethod.trim().split(";")));
			} else {
				encryption_settings_old_encrypt_methods.add(List.of(oldEncryptionMethod.trim()));
			}
		}
	}

	public void loadUspMessages(FileConfiguration message) {
		ConfigurationSection uspmsg = message.getConfigurationSection("uspmsg");
		uspmsg_consoleonly = getMessage(uspmsg, "consoleonly");
		uspmsg_playeronly = getMessage(uspmsg, "playeronly");
		uspmsg_logout = getMessage(uspmsg, "logout");
		uspmsg_reloaded = getMessage(uspmsg, "reloaded");
		uspmsg_rebooted = getMessage(uspmsg, "rebooted");
		uspmsg_playernotfound = getMessage(uspmsg, "playernotfound");
		uspmsg_alreadyinconfig = getMessage(uspmsg, "alreadyinconfig");
		uspmsg_notinconfig = getMessage(uspmsg, "notinconfig");
		uspmsg_playeradded = getMessage(uspmsg, "playeradded");
		uspmsg_playerremoved = getMessage(uspmsg, "playerremoved");
		uspmsg_ipadded = getMessage(uspmsg, "ipadded");
		uspmsg_setpassusage = getMessage(uspmsg, "setpassusage");
		uspmsg_addopusage = getMessage(uspmsg, "addopusage");
		uspmsg_addipusage = getMessage(uspmsg, "addipusage");
		uspmsg_rempassusage = getMessage(uspmsg, "rempassusage");
		uspmsg_remopusage = getMessage(uspmsg, "remopusage");
		uspmsg_ipremoved = getMessage(uspmsg, "ipremoved");
		uspmsg_remipusage = getMessage(uspmsg, "remipusage");
		uspmsg_usage = getMessage(uspmsg, "usage");
		uspmsg_usage_logout = getMessage(uspmsg, "usage-logout");
		uspmsg_usage_reload = getMessage(uspmsg, "usage-reload");
		uspmsg_usage_reboot = getMessage(uspmsg, "usage-reboot");
		uspmsg_usage_encrypt = getMessage(uspmsg, "usage-encrypt");
		uspmsg_usage_setpass = getMessage(uspmsg, "usage-setpass");
		uspmsg_usage_rempass = getMessage(uspmsg, "usage-rempass");
		uspmsg_usage_addop = getMessage(uspmsg, "usage-addop");
		uspmsg_usage_remop = getMessage(uspmsg, "usage-remop");
		uspmsg_usage_addip = getMessage(uspmsg, "usage-addip");
		uspmsg_usage_remip = getMessage(uspmsg, "usage-remip");
		uspmsg_otherdisabled = getMessage(uspmsg, "otherdisabled");
	}

	public void loadMsgMessages(FileConfiguration message) {
		ConfigurationSection msg = message.getConfigurationSection("msg");
		msg_message = getMessage(msg, "message");
		msg_incorrect = getMessage(msg, "incorrect");
		msg_correct = getMessage(msg, "correct");
		msg_noneed = getMessage(msg, "noneed");
		msg_cantbenull = getMessage(msg, "cantbenull");
		msg_playeronly = getMessage(msg, "playeronly");
	}

	public void loadBroadcastMessages(FileConfiguration message) {
		ConfigurationSection broadcasts = message.getConfigurationSection("broadcasts");
		broadcasts_failed = getMessage(broadcasts, "failed");
		broadcasts_passed = getMessage(broadcasts, "passed");
		broadcasts_joined = getMessage(broadcasts, "joined");
		broadcasts_captured = getMessage(broadcasts, "captured");
	}

	public void loadTitleMessages(FileConfiguration message) {
		ConfigurationSection titles = message.getConfigurationSection("titles");
		titles_message = getMessage(titles, "message").split(";");
		titles_incorrect = getMessage(titles, "incorrect").split(";");
		titles_correct = getMessage(titles, "correct").split(";");
	}

	public void loadBossbar(FileConfiguration config) {
		ConfigurationSection bossbarSettings = config.getConfigurationSection("bossbar-settings");
		if (!bossbarSettings.getBoolean("enable-bossbar")) {
			return;
		}
		bossbar_settings_enable_bossbar = bossbarSettings.getBoolean("enable-bossbar");
		bossbar_settings_bar_color = bossbarSettings.getString("bar-color");
		bossbar_settings_bar_style = bossbarSettings.getString("bar-style");
		ConfigurationSection bossbar = plugin.messageFile.getConfigurationSection("bossbar");
		bossbar_message = getMessage(bossbar, "message");
	}

	public void loadSecureSettings(FileConfiguration config) {
		ConfigurationSection secureSettings = config.getConfigurationSection("secure-settings");
		secure_settings_enable_op_whitelist = secureSettings.getBoolean("enable-op-whitelist");
		secure_settings_enable_notadmin_punish = secureSettings.getBoolean("enable-notadmin-punish");
		secure_settings_enable_permission_blacklist = secureSettings.getBoolean("enable-permission-blacklist");
		secure_settings_enable_ip_whitelist = secureSettings.getBoolean("enable-ip-whitelist");
		secure_settings_only_console_usp = secureSettings.getBoolean("only-console-usp");
		secure_settings_enable_excluded_players = secureSettings.getBoolean("enable-excluded-players");
		secure_settings_call_event_on_password_enter = secureSettings.getBoolean("call-event-on-password-enter");
	}

	public void loadAdditionalChecks(FileConfiguration config) {
		ConfigurationSection blockingSettings = config.getConfigurationSection("blocking-settings");
		blocking_settings_block_item_drop = blockingSettings.getBoolean("block-item-drop");
		blocking_settings_block_item_pickup = blockingSettings.getBoolean("block-item-pickup");
		blocking_settings_block_tab_complete = blockingSettings.getBoolean("block-tab-complete");
		blocking_settings_block_damage = blockingSettings.getBoolean("block-damage");
		blocking_settings_damaging_entity = blockingSettings.getBoolean("block-damaging-entity");
		blocking_settings_block_inventory_open = blockingSettings.getBoolean("block-inventory-open");
		blocking_settings_hide_on_entering = blockingSettings.getBoolean("hide-on-entering");
		blocking_settings_hide_other_on_entering = blockingSettings.getBoolean("hide-other-on-entering");
		blocking_settings_allow_orientation_change = blockingSettings.getBoolean("allow-orientation-change");
	}

	public void loadPunishSettings(FileConfiguration config) {
		ConfigurationSection punishSettings = config.getConfigurationSection("punish-settings");
		punish_settings_enable_attempts = punishSettings.getBoolean("enable-attempts");
		punish_settings_max_attempts = punishSettings.getInt("max-attempts");
		punish_settings_enable_time = punishSettings.getBoolean("enable-time");
		punish_settings_time = punishSettings.getInt("time");
		punish_settings_enable_rejoin = punishSettings.getBoolean("enable-rejoin");
		punish_settings_max_rejoins = punishSettings.getInt("max-rejoins");
	}

	public void loadSessionSettings(FileConfiguration config) {
		ConfigurationSection sessionSettings = config.getConfigurationSection("session-settings");
		session_settings_session = sessionSettings.getBoolean("session");
		session_settings_session_time_enabled = sessionSettings.getBoolean("session-time-enabled");
		session_settings_session_time = sessionSettings.getInt("session-time");
	}

	public void loadMessageSettings(FileConfiguration config) {
		ConfigurationSection messageSettings = config.getConfigurationSection("message-settings");
		message_settings_send_title = messageSettings.getBoolean("send-titles");
		message_settings_enable_broadcasts = messageSettings.getBoolean("enable-broadcasts");
		message_settings_enable_console_broadcasts = messageSettings.getBoolean("enable-console-broadcasts");
	}

	public void loadSoundSettings(FileConfiguration config) {
		ConfigurationSection soundSettings = config.getConfigurationSection("sound-settings");
		sound_settings_enable_sounds = soundSettings.getBoolean("enable-sounds");
		sound_settings_on_capture = soundSettings.getString("on-capture").split(";");
		sound_settings_on_pas_fail = soundSettings.getString("on-pas-fail").split(";");
		sound_settings_on_pas_correct = soundSettings.getString("on-pas-correct").split(";");
	}

	public void loadEffects(FileConfiguration config) {
		ConfigurationSection effectSettings = config.getConfigurationSection("effect-settings");
		effect_settings_enable_effects = effectSettings.getBoolean("enable-effects");
		effect_settings_effects = effectSettings.getStringList("effects");
	}

	public void loadLoggingSettings(FileConfiguration config) {
		ConfigurationSection logging_settings = config.getConfigurationSection("logging-settings");
		logging_settings_logging_pas = logging_settings.getBoolean("logging-pas");
		logging_settings_logging_join = logging_settings.getBoolean("logging-join");
		logging_settings_logging_enable_disable = logging_settings.getBoolean("logging-enable-disable");
		logging_settings_logging_command_execution = logging_settings.getBoolean("logging-command-execution");
	}

	public void loadPerms(FileConfiguration config) {
		perms = new HashSet<>(config.getStringList("permissions"));
	}

	public void loadLists(FileConfiguration config) {
		allowed_commands = new ArrayList<>(config.getStringList("allowed-commands"));
		ConfigurationSection secureSettings = config.getConfigurationSection("secure-settings");
		if (secureSettings.getBoolean("enable-op-whitelist")) {
			op_whitelist = new ArrayList<>(config.getStringList("op-whitelist"));
		}
		if (secureSettings.getBoolean("enable-permission-blacklist")) {
			blacklisted_perms = new HashSet<>(config.getStringList("blacklisted-perms"));
		}
		if (secureSettings.getBoolean("enable-ip-whitelist")) {
			ip_whitelist = new HashMap<>();
			for (String ipwl_player : config.getConfigurationSection("ip-whitelist").getKeys(false)) {
				List<String> ips = new ArrayList<>(config.getStringList("ip-whitelist." + ipwl_player));
				ip_whitelist.put(ipwl_player, ips);
			}
		}
	}

	public void setupExcluded(FileConfiguration config) {
		if (config.getBoolean("secure-settings.enable-excluded-players")) {
			ConfigurationSection excludedPlayers = config.getConfigurationSection("excluded-players");
			excluded_admin_pass = new ArrayList<>(excludedPlayers.getStringList("admin-pass"));
			excluded_op_whitelist = new ArrayList<>(excludedPlayers.getStringList("op-whitelist"));
			excluded_ip_whitelist = new ArrayList<>(excludedPlayers.getStringList("ip-whitelist"));
			excluded_blacklisted_perms = new ArrayList<>(excludedPlayers.getStringList("blacklisted-perms"));
		}
	}

	public String getMessage(ConfigurationSection section, String key) {
		return Utils.colorize(section.getString(key, "&4&lERROR&r: " + key + " does not exist!").replace("%prefix%", main_settings_prefix), serializer);
	}

	public FileConfiguration getFile(String path, String fileName) {
		File file = new File(path, fileName);
		if (!file.exists()) {
			plugin.saveResource(fileName, false);
		}
		return YamlConfiguration.loadConfiguration(file);
	}

	public void save(String path, FileConfiguration config, String fileName) {
		plugin.getRunner().runAsync(() -> {
			try {
				config.save(new File(path, fileName));
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		});
	}
}