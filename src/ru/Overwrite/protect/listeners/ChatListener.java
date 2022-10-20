package ru.overwrite.protect.listeners;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import ru.overwrite.protect.ServerProtector;

import java.util.Locale;

public class ChatListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e) {
        FileConfiguration config = ServerProtector.getInstance().getConfig();
        Player p = e.getPlayer();
        String msg = e.getMessage();
        if (ServerProtector.getInstance().login.containsKey(p)) {
            e.setCancelled(true);
            e.setMessage("");
            if (!config.getBoolean("main-settings.use-command")) {
            	ServerProtector.getInstance().passwordHandler.checkPassword(p, msg, true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (!ServerProtector.getInstance().login.containsKey(p)) return;
        e.setCancelled(true);
        FileConfiguration config = ServerProtector.getInstance().getConfig();
        if (config.getBoolean("main-settings.use-command")) {
            String message = e.getMessage();
            String label = cutCommand(message).toLowerCase(Locale.ROOT);
            if (label.equals("/" + config.getString("main-settings.pas-command"))) {
                e.setCancelled(false);
            } else for (String command : config.getStringList("allowed-commands")) {
                if (label.equals(command) || message.equalsIgnoreCase(command)) {
                    e.setCancelled(false);
                    break;
                }
            }
        }
    }

    private static String cutCommand(String str) {
        int index = str.indexOf(' ');
        return index == -1 ? str : str.substring(0, index);
    }
}
