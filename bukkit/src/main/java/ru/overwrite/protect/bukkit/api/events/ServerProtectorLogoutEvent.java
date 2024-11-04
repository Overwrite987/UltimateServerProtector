package ru.overwrite.protect.bukkit.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class ServerProtectorLogoutEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final String ip;

    public ServerProtectorLogoutEvent(Player player, String ip) {
        super(player, true);
        this.player = player;
        this.ip = ip;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public String getIp() {
        return ip;
    }

}