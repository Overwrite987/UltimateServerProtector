package ru.overwrite.protect.bukkit.api.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

// Literally just copied PlayerEvent from Paper
public abstract class ServerProtectorPlayerEvent extends Event {

    protected Player player;

    protected ServerProtectorPlayerEvent(@NotNull final Player who) {
        this.player = who;
    }

    protected ServerProtectorPlayerEvent(@NotNull final Player who, boolean async) {
        super(async);
        this.player = who;
    }

    /**
     * Calls the event and tests if cancelled.
     *
     * @return false if event was cancelled, if cancellable. otherwise true.
     */
    @Override
    public boolean callEvent() {
        Bukkit.getPluginManager().callEvent(this);
        if (this instanceof Cancellable cancellable) {
            return !cancellable.isCancelled();
        } else {
            return true;
        }
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
