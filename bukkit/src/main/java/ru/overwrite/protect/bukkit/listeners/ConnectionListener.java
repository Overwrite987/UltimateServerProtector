package ru.overwrite.protect.bukkit.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.api.ServerProtectorCaptureEvent;
import ru.overwrite.protect.bukkit.task.Runner;
import ru.overwrite.protect.bukkit.utils.Config;
import ru.overwrite.protect.bukkit.utils.Utils;

import java.util.Date;
import java.util.List;

public class ConnectionListener implements Listener {

	private final ServerProtectorManager instance;
	private final ServerProtectorAPI api;
	private final Config pluginConfig;

	private final Runner runner;

	public ConnectionListener(ServerProtectorManager plugin) {
		instance = plugin;
		api = plugin.getPluginAPI();
		pluginConfig = plugin.getPluginConfig();
		runner = plugin.getRunner();
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onLogin(PlayerLoginEvent e) {
		Player p = e.getPlayer();
		instance.getRunner().runAsync (() -> {
			if (instance.isPermissions(p)) {
				String ip = e.getAddress().getHostAddress();
				if (pluginConfig.secure_settings_enable_ip_whitelist) {
					if (!isIPAllowed(p.getName(), ip)) {
						if (!instance.isExcluded(p, pluginConfig.excluded_ip_whitelist)) {
							instance.checkFail(p.getName(),
									instance.getConfig().getStringList("commands.not-admin-ip"));
						}
					}
				}
				if (!api.ips.contains(p.getName() + ip) && pluginConfig.session_settings_session) {
					if (!instance.isExcluded(p, pluginConfig.excluded_admin_pass)) {
						ServerProtectorCaptureEvent captureEvent = new ServerProtectorCaptureEvent(p, ip);
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
		instance.getRunner().runAsync(() -> {
			if (instance.isPermissions(p)) {
				if (api.isCaptured(p)) {
					if (pluginConfig.effect_settings_enable_effects) {
						instance.giveEffect(p);
					}
				}
				if (pluginConfig.blocking_settings_hide_on_entering) {
					runner.runPlayer(() -> {
						for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
							if (!onlinePlayer.equals(p)) {
								onlinePlayer.hidePlayer(instance, p);
							}
						}
					}, p);
				}
				if (pluginConfig.blocking_settings_hide_other_on_entering) {
					runner.runPlayer(() -> {
						for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
							p.hidePlayer(instance, onlinePlayer);
						}
					}, p);
				}
				if (pluginConfig.logging_settings_logging_join) {
					instance.logAction("log-format.joined", p, new Date());
				}
				if (pluginConfig.message_settings_enable_console_broadcasts) {
					String msg = pluginConfig.broadcasts_joined.replace("%player%", p.getName()).replace("%ip%",
							Utils.getIp(p));
					Bukkit.getConsoleSender().sendMessage(msg);
				}
				if (pluginConfig.message_settings_enable_broadcasts) {
					String msg = pluginConfig.broadcasts_joined.replace("%player%", p.getName()).replace("%ip%",
							Utils.getIp(p));
					instance.sendAlert(p, msg);
				}
			}
		});
	}

	private boolean isIPAllowed(String p, String ip) {
		List<String> ips = pluginConfig.ip_whitelist.get(p);
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

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLeave(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (api.isCaptured(player)) {
			for (PotionEffect s : player.getActivePotionEffects()) {
				player.removePotionEffect(s.getType());
			}
		}
		String playerName = player.getName();
		instance.time.remove(playerName);
		instance.login.remove(playerName);
		api.saved.remove(playerName);
	}
}