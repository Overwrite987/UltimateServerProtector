package ru.overwrite.protect.bukkit.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.api.CaptureReason;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.api.ServerProtectorCaptureEvent;
import ru.overwrite.protect.bukkit.task.Runner;
import ru.overwrite.protect.bukkit.utils.Config;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionListener implements Listener {

	private final ServerProtectorManager plugin;
	private final ServerProtectorAPI api;
	private final Config pluginConfig;

	private final Runner runner;

	public ConnectionListener(ServerProtectorManager plugin) {
		this.plugin = plugin;
		api = plugin.getPluginAPI();
		pluginConfig = plugin.getPluginConfig();
		runner = plugin.getRunner();
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onLogin(PlayerLoginEvent e) {
		Player p = e.getPlayer();
		runner.runAsync(() -> {
			CaptureReason captureReason = plugin.checkPermissions(p);
			if (api.isCaptured(p) && captureReason == null) {
				api.uncapturePlayer(p);
				return;
			}
			if (captureReason != null) {
				String playerName = p.getName();
				String ip = e.getAddress().getHostAddress();
				if (pluginConfig.secure_settings_enable_ip_whitelist) {
					if (!isIPAllowed(playerName, ip)) {
						if (!plugin.isExcluded(p, pluginConfig.excluded_ip_whitelist)) {
							plugin.checkFail(playerName, pluginConfig.commands_not_admin_ip);
						}
					}
				}
				if (pluginConfig.session_settings_session && !api.hasSession(p, ip)) {
					if (!plugin.isExcluded(p, pluginConfig.excluded_admin_pass)) {
						ServerProtectorCaptureEvent captureEvent = new ServerProtectorCaptureEvent(p, ip, captureReason);
						captureEvent.callEvent();
						if (captureEvent.isCancelled()) {
							return;
						}
						api.capturePlayer(p);
					}
				}
			}
		});
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		runner.runAsync(() -> {
			CaptureReason captureReason = plugin.checkPermissions(p);
			if (captureReason != null) {
				if (api.isCaptured(p)) {
					if (pluginConfig.effect_settings_enable_effects) {
						plugin.giveEffect(p);
					}
					plugin.applyHide(p);
				}
				if (pluginConfig.logging_settings_logging_join) {
					plugin.logAction("log-format.joined", p, new Date());
				}
				plugin.sendAlert(p, pluginConfig.broadcasts_joined);
			}
		});
	}

	private boolean isIPAllowed(String p, String ip) {
		List<String> ips = pluginConfig.ip_whitelist.get(p);
		if (ips == null || ips.isEmpty()) {
			return false;
		}
		String[] ipParts = ip.split("\\.");

		return ips.stream().anyMatch(allowedIP -> {
			String[] allowedParts = allowedIP.split("\\.");
			if (ipParts.length != allowedParts.length) {
				return false;
			}

			for (int i = 0; i < ipParts.length; i++) {
				if (!allowedParts[i].equals("*") && !allowedParts[i].equals(ipParts[i])) {
					return false;
				}
			}
			return true;
		});
	}

	public final Map<String, Integer> rejoins = new HashMap<>();

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLeave(PlayerQuitEvent event) {
		Player p = event.getPlayer();
		handlePlayerLeave(p);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onKick(PlayerKickEvent event) {
		Player p = event.getPlayer();
		handlePlayerLeave(p);
	}

	private void handlePlayerLeave(Player p) {
		String playerName = p.getName();
		if (api.isCaptured(p)) {
			for (PotionEffect s : p.getActivePotionEffects()) {
				p.removePotionEffect(s.getType());
			}
			if (pluginConfig.punish_settings_enable_rejoin) {
				rejoins.put(playerName, rejoins.getOrDefault(playerName, 0) + 1);
				if (isMaxRejoins(playerName)) {
					rejoins.remove(playerName);
					plugin.checkFail(p.getName(), pluginConfig.commands_failed_rejoin);
				}
			}
		}
		plugin.time.remove(playerName);
		api.saved.remove(playerName);
	}

	private boolean isMaxRejoins(String playerName) {
		if (!rejoins.containsKey(playerName))
			return false;
		return rejoins.get(playerName) > pluginConfig.punish_settings_max_rejoins;
	}
}