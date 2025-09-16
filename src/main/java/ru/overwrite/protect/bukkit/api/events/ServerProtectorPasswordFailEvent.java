package ru.overwrite.protect.bukkit.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ServerProtectorPasswordFailEvent extends ServerProtectorPlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final int attempts;

    private boolean isCancelled = false;

    public ServerProtectorPasswordFailEvent(Player player, Integer attempts) {
        super(player);
        this.player = player;
        this.attempts = attempts;
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

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }

    public int getAttempts() {
        return this.attempts;
    }

}
