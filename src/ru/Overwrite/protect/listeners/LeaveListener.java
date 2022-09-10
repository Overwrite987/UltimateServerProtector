package ru.Overwrite.protect.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import ru.Overwrite.protect.Main;

public class LeaveListener implements Listener {
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	  public void onLeave(PlayerQuitEvent event) {
		  Player player = event.getPlayer();
		if (Main.getInstance().login.containsKey(player)) {
		      Main.getInstance().login.remove(player); 
		    }
		if (Main.getInstance().time.containsKey(player)) {
			  Main.getInstance().time.remove(player); 
		    }
	  }
	  
	  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	  public void onKick(PlayerKickEvent event) {
		  Player player = event.getPlayer();
	    if (Main.getInstance().login.containsKey(player)) {
	          Main.getInstance().login.remove(player); 
	        }
	    if (Main.getInstance().time.containsKey(player)) {
		      Main.getInstance().time.remove(player); 
	        }
	 }

}
