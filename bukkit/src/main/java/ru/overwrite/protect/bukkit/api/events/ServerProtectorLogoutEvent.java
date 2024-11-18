package ru.overwrite.protect.bukkit.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class ServerProtectorLogoutEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final String ip;

    public ServerProtectorLogoutEvent(Player player, String ip) {
        super(player, true);
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
    public String getIp() {
        return ip;
    }

}
