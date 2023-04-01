package ru.overwrite.protect.bukkit.utils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.overwrite.protect.bukkit.ServerProtector;
import ru.overwrite.protect.bukkit.ServerProtectorManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Config {
	
	private static final ServerProtector instance = ServerProtector.getInstance();
	
	public static String 
	uspmsg_consoleonly,
	uspmsg_reloaded,
	uspmsg_rebooted,
	uspmsg_playernotfound,
	uspmsg_alreadyinconfig,
	uspmsg_playeradded,
	uspmsg_ipadded,
	uspmsg_setpassusage,
	uspmsg_addopusage,
	uspmsg_addipusage,
	uspmsg_usage,
	uspmsg_usage_reload,
	uspmsg_usage_reboot,
	uspmsg_usage_setpass,
	uspmsg_usage_addop,
	uspmsg_usage_addip,
	msg_message,
	msg_incorrect,
	msg_correct,
	msg_noneed,
	msg_cantbenull,
	msg_playeronly,
	broadcasts_failed,
	broadcasts_passed,
	broadcasts_joined,
	broadcasts_captured,
	titles_title,
	titles_subtitle,
	bossbar_message,
	bossbar_settings_bar_color,
	bossbar_settings_bar_style,
	main_settings_prefix,
	main_settings_pas_command,
	sound_settings_on_capture,
	sound_settings_on_pas_fail,
	sound_settings_on_pas_correct;
	
	public static boolean 
	blocking_settings_block_item_drop,
	blocking_settings_block_item_pickup,
	blocking_settings_block_tab_complete,
	blocking_settings_block_damage,
	blocking_settings_damaging_entity,
	blocking_settings_mobs_targeting,
	main_settings_use_command,
	main_settings_enable_admin_commands,
	punish_settings_enable_attempts,
	punish_settings_enable_time,
	bossbar_settings_enable_bossbar,
	secure_settings_enable_op_whitelist,
	secure_settings_enable_notadmin_punish,
	secure_settings_enable_permission_blacklist,
	secure_settings_enable_ip_whitelist,
	secure_settings_only_console_usp,
	secure_settings_enable_excluded_players,
	session_settings_session,
	session_settings_session_time_enabled,
	message_settings_send_title,
	message_settings_enable_broadcasts,
	message_settings_enable_console_broadcasts,
	sound_settings_enable_sounds,
	effect_settings_enable_effects,
	logging_settings_logging_pas,
	logging_settings_logging_join,
	logging_settings_logging_enable_disable;
	
	public static int
	punish_settings_max_attempts,
	punish_settings_time,
	session_settings_session_time;
	
	public static List<String>
	effect_settings_effects;
	
	public static float
	sound_settings_volume,
	sound_settings_pitch;
	
	public static void loadUspMessages() {
		uspmsg_consoleonly = ServerProtector.getMessage("uspmsg.consoleonly");
		uspmsg_reloaded = ServerProtector.getMessage("uspmsg.reloaded");
		uspmsg_rebooted = ServerProtector.getMessage("uspmsg.rebooted");
		uspmsg_playernotfound = ServerProtector.getMessage("uspmsg.playernotfound");
		uspmsg_alreadyinconfig = ServerProtector.getMessage("uspmsg.alreadyinconfig");
		uspmsg_playeradded = ServerProtector.getMessage("uspmsg.playeradded");
		uspmsg_ipadded = ServerProtector.getMessage("uspmsg.ipadded");
		uspmsg_setpassusage = ServerProtector.getMessage("uspmsg.setpassusage");
		uspmsg_addopusage = ServerProtector.getMessage("uspmsg.addopusage");
		uspmsg_addipusage = ServerProtector.getMessage("uspmsg.addipusage");
		uspmsg_usage = ServerProtector.getMessage("uspmsg.usage");
		uspmsg_usage_reload = ServerProtector.getMessage("uspmsg.usage-reload");
		uspmsg_usage_reboot = ServerProtector.getMessage("uspmsg.usage-reboot");
		uspmsg_usage_setpass = ServerProtector.getMessage("uspmsg.usage-setpass");
		uspmsg_usage_addop = ServerProtector.getMessage("uspmsg.usage-addop");
		uspmsg_usage_addip = ServerProtector.getMessage("uspmsg.usage-addip");
	}
	
	public static void loadMsgMessages() {
		msg_message = ServerProtectorManager.getMessage("msg.message");
		msg_incorrect = ServerProtectorManager.getMessage("msg.incorrect");
		msg_correct = ServerProtectorManager.getMessage("msg.correct");
		msg_noneed = ServerProtector.getMessage("msg.noneed");
		msg_cantbenull = ServerProtector.getMessage("msg.cantbenull");
		msg_playeronly = ServerProtector.getMessage("msg.playeronly");	
	}
	
	public static void loadBroadcastMessages() {
		broadcasts_failed = ServerProtector.getMessage("broadcasts.failed");
		broadcasts_passed = ServerProtector.getMessage("broadcasts.passed");
		broadcasts_joined = ServerProtector.getMessage("broadcasts.joined");
		broadcasts_captured = ServerProtector.getMessage("broadcasts.captured");
	}
	
	public static void loadTitleMessages() {
		titles_title = ServerProtector.getMessage("titles.title");
		titles_subtitle = ServerProtector.getMessage("titles.subtitle");
	}
	
	public static void loadBossbar(FileConfiguration config) {
		bossbar_settings_enable_bossbar = config.getBoolean("bossbar-settings.enable-bossbar");
		bossbar_settings_bar_color = config.getString("bossbar-settings.bar-color");
		bossbar_settings_bar_style = config.getString("bossbar-settings.bar-style");
		bossbar_message = ServerProtector.getMessage("bossbar.message");
	}
	
	public static void loadMainSettings(FileConfiguration config) {
		main_settings_prefix = Utils.colorize(config.getString("main-settings.prefix"));
		main_settings_pas_command = config.getString("main-settings.pas-command");
		main_settings_use_command = config.getBoolean("main-settings.use-command");
		main_settings_enable_admin_commands = config.getBoolean("main-settings.enable-admin-commands");
	}
	
	public static void loadSecureSettings(FileConfiguration config) {
		secure_settings_enable_op_whitelist = config.getBoolean("secure-settings.enable-op-whitelist");
		secure_settings_enable_notadmin_punish = config.getBoolean("secure-settings.enable-notadmin-punish");
		secure_settings_enable_permission_blacklist = config.getBoolean("secure-settings.enable-permission-blacklist");
		secure_settings_enable_ip_whitelist = config.getBoolean("secure-settings.enable-ip-whitelist");
		secure_settings_only_console_usp = config.getBoolean("secure-settings.only-console-usp");
		secure_settings_enable_excluded_players = config.getBoolean("secure-settings.enable-excluded-players");
	}
	
	public static void loadAdditionalChecks(FileConfiguration config) {
		blocking_settings_block_item_drop = config.getBoolean("blocking-settings.block-item-drop");
		blocking_settings_block_item_pickup = config.getBoolean("blocking-settings.block-item-pickup");
		blocking_settings_block_tab_complete = config.getBoolean("blocking-settings.block-tab-complete");
		blocking_settings_block_damage = config.getBoolean("blocking-settings.block-damage");
		blocking_settings_damaging_entity = config.getBoolean("blocking-settings.block-damaging-entity");
		blocking_settings_mobs_targeting = config.getBoolean("blocking-settings.block-mobs-targeting");
	}
	
	public static void loadAttempts(FileConfiguration config) {
		punish_settings_enable_attempts = config.getBoolean("punish-settings.enable-attempts");
		punish_settings_max_attempts = config.getInt("punish-settings.max-attempts");
	}
	
	public static void loadTime(FileConfiguration config) {
		punish_settings_enable_time = config.getBoolean("punish-settings.enable-time");
		punish_settings_time = config.getInt("punish-settings.time");
	}
	
	public static void loadSessionSettings(FileConfiguration config) {
		session_settings_session = config.getBoolean("session-settings.session");
		session_settings_session_time_enabled = config.getBoolean("session-settings.session-time-enabled");
		session_settings_session_time = config.getInt("session-settings.session-time");
	}
	
	public static void loadMessageSettings(FileConfiguration config) {
		message_settings_send_title = config.getBoolean("message-settings.send-titles");
		message_settings_enable_broadcasts = config.getBoolean("message-settings.enable-broadcasts");
		message_settings_enable_console_broadcasts= config.getBoolean("message-settings.enable-console-broadcasts");
	}
	
	public static void loadSoundSettings(FileConfiguration config) {
		sound_settings_enable_sounds = config.getBoolean("sound-settings.enable-sounds");
		sound_settings_on_capture = config.getString("sound-settings.on-capture");
		sound_settings_on_pas_fail = config.getString("sound-settings.on-pas-fail");
		sound_settings_on_pas_correct = config.getString("sound-settings.on-pas-correct");
		sound_settings_volume = (float)config.getDouble("sound-settings.volume");
		sound_settings_pitch = (float)config.getDouble("sound-settings.pitch");
	}
	
	public static void loadEffects(FileConfiguration config) {
		effect_settings_enable_effects = config.getBoolean("effect-settings.enable-effects");
		effect_settings_effects = config.getStringList("effect-settings.effects");
	}
	
	public static void loadLoggingSettings(FileConfiguration config) {
		logging_settings_logging_pas = config.getBoolean("logging-settings.logging-pas");
		logging_settings_logging_join = config.getBoolean("logging-settings.logging-join");
		logging_settings_logging_enable_disable = config.getBoolean("logging-settings.logging-enable-disable");
	}

	public static FileConfiguration getFile(String fileName) {
	    File file = new File(instance.getDataFolder(), fileName);
	    if (!file.exists()) {
	    	instance.saveResource(fileName, false);
	    }
	    return YamlConfiguration.loadConfiguration(file);
	}
    
    public static FileConfiguration getFileFullPath(String fileName) {
        File file = new File(instance.getConfig().getString("file-settings.data-file-path"), fileName);
        if (!file.exists()) {
        	instance.saveResource(fileName, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public static void save(FileConfiguration config, String fileName) {
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            try {
                config.save(new File(instance.getDataFolder(), fileName));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
    	});
    }
    
    public static void saveFullPath(FileConfiguration config, String fileName) {
        Bukkit.getScheduler().runTaskAsynchronously(ServerProtector.getInstance(), () -> {
            try {
            	config.save(new File(instance.getConfig().getString("file-settings.data-file-path"), fileName));
            } catch (IOException ex) {
            	ex.printStackTrace();
            }
        });
    }
}
