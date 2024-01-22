package ru.overwrite.protect.bukkit.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ServerProtectorLogoutEvent extends Event {

	private static final HandlerList HANDLERS = new HandlerList();

	private final Player player;
	
	private final String ip;

	public ServerProtectorLogoutEvent(Player player, String ip) {
		super(true);
		this.player = player;
		this.ip = ip;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public Player getPlayer() {
		return player;
	}
	
	public String getIp() {
		return ip;
	}

}