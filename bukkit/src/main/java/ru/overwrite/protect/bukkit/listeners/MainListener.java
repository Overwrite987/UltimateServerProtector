package ru.overwrite.protect.bukkit.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.configuration.Config;

public class MainListener implements Listener {

    private final ServerProtectorAPI api;
    private final Config pluginConfig;

    public MainListener(ServerProtectorManager plugin) {
        this.api = plugin.getApi();
        this.pluginConfig = plugin.getPluginConfig();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        if (!api.isAnybodyCaptured())
            return;
        if (pluginConfig.getBlockingSettings().allowOrientationChange() && hasChangedOrientation(e.getFrom(), e.getTo()) && !hasChangedPosition(e.getFrom(), e.getTo())) {
            return;
        }
        Player player = e.getPlayer();
        api.handleInteraction(player, e);
    }

    private boolean hasChangedOrientation(Location from, Location to) {
        return from.getPitch() != to.getPitch() || from.getYaw() != to.getYaw();
    }

    public boolean hasChangedPosition(Location from, Location to) {
        return from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!api.isAnybodyCaptured())
            return;
        Player player = e.getPlayer();
        api.handleInteraction(player, e);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        if (!api.isAnybodyCaptured())
            return;
        Player player = e.getPlayer();
        api.handleInteraction(player, e);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent e) {
        if (!api.isAnybodyCaptured())
            return;
        Player player = e.getPlayer();
        if (pluginConfig.getBlockingSettings().blockItemDrop()) {
            api.handleInteraction(player, e);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onItemPickup(EntityPickupItemEvent e) {
        if (!api.isAnybodyCaptured())
            return;
        if (!(e.getEntity() instanceof Player player))
            return;
        if (pluginConfig.getBlockingSettings().blockItemPickup()) {
            api.handleInteraction(player, e);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent e) {
        if (!api.isAnybodyCaptured())
            return;
        if (!(e.getEntity() instanceof Player player))
            return;
        if (pluginConfig.getBlockingSettings().blockDamage()) {
            api.handleInteraction(player, e);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDamageEntity(EntityDamageByEntityEvent e) {
        if (!api.isAnybodyCaptured())
            return;
        if (!(e.getDamager() instanceof Player player))
            return;
        if (pluginConfig.getBlockingSettings().blockDamagingEntity()) {
            api.handleInteraction(player, e);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (!api.isAnybodyCaptured())
            return;
        Player player = (Player) e.getPlayer();
        if (pluginConfig.getBlockingSettings().blockInventoryOpen()) {
            api.handleInteraction(player, e);
        }
    }
}
