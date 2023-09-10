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

	private final ServerProtectorManager instance;
	private final ServerProtectorAPI api;
	private final PasswordHandler passwordHandler;
	private final Config pluginConfig;

	public ChatListener(ServerProtectorManager plugin) {
		instance = plugin;
		pluginConfig = plugin.getPluginConfig();
		passwordHandler = new PasswordHandler(plugin);
		api = plugin.getPluginAPI();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChat(AsyncPlayerChatEvent e) {
		if (instance.login.isEmpty())
			return;
		Player p = e.getPlayer();
		String msg = e.getMessage();
		if (api.isCaptured(p)) {
			e.setCancelled(true);
			e.setMessage("");
			if (!pluginConfig.main_settings_use_command) {
				passwordHandler.checkPassword(p, msg, true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onCommand(PlayerCommandPreprocessEvent e) {
		if (instance.login.isEmpty())
			return;
		Player p = e.getPlayer();
		if (!api.isCaptured(p))
			return;
		e.setCancelled(true);
		if (pluginConfig.main_settings_use_command) {
			String message = e.getMessage();
			String label = cutCommand(message).toLowerCase();
			if (label.equals("/" + pluginConfig.main_settings_pas_command)) {
				e.setCancelled(false);
			} else
				for (String command : pluginConfig.allowed_commands) {
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