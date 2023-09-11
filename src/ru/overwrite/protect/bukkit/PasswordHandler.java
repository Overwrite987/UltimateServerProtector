package ru.overwrite.protect.bukkit;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import ru.overwrite.protect.bukkit.api.ServerProtectorPasswordSuccessEvent;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.api.ServerProtectorPasswordEnterEvent;
import ru.overwrite.protect.bukkit.api.ServerProtectorPasswordFailEvent;
import ru.overwrite.protect.bukkit.utils.Config;
import ru.overwrite.protect.bukkit.utils.Utils;

public class PasswordHandler {

	private final ServerProtectorManager instance;
	private final ServerProtectorAPI api;
	private final Config pluginConfig;
	public final Map<Player, Integer> attempts;

	public PasswordHandler(ServerProtectorManager plugin) {
		instance = plugin;
		pluginConfig = plugin.getPluginConfig();
		api = plugin.getPluginAPI();
		attempts = new HashMap<>();
	}

	public void checkPassword(Player player, String input, boolean resync) {
		Runnable run = () -> {
			ServerProtectorPasswordEnterEvent enterEvent = new ServerProtectorPasswordEnterEvent(player, input);
			enterEvent.callEvent();
			if (enterEvent.isCancelled()) {
				return;
			}
			FileConfiguration data = instance.data;
			if (input.equals(data.getString("data." + player.getName() + ".pass"))) {
				correctPassword(player);
			} else {
				player.sendMessage(pluginConfig.msg_incorrect);
				failedPassword(player);
				if (!isAttemptsMax(player) && pluginConfig.punish_settings_enable_attempts) {
					failedPasswordCommands(player);
				}
			}
		};
		if (resync) {
			instance.runSyncTask(run);
			return;
		} else {
			run.run();
		}
	}

	private boolean isAttemptsMax(Player player) {
		if (!attempts.containsKey(player))
			return true;
		return (attempts.get(player) < pluginConfig.punish_settings_max_attempts);
	}

	private void failedPasswordCommands(Player player) {
		for (String command : instance.getConfig().getStringList("commands.failed-pass")) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
		}
	}

	private void failedPassword(Player player) {
		if (pluginConfig.punish_settings_enable_attempts) {
			attempts.put(player, attempts.getOrDefault(player, 0) + 1);
		}
		ServerProtectorPasswordFailEvent failEvent = new ServerProtectorPasswordFailEvent(player, attempts.get(player));
		failEvent.callEvent();
		if (failEvent.isCancelled()) {
			return;
		}
		if (pluginConfig.sound_settings_enable_sounds) {
			player.playSound(player.getLocation(), Sound.valueOf(pluginConfig.sound_settings_on_pas_fail),
					pluginConfig.sound_settings_volume, pluginConfig.sound_settings_pitch);
		}
		if (pluginConfig.logging_settings_logging_pas) {
			instance.logAction("log-format.failed", player, new Date());
		}
		String msg = pluginConfig.broadcasts_failed.replace("%player%", player.getName()).replace("%ip%",
				Utils.getIp(player));
		if (pluginConfig.message_settings_enable_broadcasts) {
			instance.sendAlert(player, msg);
		}
		if (pluginConfig.message_settings_enable_console_broadcasts) {
			Bukkit.getConsoleSender().sendMessage(msg);
		}
	}

	private void correctPassword(Player player) {
		ServerProtectorPasswordSuccessEvent successEvent = new ServerProtectorPasswordSuccessEvent(player);
		successEvent.callEvent();
		if (successEvent.isCancelled()) {
			return;
		}
		api.uncapturePlayer(player);
		String playerName = player.getName();
		player.sendMessage(pluginConfig.msg_correct);
		instance.time.remove(playerName);
		if (pluginConfig.sound_settings_enable_sounds) {
			player.playSound(player.getLocation(), Sound.valueOf(pluginConfig.sound_settings_on_pas_correct),
					pluginConfig.sound_settings_volume, pluginConfig.sound_settings_pitch);
		}
		if (pluginConfig.effect_settings_enable_effects) {
			for (PotionEffect s : player.getActivePotionEffects()) {
				player.removePotionEffect(s.getType());
			}
		}
		if (pluginConfig.session_settings_session) {
			instance.ips.add(playerName + Utils.getIp(player));
		} else {
			instance.saved.add(playerName);
		}
		if (pluginConfig.session_settings_session_time_enabled) {
			Runnable run = () -> {
				if (!instance.login.contains(playerName)) {
					instance.ips.remove(playerName + Utils.getIp(player));
				}
			};
			instance.runAsyncDelayedTask(run);
		}
		if (pluginConfig.logging_settings_logging_pas) {
			instance.logAction("log-format.passed", player, new Date());
		}
		if (pluginConfig.bossbar_settings_enable_bossbar) {
			if (Utils.bossbar == null) {
				return;
			}
			if (Utils.bossbar.getPlayers().contains(player)) {
				Utils.bossbar.removePlayer(player);
			}
		}
		String msg = pluginConfig.broadcasts_passed.replace("%player%", playerName).replace("%ip%",
				Utils.getIp(player));
		if (pluginConfig.message_settings_enable_broadcasts) {
			instance.sendAlert(player, msg);
		}
		if (pluginConfig.message_settings_enable_console_broadcasts) {
			Bukkit.getConsoleSender().sendMessage(msg);
		}
	}
}