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
import ru.overwrite.protect.bukkit.utils.Utils;

public class ChatListener implements Listener {

	private final ServerProtectorManager instance;
	private final ServerProtectorAPI api;
	private final PasswordHandler passwordHandler;
	private final Config pluginConfig;

	public ChatListener(ServerProtectorManager plugin) {
		instance = plugin;
		pluginConfig = plugin.getPluginConfig();
		passwordHandler = plugin.getPasswordHandler();
		api = plugin.getPluginAPI();
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onChat(AsyncPlayerChatEvent e) {
		Player p = e.getPlayer();
		if (!api.isCaptured(p)) {
			return;
		}
		if (!pluginConfig.main_settings_use_command) {
			String message = e.getMessage();
			String inputPass = pluginConfig.encryption_settings_enable_encryption
					? Utils.encryptPassword(false, message, pluginConfig.encryption_settings_encrypt_methods)
					: message;
			passwordHandler.checkPassword(p, inputPass, true);
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
		String label = cutCommand(message).toLowerCase();
		if (pluginConfig.main_settings_use_command) {
			if (label.equals("/" + pluginConfig.main_settings_pas_command)) {
				if (!instance.paper) {
					String inputPass = pluginConfig.encryption_settings_enable_encryption
							? Utils.encryptPassword(false, message.split(" ")[1], pluginConfig.encryption_settings_encrypt_methods)
							: message.split(" ")[1];
					passwordHandler.checkPassword(p, inputPass, false);
				}
				return;
			}
		}
		for (String command : pluginConfig.allowed_commands) {
			if (label.equals(command) || message.equalsIgnoreCase(command)) {
				return;
			}
		}
		e.setCancelled(true);
	}

	private static String cutCommand(String str) {
		int index = str.indexOf(' ');
		return index == -1 ? str : str.substring(0, index);
	}
}