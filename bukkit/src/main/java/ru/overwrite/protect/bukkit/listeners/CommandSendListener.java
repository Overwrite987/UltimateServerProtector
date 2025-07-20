package ru.overwrite.protect.bukkit.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.api.events.ServerProtectorPasswordSuccessEvent;
import ru.overwrite.protect.bukkit.configuration.Config;

public class CommandSendListener implements Listener {

    private final ServerProtectorAPI api;
    private final Config pluginConfig;

    public CommandSendListener(ServerProtectorManager plugin) {
        this.api = plugin.getApi();
        this.pluginConfig = plugin.getPluginConfig();
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommandSend(PlayerCommandSendEvent e) {
        if (pluginConfig.getBlockingSettings().blockTabComplete() && api.isCaptured(e.getPlayer())) {
            e.getCommands().removeIf(command -> !command.equals(pluginConfig.getMainSettings().pasCommand()));
        }
    }

    @EventHandler
    public void onSucsessPassword(ServerProtectorPasswordSuccessEvent e) {
        e.getPlayer().updateCommands();
    }
}
