package ru.overwrite.protect.bukkit.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

// Literally just copied PlayerEvent from Paper
public abstract class ServerProtectorPlayerEvent extends Event {

    protected Player player;

    public ServerProtectorPlayerEvent(@NotNull final Player who) {
        this.player = who;
    }

    public ServerProtectorPlayerEvent(@NotNull final Player who, boolean async) {
        super(async);
        this.player = who;
    }

    /**
     * Returns the player involved in this event
     *
     * @return Player who is involved in this event
     */
    @NotNull
    public final Player getPlayer() {
        return this.player;
    }
}
