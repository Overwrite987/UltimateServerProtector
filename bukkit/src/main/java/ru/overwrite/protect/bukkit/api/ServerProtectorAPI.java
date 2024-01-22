package ru.overwrite.protect.bukkit.api;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import ru.overwrite.protect.bukkit.Logger;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.utils.Config;
import ru.overwrite.protect.bukkit.utils.Utils;

public class ServerProtectorAPI {

	private final ServerProtectorManager instance;
	private final Config pluginConfig;
	private final Logger logger;
	public Set<String> ips = new HashSet<>();
	public Set<String> saved = new HashSet<>();

	public ServerProtectorAPI(ServerProtectorManager plugin) {
		this.instance = plugin;
		pluginConfig = plugin.getPluginConfig();
		logger = plugin.getPluginLogger();
	}

	public boolean isCaptured(Player p) {
		return instance.login.contains(p.getName());
	}

	public void capturePlayer(Player p) {
		if (isCaptured(p)) {
			logger.warn("Unable to capture " + p.getName() + " Reason: Already captured");
			return;
		}
		instance.login.add(p.getName());
	}

	public void uncapturePlayer(Player p) {
		if (!isCaptured(p)) {
			logger.warn("Unable to uncapture " + p.getName() + " Reason: Not captured");
			return;
		}
		instance.login.remove(p.getName());
	}

	public boolean isAuthorised(Player p) {
		return pluginConfig.session_settings_session ? ips.contains(p.getName() + Utils.getIp(p))
				: saved.contains(p.getName());
	}

	public void authorisePlayer(Player p) {
		if (isAuthorised(p)) {
			logger.warn("Unable to authorise " + p.getName() + " Reason: Alerady authorised");
			return;
		}
		if (pluginConfig.session_settings_session) {
			ips.add(p.getName() + Utils.getIp(p));
		} else {
			saved.add(p.getName());
		}
	}
	
	public void deauthorisePlayer(Player p) {
		if (!isAuthorised(p)) {
			logger.warn("Unable to deauthorise " + p.getName() + " Reason: Is not authorised");
			return;
		}
		if (pluginConfig.session_settings_session) {
			ips.remove(p.getName() + Utils.getIp(p));
		} else {
			saved.remove(p.getName());
		}
	}

	public void handleInteraction(Player p, Cancellable e) {
		if (isCaptured(p)) {
			e.setCancelled(true);
		}
	}
}