package ru.overwrite.protect.bukkit.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import ru.overwrite.protect.bukkit.ServerProtectorManager;

public class Config {
	
	private final ServerProtectorManager instance;
	
	public Config(ServerProtectorManager plugin) {
        this.instance = plugin;
    }
	
	public Set<String> perms;
	
	public List<String> 
	allowed_commands, 
	op_whitelist, 
	blacklisted_perms, 
	excluded_players, 
	ip_whitelist;
	
	public String 
	uspmsg_consoleonly,
	uspmsg_reloaded,
	uspmsg_rebooted,
	uspmsg_playernotfound,
	uspmsg_alreadyinconfig,
	uspmsg_notinconfig,
	uspmsg_playeradded, 
	uspmsg_playerremoved,
	uspmsg_ipadded,
	uspmsg_setpassusage,
	uspmsg_addopusage, 
	uspmsg_remopusage, 
	uspmsg_ipremoved,
	uspmsg_remipusage,
	uspmsg_addipusage,
	uspmsg_rempassusage,
	uspmsg_usage,
	uspmsg_usage_reload,
	uspmsg_usage_reboot,
	uspmsg_usage_setpass,
	uspmsg_usage_rempass,
	uspmsg_usage_addop,
	uspmsg_usage_remop,
	uspmsg_usage_addip,
	uspmsg_usage_remip,
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
	
	public boolean 
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
	
	public int
	punish_settings_max_attempts,
	punish_settings_time,
	session_settings_session_time;
	
	public List<String>
	effect_settings_effects;
	
	public float
	sound_settings_volume,
	sound_settings_pitch;
	
	public void loadUspMessages() {
		uspmsg_consoleonly = instance.getMessage("uspmsg.consoleonly");
		uspmsg_reloaded = instance.getMessage("uspmsg.reloaded");
		uspmsg_rebooted = instance.getMessage("uspmsg.rebooted");
		uspmsg_playernotfound = instance.getMessage("uspmsg.playernotfound");
		uspmsg_alreadyinconfig = instance.getMessage("uspmsg.alreadyinconfig");
		uspmsg_notinconfig = instance.getMessage("uspmsg.notinconfig");
		uspmsg_playeradded = instance.getMessage("uspmsg.playeradded");
		uspmsg_playerremoved = instance.getMessage("uspmsg.playerremoved");
		uspmsg_ipadded = instance.getMessage("uspmsg.ipadded");
		uspmsg_setpassusage = instance.getMessage("uspmsg.setpassusage");
		uspmsg_addopusage = instance.getMessage("uspmsg.addopusage");
		uspmsg_addipusage = instance.getMessage("uspmsg.addipusage");
		uspmsg_rempassusage = instance.getMessage("uspmsg.rempassusage");
		uspmsg_remopusage = instance.getMessage("uspmsg.remopusage");
		uspmsg_ipremoved = instance.getMessage("uspmsg.ipremoved");
		uspmsg_remipusage = instance.getMessage("uspmsg.remipusage");
		uspmsg_usage = instance.getMessage("uspmsg.usage");
		uspmsg_usage_reload = instance.getMessage("uspmsg.usage-reload");
		uspmsg_usage_reboot = instance.getMessage("uspmsg.usage-reboot");
		uspmsg_usage_setpass = instance.getMessage("uspmsg.usage-setpass");
		uspmsg_usage_rempass = instance.getMessage("uspmsg.usage-rempass");
		uspmsg_usage_addop = instance.getMessage("uspmsg.usage-addop");
		uspmsg_usage_remop = instance.getMessage("uspmsg.usage-remop");
		uspmsg_usage_addip = instance.getMessage("uspmsg.usage-addip");
		uspmsg_usage_remip = instance.getMessage("uspmsg.usage-remip");
	}
	
	public void loadMsgMessages() {
		msg_message = instance.getMessage("msg.message");
		msg_incorrect = instance.getMessage("msg.incorrect");
		msg_correct = instance.getMessage("msg.correct");
		msg_noneed = instance.getMessage("msg.noneed");
		msg_cantbenull = instance.getMessage("msg.cantbenull");
		msg_playeronly = instance.getMessage("msg.playeronly");	
	}
	
	public void loadBroadcastMessages() {
		broadcasts_failed = instance.getMessage("broadcasts.failed");
		broadcasts_passed = instance.getMessage("broadcasts.passed");
		broadcasts_joined = instance.getMessage("broadcasts.joined");
		broadcasts_captured = instance.getMessage("broadcasts.captured");
	}
	
	public void loadTitleMessages() {
		titles_title = instance.getMessage("titles.title");
		titles_subtitle = instance.getMessage("titles.subtitle");
	}
	
	public void loadBossbar(FileConfiguration config) {
		bossbar_settings_enable_bossbar = config.getBoolean("bossbar-settings.enable-bossbar");
		bossbar_settings_bar_color = config.getString("bossbar-settings.bar-color");
		bossbar_settings_bar_style = config.getString("bossbar-settings.bar-style");
		bossbar_message = instance.getMessage("bossbar.message");
	}
	
	public void loadMainSettings(FileConfiguration config) {
		main_settings_prefix = Utils.colorize(config.getString("main-settings.prefix"));
		main_settings_pas_command = config.getString("main-settings.pas-command");
		main_settings_use_command = config.getBoolean("main-settings.use-command");
		main_settings_enable_admin_commands = config.getBoolean("main-settings.enable-admin-commands");
	}
	
	public void loadSecureSettings(FileConfiguration config) {
		secure_settings_enable_op_whitelist = config.getBoolean("secure-settings.enable-op-whitelist");
		secure_settings_enable_notadmin_punish = config.getBoolean("secure-settings.enable-notadmin-punish");
		secure_settings_enable_permission_blacklist = config.getBoolean("secure-settings.enable-permission-blacklist");
		secure_settings_enable_ip_whitelist = config.getBoolean("secure-settings.enable-ip-whitelist");
		secure_settings_only_console_usp = config.getBoolean("secure-settings.only-console-usp");
		secure_settings_enable_excluded_players = config.getBoolean("secure-settings.enable-excluded-players");
	}
	
	public void loadAdditionalChecks(FileConfiguration config) {
		blocking_settings_block_item_drop = config.getBoolean("blocking-settings.block-item-drop");
		blocking_settings_block_item_pickup = config.getBoolean("blocking-settings.block-item-pickup");
		blocking_settings_block_tab_complete = config.getBoolean("blocking-settings.block-tab-complete");
		blocking_settings_block_damage = config.getBoolean("blocking-settings.block-damage");
		blocking_settings_damaging_entity = config.getBoolean("blocking-settings.block-damaging-entity");
		blocking_settings_mobs_targeting = config.getBoolean("blocking-settings.block-mobs-targeting");
	}
	
	public void loadAttempts(FileConfiguration config) {
		punish_settings_enable_attempts = config.getBoolean("punish-settings.enable-attempts");
		punish_settings_max_attempts = config.getInt("punish-settings.max-attempts");
	}
	
	public void loadTime(FileConfiguration config) {
		punish_settings_enable_time = config.getBoolean("punish-settings.enable-time");
		punish_settings_time = config.getInt("punish-settings.time");
	}
	
	public void loadSessionSettings(FileConfiguration config) {
		session_settings_session = config.getBoolean("session-settings.session");
		session_settings_session_time_enabled = config.getBoolean("session-settings.session-time-enabled");
		session_settings_session_time = config.getInt("session-settings.session-time");
	}
	
	public void loadMessageSettings(FileConfiguration config) {
		message_settings_send_title = config.getBoolean("message-settings.send-titles");
		message_settings_enable_broadcasts = config.getBoolean("message-settings.enable-broadcasts");
		message_settings_enable_console_broadcasts= config.getBoolean("message-settings.enable-console-broadcasts");
	}
	
	public void loadSoundSettings(FileConfiguration config) {
		sound_settings_enable_sounds = config.getBoolean("sound-settings.enable-sounds");
		sound_settings_on_capture = config.getString("sound-settings.on-capture");
		sound_settings_on_pas_fail = config.getString("sound-settings.on-pas-fail");
		sound_settings_on_pas_correct = config.getString("sound-settings.on-pas-correct");
		sound_settings_volume = (float)config.getDouble("sound-settings.volume");
		sound_settings_pitch = (float)config.getDouble("sound-settings.pitch");
	}
	
	public void loadEffects(FileConfiguration config) {
		effect_settings_enable_effects = config.getBoolean("effect-settings.enable-effects");
		effect_settings_effects = config.getStringList("effect-settings.effects");
	}
	
	public void loadLoggingSettings(FileConfiguration config) {
		logging_settings_logging_pas = config.getBoolean("logging-settings.logging-pas");
		logging_settings_logging_join = config.getBoolean("logging-settings.logging-join");
		logging_settings_logging_enable_disable = config.getBoolean("logging-settings.logging-enable-disable");
	}
	
	public void loadPerms(FileConfiguration config) {
		perms = new HashSet<>(config.getStringList("permissions"));
	}
	
	public void loadLists(FileConfiguration config) {
		allowed_commands = new ArrayList<>(config.getStringList("allowed-commands"));
		if (config.getBoolean("secure-settings.enable-op-whitelist")) {
			op_whitelist = new ArrayList<>(config.getStringList("op-whitelist"));
		}
		if (config.getBoolean("secure-settings.enable-permission-blacklist")) {
			blacklisted_perms = new ArrayList<>(config.getStringList("blacklisted-perms"));
		}
		if (config.getBoolean("secure-settings.enable-excluded-players")) {
			excluded_players = new ArrayList<>(config.getStringList("excluded-players"));
		}
		if (config.getBoolean("secure-settings.enable-ip-whitelist")) {
			ip_whitelist = new ArrayList<>(config.getStringList("ip-whitelist"));
		}
	}

	public FileConfiguration getFile(String fileName) {
	    File file = new File(instance.getDataFolder(), fileName);
	    if (!file.exists()) {
	    	instance.saveResource(fileName, false);
	    }
	    return YamlConfiguration.loadConfiguration(file);
	}
    
    public FileConfiguration getFileFullPath(String fileName) {
        File file = new File(instance.getConfig().getString("file-settings.data-file-path"), fileName);
        if (!file.exists()) {
        	instance.saveResource(fileName, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public void save(FileConfiguration config, String fileName) {
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            try {
                config.save(new File(instance.getDataFolder(), fileName));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
    	});
    }
    
    public void saveFullPath(FileConfiguration config, String fileName) {
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            try {
            	config.save(new File(instance.getConfig().getString("file-settings.data-file-path"), fileName));
            } catch (IOException ex) {
            	ex.printStackTrace();
            }
        });
    }
}
