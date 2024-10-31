package ru.overwrite.protect.bukkit.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import ru.overwrite.protect.bukkit.PasswordHandler;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.utils.Config;

public class ChatListener implements Listener {

    private final ServerProtectorManager plugin;
    private final ServerProtectorAPI api;
    private final PasswordHandler passwordHandler;
    private final Config pluginConfig;

    public ChatListener(ServerProtectorManager plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
        this.passwordHandler = plugin.getPasswordHandler();
        this.api = plugin.getPluginAPI();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (!api.isCaptured(p)) {
            return;
        }
        if (!pluginConfig.getMainSettings().useCommand()) {
            String message = e.getMessage();
            passwordHandler.checkPassword(p, message, true);
        }
        e.setCancelled(true);
        e.setMessage("");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (!api.isCaptured(p)) {
            return;
        }
        String message = e.getMessage();
        String label = cutCommand(message);
        if (pluginConfig.getMainSettings().useCommand()) {
            if (label.equalsIgnoreCase("/" + pluginConfig.getMainSettings().pasCommand())) {
                if (!plugin.isPaper()) {
                    passwordHandler.checkPassword(p, message.split(" ", 1)[1], false);
                }
                return;
            }
        }
        for (String command : pluginConfig.getAccessData().allowedCommands()) {
            if (label.equalsIgnoreCase(command) || message.equalsIgnoreCase(command)) {
                return;
            }
        }
        e.setCancelled(true);
    }

    private String cutCommand(String str) {
        int index = str.indexOf(' ');
        return index == -1 ? str : str.substring(0, index);
    }
}