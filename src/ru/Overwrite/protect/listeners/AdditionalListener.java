package ru.overwrite.protect.listeners;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import ru.overwrite.protect.ServerProtector;

public class AdditionalListener implements Listener {
	
	FileConfiguration config = ServerProtector.getInstance().getConfig();

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        if (config.getBoolean("blocking-settings.block-item-drop")) {
        	ServerProtector.getInstance().handleInteraction(p, e);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent e) {
        Player p = e.getPlayer();
        if (config.getBoolean("blocking-settings.block-item-pickup")) {
        	ServerProtector.getInstance().handleInteraction(p, e);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player)e.getEntity();
        if (config.getBoolean("blocking-settings.block-damage")) {
        	ServerProtector.getInstance().handleInteraction(p, e);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTabComplete(AsyncTabCompleteEvent e) {
        if (!(e.getSender() instanceof Player)) return;
        Player p = (Player)e.getSender();
        if (config.getBoolean("blocking-settings.block-tab-complete")) {
        	ServerProtector.getInstance().handleInteraction(p, e);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onTarget(EntityTargetEvent e) {
        if (!(e.getTarget() instanceof Player)) return;
        Player p = (Player) e.getTarget();
        if (config.getBoolean("blocking-settings.block-mobs-targeting") &&
                    (e.getReason() == TargetReason.CLOSEST_PLAYER || e.getReason() == TargetReason.RANDOM_TARGET)) {
        	ServerProtector.getInstance().handleInteraction(p, e);
        }
    }
}
