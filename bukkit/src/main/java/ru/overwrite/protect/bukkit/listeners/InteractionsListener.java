package ru.overwrite.protect.bukkit.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;

public class InteractionsListener implements Listener {

	private final ServerProtectorManager plugin;
	private final ServerProtectorAPI api;

	public InteractionsListener(ServerProtectorManager plugin) {
		this.plugin = plugin;
		api = plugin.getPluginAPI();
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onMove(PlayerMoveEvent e) {
		if (plugin.login.isEmpty())
			return;
		Player p = e.getPlayer();
		api.handleInteraction(p, e);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (plugin.login.isEmpty())
			return;
		Player p = e.getPlayer();
		api.handleInteraction(p, e);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
		if (plugin.login.isEmpty())
			return;
		Player p = e.getPlayer();
		api.handleInteraction(p, e);
	}

}