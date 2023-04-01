package ru.overwrite.protect.bukkit.listeners;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import ru.overwrite.protect.bukkit.ServerProtector;
import ru.overwrite.protect.bukkit.utils.Config;
import java.util.Locale;

public class ChatListener implements Listener {
	
	private final ServerProtector instance = ServerProtector.getInstance();
	
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e) {
    	if (instance.login.isEmpty()) return;
        Player p = e.getPlayer();
        String msg = e.getMessage();
        if (instance.login.contains(p.getName())) {
            e.setCancelled(true);
            e.setMessage("");
            if (!Config.main_settings_use_command) {
            	instance.passwordHandler.checkPassword(p, msg, true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent e) {
    	if (instance.login.isEmpty()) return;
        Player p = e.getPlayer();
        if (!instance.login.contains(p.getName())) return;
        e.setCancelled(true);
        if (Config.main_settings_use_command) {
            String message = e.getMessage();
            String label = cutCommand(message).toLowerCase(Locale.ROOT);
            FileConfiguration config = instance.getConfig();
            if (label.equals("/" + Config.main_settings_pas_command)) {
                e.setCancelled(false);
            } else for (String command : config.getStringList("allowed-commands")) {
                if (label.equals(command) || message.equalsIgnoreCase(command)) {
                    e.setCancelled(false);
                    break;
                }
            }
        }
    }

    private String cutCommand(String str) {
        int index = str.indexOf(' ');
        return index == -1 ? str : str.substring(0, index);
    }
}
