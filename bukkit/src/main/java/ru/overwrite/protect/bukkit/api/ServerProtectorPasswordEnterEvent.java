package ru.overwrite.protect.bukkit.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class ServerProtectorPasswordEnterEvent extends PlayerEvent implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();

	private final String password;

	private boolean isCancelled = false;

	public ServerProtectorPasswordEnterEvent(Player player, String password) {
		super(player);
		this.password = password;
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

	public String getEnteredPassword() {
		return password;
	}

}