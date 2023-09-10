package ru.overwrite.protect.bukkit.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import ru.overwrite.protect.bukkit.ServerProtectorManager;

public class ServerProtectorAPI {

	private final ServerProtectorManager instance;

	public ServerProtectorAPI(ServerProtectorManager plugin) {
		this.instance = plugin;
	}

	public boolean isCaptured(Player p) {
		return instance.login.contains(p.getName());
	}

	public void capturePlayer(Player p) {
		if (!isCaptured(p)) {
			instance.login.add(p.getName());
		}
	}

	public void uncapturePlayer(Player p) {
		if (isCaptured(p)) {
			instance.login.remove(p.getName());
		}
	}

	public void handleInteraction(Player p, Cancellable e) {
		if (isCaptured(p)) {
			e.setCancelled(true);
		}
	}
}