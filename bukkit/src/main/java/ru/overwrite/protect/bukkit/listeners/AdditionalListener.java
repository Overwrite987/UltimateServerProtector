package ru.overwrite.protect.bukkit.listeners;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.utils.Config;

public class AdditionalListener implements Listener {

	private final ServerProtectorManager plugin;
	private final ServerProtectorAPI api;
	private final Config pluginConfig;

	public AdditionalListener(ServerProtectorManager plugin) {
		this.plugin = plugin;
		api = plugin.getPluginAPI();
		pluginConfig = plugin.getPluginConfig();
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onItemDrop(PlayerDropItemEvent e) {
		if (plugin.login.isEmpty())
			return;
		Player p = e.getPlayer();
		if (pluginConfig.blocking_settings_block_item_drop) {
			api.handleInteraction(p, e);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onItemPickup(EntityPickupItemEvent e) {
		if (plugin.login.isEmpty())
			return;
		if (!(e.getEntity() instanceof Player))
			return;
		Player p = (Player) e.getEntity();
		if (pluginConfig.blocking_settings_block_item_pickup) {
			api.handleInteraction(p, e);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onTabComplete(AsyncTabCompleteEvent e) {
		if (plugin.login.isEmpty())
			return;
		if (!(e.getSender() instanceof Player))
			return;
		Player p = (Player) e.getSender();
		if (pluginConfig.blocking_settings_block_tab_complete) {
			api.handleInteraction(p, e);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerDamage(EntityDamageEvent e) {
		if (plugin.login.isEmpty())
			return;
		if (!(e.getEntity() instanceof Player))
			return;
		Player p = (Player) e.getEntity();
		if (pluginConfig.blocking_settings_block_damage) {
			api.handleInteraction(p, e);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerDamageEntity(EntityDamageByEntityEvent e) {
		if (plugin.login.isEmpty())
			return;
		if (!(e.getDamager() instanceof Player))
			return;
		Player p = (Player) e.getDamager();
		if (pluginConfig.blocking_settings_damaging_entity) {
			api.handleInteraction(p, e);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onInventoryOpen(InventoryOpenEvent e) {
		if (plugin.login.isEmpty())
			return;
		Player p = (Player) e.getPlayer();
		if (pluginConfig.blocking_settings_block_inventory_open) {
			api.handleInteraction(p, e);
		}
	}
}