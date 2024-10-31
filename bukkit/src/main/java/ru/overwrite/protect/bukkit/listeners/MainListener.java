package ru.overwrite.protect.bukkit.listeners;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.utils.Config;

public class MainListener implements Listener {

    private final ServerProtectorAPI api;
    private final Config pluginConfig;

    public MainListener(ServerProtectorManager plugin) {
        this.api = plugin.getPluginAPI();
        this.pluginConfig = plugin.getPluginConfig();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        if (api.login.isEmpty())
            return;
        if (pluginConfig.getBlockingSettings().allowOrientationChange() && hasChangedOrientation(e.getFrom(), e.getTo()) && !hasExplicitlyChangedBlock(e.getFrom(), e.getTo())) {
            return;
        }
        Player p = e.getPlayer();
        api.handleInteraction(p, e);
    }

    private boolean hasChangedOrientation(Location from, Location to) {
        return from.getPitch() != to.getPitch() || from.getYaw() != to.getYaw();
    }

    public boolean hasExplicitlyChangedBlock(Location from, Location to) {
        return from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (api.login.isEmpty())
            return;
        Player p = e.getPlayer();
        api.handleInteraction(p, e);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        if (api.login.isEmpty())
            return;
        Player p = e.getPlayer();
        api.handleInteraction(p, e);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent e) {
        if (api.login.isEmpty())
            return;
        Player p = e.getPlayer();
        if (pluginConfig.getBlockingSettings().blockItemDrop()) {
            api.handleInteraction(p, e);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onItemPickup(EntityPickupItemEvent e) {
        if (api.login.isEmpty())
            return;
        if (!(e.getEntity() instanceof Player p))
            return;
        if (pluginConfig.getBlockingSettings().blockItemPickup()) {
            api.handleInteraction(p, e);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent e) {
        if (api.login.isEmpty())
            return;
        if (!(e.getEntity() instanceof Player p))
            return;
        if (pluginConfig.getBlockingSettings().blockDamage()) {
            api.handleInteraction(p, e);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDamageEntity(EntityDamageByEntityEvent e) {
        if (api.login.isEmpty())
            return;
        if (!(e.getDamager() instanceof Player p))
            return;
        if (pluginConfig.getBlockingSettings().blockDamagingEntity()) {
            api.handleInteraction(p, e);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (api.login.isEmpty())
            return;
        Player p = (Player) e.getPlayer();
        if (pluginConfig.getBlockingSettings().blockInventoryOpen()) {
            api.handleInteraction(p, e);
        }
    }
}