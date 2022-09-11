package ru.Overwrite.protect.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.Overwrite.protect.Main;

public class LeaveListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Main.getInstance().login.remove(player);
        Main.getInstance().time.remove(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        Main.getInstance().login.remove(player);
        Main.getInstance().time.remove(player);
    }

}
