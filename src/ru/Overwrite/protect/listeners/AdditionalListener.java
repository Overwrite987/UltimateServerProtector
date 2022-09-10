package ru.Overwrite.protect.listeners;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;

import ru.Overwrite.protect.Main;

public class AdditionalListener implements Listener {
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	  public void onItemDrop(PlayerDropItemEvent e) {
		FileConfiguration config = Main.getInstance().getConfig();
		  Player p = e.getPlayer();
		if (config.getBoolean("blocking-settings.block-item-drop")) {
		if (Main.getInstance().login.containsKey(p)) {
	      e.setCancelled(true);
	    }
	  }
	}
	  
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	  public void onItemPickup(PlayerPickupItemEvent e) {
		FileConfiguration config = Main.getInstance().getConfig();
		  Player p = e.getPlayer();
		if (config.getBoolean("blocking-settings.block-item-pickup")) {
	    if (Main.getInstance().login.containsKey(p)) {
	      e.setCancelled(true); 
	    }
	  }
	}
	  
	  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	  public void onPlayerDamage(EntityDamageEvent e) {
	    if (!(e.getEntity() instanceof Player))
		  return; 
	    FileConfiguration config = Main.getInstance().getConfig();
		  Player p = (Player)e.getEntity();
		if (config.getBoolean("blocking-settings.block-damage")) {
	      if (Main.getInstance().login.containsKey(p)) {
	        e.setCancelled(true); 
	      }
	    }
	  }
	  
	  @EventHandler
	  public void onTabComplete(AsyncTabCompleteEvent e) {
	  if (!(e.getSender() instanceof Player)) {
		return;
	  }
		FileConfiguration config = Main.getInstance().getConfig();
		  Player p = (Player)e.getSender();
		if (config.getBoolean("blocking-settings.block-tab-complete")) {
		if (Main.getInstance().login.containsKey(p)) {
	      e.setCancelled(true);
	    }
	  }
	}
	  
	  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	  public void onTarget(EntityTargetEvent e) {
		if (!(e.getTarget() instanceof Player))
			return;
		FileConfiguration config = Main.getInstance().getConfig();
	      Player p = (Player)e.getTarget();
	    if (config.getBoolean("blocking-settings.block-mobs-targeting")) {
	    if (!e.isCancelled() && Main.getInstance().login.containsKey(p) && 
	    (e.getReason() == EntityTargetEvent.TargetReason.CLOSEST_PLAYER || e.getReason() == EntityTargetEvent.TargetReason.RANDOM_TARGET))
	      e.setCancelled(true); 
	    }
	  }
}
