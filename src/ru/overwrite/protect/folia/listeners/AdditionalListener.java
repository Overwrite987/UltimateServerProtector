package ru.overwrite.protect.folia.listeners;

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
import ru.overwrite.protect.folia.ServerProtector;
import ru.overwrite.protect.folia.utils.Config;

public class AdditionalListener implements Listener {
	
	private final ServerProtector instance = ServerProtector.getInstance();

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent e) {
    	if (instance.login.isEmpty()) return;
        Player p = e.getPlayer();
        if (Config.blocking_settings_block_item_drop) {
        	instance.handleInteraction(p, e);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onItemPickup(EntityPickupItemEvent e) {
    	if (instance.login.isEmpty()) return;
    	if (!(e.getEntity() instanceof Player)) return;
    	Player p = (Player)e.getEntity();
        if (Config.blocking_settings_block_item_pickup) {
        	instance.handleInteraction(p, e);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent e) {
    	if (instance.login.isEmpty()) return;
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player)e.getEntity();
        if (Config.blocking_settings_block_damage) {
        	instance.handleInteraction(p, e);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDamageEntity(EntityDamageByEntityEvent e) {
    	if (instance.login.isEmpty()) return;
    	if (!(e.getDamager() instanceof Player)) return;
    	Player p = (Player)e.getDamager();
    	if (Config.blocking_settings_damaging_entity) {
        	instance.handleInteraction(p, e);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTabComplete(AsyncTabCompleteEvent e) {
    	if (instance.login.isEmpty()) return;
        if (!(e.getSender() instanceof Player)) return;
        Player p = (Player)e.getSender();
        if (Config.blocking_settings_block_tab_complete) {
        	instance.handleInteraction(p, e);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onTarget(EntityTargetEvent e) {
    	if (instance.login.isEmpty()) return;
        if (!(e.getTarget() instanceof Player)) return;
        Player p = (Player)e.getTarget();
        if (Config.blocking_settings_mobs_targeting &&
                    (e.getReason() == TargetReason.CLOSEST_PLAYER || e.getReason() == TargetReason.RANDOM_TARGET)) {
        	instance.handleInteraction(p, e);
        }
    }
}
