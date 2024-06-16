package ru.overwrite.protect.bukkit.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ServerProtectorCaptureEvent extends Event implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();

	private final Player player;
	
	private final String ip;

	private final CaptureReason captureReason;

	private boolean isCancelled;

	public ServerProtectorCaptureEvent(Player player, String ip, CaptureReason captureReason) {
		super(true);
		this.player = player;
		this.ip = ip;
		this.captureReason = captureReason;
		isCancelled = false;
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
	
	public String getIp() {
		return ip;
	}

	public CaptureReason getCaptureReason() {
		return captureReason;
	}

}