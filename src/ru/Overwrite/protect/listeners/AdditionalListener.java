package ru.Overwrite.protect.listeners;

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
import ru.Overwrite.protect.Main;

public class AdditionalListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent e) {
        FileConfiguration config = Main.getInstance().getConfig();
        Player p = e.getPlayer();
        if (config.getBoolean("blocking-settings.block-item-drop")) {
            Main.handleInteraction(p, e);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent e) {
        FileConfiguration config = Main.getInstance().getConfig();
        Player p = e.getPlayer();
        if (config.getBoolean("blocking-settings.block-item-pickup")) {
            Main.handleInteraction(p, e);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        FileConfiguration config = Main.getInstance().getConfig();
        Player p = (Player)e.getEntity();
        if (config.getBoolean("blocking-settings.block-damage")) {
            Main.handleInteraction(p, e);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTabComplete(AsyncTabCompleteEvent e) {
        if (!(e.getSender() instanceof Player)) return;
        FileConfiguration config = Main.getInstance().getConfig();
        Player p = (Player)e.getSender();
        if (config.getBoolean("blocking-settings.block-tab-complete")) {
            Main.handleInteraction(p, e);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onTarget(EntityTargetEvent e) {
        if (!(e.getTarget() instanceof Player)) return;
        FileConfiguration config = Main.getInstance().getConfig();
        Player p = (Player) e.getTarget();
        if (config.getBoolean("blocking-settings.block-mobs-targeting") &&
                    (e.getReason() == TargetReason.CLOSEST_PLAYER || e.getReason() == TargetReason.RANDOM_TARGET)) {
            Main.handleInteraction(p, e);
        }
    }
}
