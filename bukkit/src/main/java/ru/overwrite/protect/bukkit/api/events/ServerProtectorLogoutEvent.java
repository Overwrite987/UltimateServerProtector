package ru.overwrite.protect.bukkit.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ServerProtectorLogoutEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;

    private final String ip;

    public ServerProtectorLogoutEvent(Player player, String ip) {
        super(true);
        this.player = player;
        this.ip = ip;
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

    @NotNull
    public final Player getPlayer() {
        return this.player;
    }

    @NotNull
    public String getIp() {
        return this.ip;
    }

}
