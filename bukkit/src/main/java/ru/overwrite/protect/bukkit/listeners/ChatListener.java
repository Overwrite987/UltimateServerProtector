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
import ru.overwrite.protect.bukkit.configuration.Config;

import java.util.List;

public class ChatListener implements Listener {

    private final ServerProtectorManager plugin;
    private final ServerProtectorAPI api;
    private final PasswordHandler passwordHandler;
    private final Config pluginConfig;

    public ChatListener(ServerProtectorManager plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
        this.passwordHandler = plugin.getPasswordHandler();
        this.api = plugin.getApi();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        if (!api.isCaptured(player)) {
            return;
        }
        if (!pluginConfig.getMainSettings().useCommand()) {
            String message = e.getMessage();
            passwordHandler.checkPassword(player, message, true);
        }
        e.setMessage("");
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        if (!api.isCaptured(player)) {
            return;
        }
        String message = e.getMessage();
        String label = cutCommand(message);
        if (pluginConfig.getMainSettings().useCommand()) {
            if (label.equalsIgnoreCase("/" + pluginConfig.getMainSettings().pasCommand())) {
                if (!plugin.isPaper()) {
                    passwordHandler.checkPassword(player, message.split(" ", 1)[1], false);
                }
                return;
            }
        }
        List<String> allowedCommands = pluginConfig.getAccessData().allowedCommands();
        for (int i = 0; i < allowedCommands.size(); i++) {
            final String command = allowedCommands.get(i);
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
