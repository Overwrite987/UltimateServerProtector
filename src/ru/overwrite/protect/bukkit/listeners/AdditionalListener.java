package ru.overwrite.protect.bukkit.listeners;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import org.bukkit.configuration.file.FileConfiguration;
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
import ru.overwrite.protect.bukkit.ServerProtector;
public class AdditionalListener implements Listener { 
 private final ServerProtector instance = ServerProtector.getInstance();
 @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
 public void onItemDrop(PlayerDropItemEvent e) {
 FileConfiguration config = instance.getConfig();
 Player p = e.getPlayer();
 if (config.getBoolean("blocking-settings.block-item-drop")) {
 instance.handleInteraction(p, e);
 }
 }
 @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
 public void onItemPickup(EntityPickupItemEvent e) {
 if (!(e.getEntity() instanceof Player)) return;
 FileConfiguration config = instance.getConfig();
 Player p = (Player)e.getEntity();
 if (config.getBoolean("blocking-settings.block-item-pickup")) {
 instance.handleInteraction(p, e);
 }
 }
 @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
 public void onPlayerDamage(EntityDamageEvent e) {
 if (!(e.getEntity() instanceof Player)) return;
 FileConfiguration config = instance.getConfig();
 Player p = (Player)e.getEntity();
 if (config.getBoolean("blocking-settings.block-damage")) {
 instance.handleInteraction(p, e);
 }
 } 
 @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
 public void onPlayerDamageEntity(EntityDamageByEntityEvent e) {
 if (!(e.getDamager() instanceof Player)) return;
 FileConfiguration config = instance.getConfig();
 Player p = (Player)e.getDamager();
 if (config.getBoolean("blocking-settings.block-damaging-entity")) {
 instance.handleInteraction(p, e);
 }
 }
 @EventHandler(ignoreCancelled = true)
 public void onTabComplete(AsyncTabCompleteEvent e) {
 if (!(e.getSender() instanceof Player)) return;
 FileConfiguration config = instance.getConfig();
 Player p = (Player)e.getSender();
 if (config.getBoolean("blocking-settings.block-tab-complete")) {
 instance.handleInteraction(p, e);
 }
 }
 @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
 public void onTarget(EntityTargetEvent e) {
 if (!(e.getTarget() instanceof Player)) return;
 FileConfiguration config = instance.getConfig();
 Player p = (Player)e.getTarget();
 if (config.getBoolean("blocking-settings.block-mobs-targeting") &&
 (e.getReason() == TargetReason.CLOSEST_PLAYER || e.getReason() == TargetReason.RANDOM_TARGET)) {
 instance.handleInteraction(p, e);
 }
 }
}
