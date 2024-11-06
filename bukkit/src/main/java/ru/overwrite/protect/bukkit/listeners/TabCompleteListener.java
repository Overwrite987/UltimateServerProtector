package ru.overwrite.protect.bukkit.listeners;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.configuration.Config;

public class TabCompleteListener implements Listener {

    private final ServerProtectorAPI api;
    private final Config pluginConfig;

    public TabCompleteListener(ServerProtectorManager plugin) {
        this.api = plugin.getPluginAPI();
        this.pluginConfig = plugin.getPluginConfig();
    }

    @EventHandler(ignoreCancelled = true)
    public void onTabComplete(AsyncTabCompleteEvent e) {
        if (!api.isAnybodyCaptured())
            return;
        if (!(e.getSender() instanceof Player p))
            return;
        if (pluginConfig.getBlockingSettings().blockTabComplete()) {
            api.handleInteraction(p, e);
        }
    }
}