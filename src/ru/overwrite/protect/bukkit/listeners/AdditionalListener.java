package ru.overwrite.protect.bukkit.listeners;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.player.PlayerDropItemEvent;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.utils.Config;

public class AdditionalListener implements Listener {
	
	private final ServerProtectorManager instance;
	private final ServerProtectorAPI api;
	private final Config pluginConfig;
	
	public AdditionalListener(ServerProtectorManager plugin) {
        instance = plugin;
        api = plugin.getPluginAPI();
        pluginConfig = plugin.getPluginConfig();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent e) {
    	if (instance.login.isEmpty()) return;
        Player p = e.getPlayer();
        if (pluginConfig.blocking_settings_block_item_drop) {
        	api.handleInteraction(p, e);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onItemPickup(EntityPickupItemEvent e) {
    	if (instance.login.isEmpty()) return;
    	if (!(e.getEntity() instanceof Player)) return;
    	Player p = (Player)e.getEntity();
        if (pluginConfig.blocking_settings_block_item_pickup) {
        	api.handleInteraction(p, e);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent e) {
    	if (instance.login.isEmpty()) return;
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player)e.getEntity();
        if (pluginConfig.blocking_settings_block_damage) {
        	api.handleInteraction(p, e);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDamageEntity(EntityDamageByEntityEvent e) {
    	if (instance.login.isEmpty()) return;
    	if (!(e.getDamager() instanceof Player)) return;
    	Player p = (Player)e.getDamager();
    	if (pluginConfig.blocking_settings_damaging_entity) {
        	api.handleInteraction(p, e);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTabComplete(AsyncTabCompleteEvent e) {
    	if (instance.login.isEmpty()) return;
        if (!(e.getSender() instanceof Player)) return;
        Player p = (Player)e.getSender();
        if (pluginConfig.blocking_settings_block_tab_complete) {
        	api.handleInteraction(p, e);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onTarget(EntityTargetEvent e) {
    	if (instance.login.isEmpty()) return;
        if (!(e.getTarget() instanceof Player)) return;
        Player p = (Player)e.getTarget();
        if (pluginConfig.blocking_settings_mobs_targeting &&
                    (e.getReason() == TargetReason.CLOSEST_PLAYER || e.getReason() == TargetReason.RANDOM_TARGET)) {
        	api.handleInteraction(p, e);
        }
    }
}