package ru.overwrite.protect.bukkit.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ServerProtectorPasswordEnterEvent extends Event implements Cancellable {
	
	private static final HandlerList HANDLERS = new HandlerList();
	  
	private final Player player;
	
	private final String password;
	  
	private boolean isCancelled;
	  
	public ServerProtectorPasswordEnterEvent(Player player, String password) {
	    this.player = player;
	    this.password = password;
	    this.isCancelled = false;
	}
	  
	public static HandlerList getHandlerList() {
	    return HANDLERS;
	}
	  
	public HandlerList getHandlers() {
	    return HANDLERS;
	}
	  
	public boolean isCancelled() {
	    return isCancelled;
	}
	  
	public void setCancelled(boolean cancel) {
	    isCancelled = cancel;
	}
	  
	public Player getPlayer() {
	    return player;
	}
	
	public String getEnteredPassword() {
	    return password;
	}

}