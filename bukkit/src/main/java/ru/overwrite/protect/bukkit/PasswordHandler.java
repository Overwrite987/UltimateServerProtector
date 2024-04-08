package ru.overwrite.protect.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.api.ServerProtectorPasswordEnterEvent;
import ru.overwrite.protect.bukkit.api.ServerProtectorPasswordFailEvent;
import ru.overwrite.protect.bukkit.api.ServerProtectorPasswordSuccessEvent;
import ru.overwrite.protect.bukkit.utils.Config;
import ru.overwrite.protect.bukkit.utils.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PasswordHandler {

	private final ServerProtectorManager plugin;
	private final ServerProtectorAPI api;
	private final Config pluginConfig;
	public final Map<String, Integer> attempts = new HashMap<>();

	public PasswordHandler(ServerProtectorManager plugin) {
		this.plugin = plugin;
		pluginConfig = plugin.getPluginConfig();
		api = plugin.getPluginAPI();
	}

	public void checkPassword(Player p, String input, boolean resync) {
		Runnable run = () -> {
			ServerProtectorPasswordEnterEvent enterEvent = new ServerProtectorPasswordEnterEvent(p, input);
			if (pluginConfig.secure_settings_call_event_on_password_enter) { enterEvent.callEvent(); }
			if (enterEvent.isCancelled()) {
				return;
			}
			if (pluginConfig.per_player_passwords.get(p.getName()) == null) {
				failedPassword(p);
				return;
			}
			String playerPass = pluginConfig.per_player_passwords.get(p.getName());
			String salt = playerPass.split(":")[0];
			String pass = pluginConfig.encryption_settings_enable_encryption
					? Utils.encryptPassword(input, salt, pluginConfig.encryption_settings_encrypt_methods)
					: input;
			if (pass.equals(playerPass)) {
				correctPassword(p);
				return;
			}
			if (!pluginConfig.encryption_settings_old_encrypt_methods.isEmpty()) {
				for (List<String> oldEncryptMethod : pluginConfig.encryption_settings_old_encrypt_methods) {
					String oldgenPass = pluginConfig.encryption_settings_enable_encryption
							? Utils.encryptPassword(input, salt, oldEncryptMethod)
							: input;
					if (oldgenPass.equals(pluginConfig.per_player_passwords.get(p.getName()))) {
						correctPassword(p);
						return;
					}
				}
			}
			failedPassword(p);
			if (pluginConfig.punish_settings_enable_attempts && !isAttemptsMax(p)) {
				plugin.checkFail(p.getName(), plugin.getConfig().getStringList("commands.failed-pass"));
			}
		};
		if (resync) {
			plugin.getRunner().runPlayer(run, p);
		} else {
			run.run();
		}
	}

	private boolean isAttemptsMax(Player p) {
		if (!attempts.containsKey(p))
			return false;
		return !(attempts.get(p) < pluginConfig.punish_settings_max_attempts);
	}

	private void failedPassword(Player p) {
		if (pluginConfig.punish_settings_enable_attempts) {
			attempts.put(p.getName(), attempts.getOrDefault(p, 0) + 1);
		}
		ServerProtectorPasswordFailEvent failEvent = new ServerProtectorPasswordFailEvent(p, attempts.get(p));
		failEvent.callEvent();
		if (failEvent.isCancelled()) {
			return;
		}
		p.sendMessage(pluginConfig.msg_incorrect);
		if (pluginConfig.message_settings_send_title) {
			Utils.sendTitleMessage(pluginConfig.titles_incorrect, p);
		}
		if (pluginConfig.sound_settings_enable_sounds) {
			Utils.sendSound(pluginConfig.sound_settings_on_pas_fail, p);
		}
		if (pluginConfig.logging_settings_logging_pas) {
			plugin.logAction("log-format.failed", p, new Date());
		}
		if (pluginConfig.message_settings_enable_broadcasts) {
			String msg = pluginConfig.broadcasts_failed.replace("%player%", p.getName()).replace("%ip%",
					Utils.getIp(p));
			plugin.sendAlert(p, msg);
		}
		if (pluginConfig.message_settings_enable_console_broadcasts) {
			String msg = pluginConfig.broadcasts_failed.replace("%player%", p.getName()).replace("%ip%",
					Utils.getIp(p));
			Bukkit.getConsoleSender().sendMessage(msg);
		}
	}

	private void correctPassword(Player p) {
		ServerProtectorPasswordSuccessEvent successEvent = new ServerProtectorPasswordSuccessEvent(p);
		successEvent.callEvent();
		if (successEvent.isCancelled()) {
			return;
		}
		api.uncapturePlayer(p);
		p.sendMessage(pluginConfig.msg_correct);
		if (pluginConfig.message_settings_send_title) {
			Utils.sendTitleMessage(pluginConfig.titles_correct, p);
		}
		String playerName = p.getName();
		plugin.time.remove(playerName);
		if (pluginConfig.sound_settings_enable_sounds) {
			Utils.sendSound(pluginConfig.sound_settings_on_pas_correct, p);
		}
		if (pluginConfig.effect_settings_enable_effects) {
			for (PotionEffect s : p.getActivePotionEffects()) {
				p.removePotionEffect(s.getType());
			}
		}
		if (pluginConfig.blocking_settings_hide_on_entering) {
			for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
				if (!onlinePlayer.equals(p)) {
					onlinePlayer.showPlayer(plugin, p);
				}
			}
		}
		if (pluginConfig.blocking_settings_hide_other_on_entering) {
			for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
				p.showPlayer(plugin, onlinePlayer);
			}
		}
		api.authorisePlayer(p);
		if (pluginConfig.session_settings_session_time_enabled) {
			plugin.getRunner().runDelayedAsync(() -> {
				if (!plugin.login.contains(playerName)) {
					api.deauthorisePlayer(p);
				}
			}, pluginConfig.session_settings_session_time * 20L);
		}
		if (pluginConfig.logging_settings_logging_pas) {
			plugin.logAction("log-format.passed", p, new Date());
		}
		if (pluginConfig.bossbar_settings_enable_bossbar) {
			if (Utils.bossbar == null) {
				return;
			}
			if (Utils.bossbar.getPlayers().contains(p)) {
				Utils.bossbar.removePlayer(p);
			}
		}
		if (pluginConfig.message_settings_enable_broadcasts) {
			String msg = pluginConfig.broadcasts_passed.replace("%player%", playerName).replace("%ip%",
					Utils.getIp(p));
			plugin.sendAlert(p, msg);
		}
		if (pluginConfig.message_settings_enable_console_broadcasts) {
			String msg = pluginConfig.broadcasts_passed.replace("%player%", playerName).replace("%ip%",
					Utils.getIp(p));
			Bukkit.getConsoleSender().sendMessage(msg);
		}
	}
}