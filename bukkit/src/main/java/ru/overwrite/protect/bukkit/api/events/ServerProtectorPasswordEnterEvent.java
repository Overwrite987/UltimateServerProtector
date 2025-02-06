package ru.overwrite.protect.bukkit.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ServerProtectorPasswordEnterEvent extends ServerProtectorPlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final String password;

    private boolean isCancelled = false;

    public ServerProtectorPasswordEnterEvent(Player player, String password) {
        super(player);
        this.player = player;
        this.password = password;
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
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }

    @NotNull
    public String getEnteredPassword() {
        return this.password;
    }

}
