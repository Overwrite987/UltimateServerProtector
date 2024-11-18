package ru.overwrite.protect.bukkit.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import ru.overwrite.protect.bukkit.api.CaptureReason;

public class ServerProtectorCaptureEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final String ip;

    private final CaptureReason captureReason;

    private boolean isCancelled = false;

    public ServerProtectorCaptureEvent(Player player, String ip, CaptureReason captureReason) {
        super(player, true);
        this.ip = ip;
        this.captureReason = captureReason;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }

    @NotNull
    public String getIp() {
        return ip;
    }

    @NotNull
    public CaptureReason getCaptureReason() {
        return captureReason;
    }

}
