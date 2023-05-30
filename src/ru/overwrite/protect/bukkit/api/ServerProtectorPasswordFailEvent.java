package ru.overwrite.protect.bukkit.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ServerProtectorPasswordFailEvent extends Event implements Cancellable {
	
	private static final HandlerList HANDLERS = new HandlerList();
	  
	private final Player player;
	
	private final int attempts;
	  
	private boolean isCancelled;
	  
	public ServerProtectorPasswordFailEvent(Player player, Integer attempts) {
	    this.player = player;
	    this.attempts = attempts;
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
	
	public int getAttempts() {
	    return attempts;
	}

}