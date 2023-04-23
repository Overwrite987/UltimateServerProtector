package ru.overwrite.protect.bukkit.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import ru.overwrite.protect.bukkit.ServerProtectorManager;

public class InteractionsListener implements Listener {
	
	private final ServerProtectorManager instance;
	
	public InteractionsListener(ServerProtectorManager plugin) {
        this.instance = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
    	if (instance.login.isEmpty()) return;
    	Player p = e.getPlayer();
    	instance.handleInteraction(p, e);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e) {
    	if (instance.login.isEmpty()) return;
    	Player p = e.getPlayer();
    	instance.handleInteraction(p, e);
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
    	if (instance.login.isEmpty()) return;
    	Player p = e.getPlayer();
    	instance.handleInteraction(p, e);
    }
    
}
