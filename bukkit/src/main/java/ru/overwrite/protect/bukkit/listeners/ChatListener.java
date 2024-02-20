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
		if (instance.login.isEmpty())
			return;
		Player p = e.getPlayer();
		if (api.isCaptured(p)) {
			String msg = e.getMessage();
			e.setCancelled(true);
			if (!pluginConfig.main_settings_use_command) {
				String inputPass = pluginConfig.encryption_settings_enable_encryption ? Utils.encryptPassword(msg, pluginConfig.encryption_settings_encrypt_methods) : msg;
				passwordHandler.checkPassword(p, inputPass, true);
			}
		}
		e.setMessage("");
	}

	@EventHandler(priority = EventPriority.LOWEST)
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
				return;
			} else {
				for (String command : pluginConfig.allowed_commands) {
					if (label.equals(command) || message.equalsIgnoreCase(command)) {
						e.setCancelled(false);
						return;
					}
				}
			}
		}
		e.setMessage("");
	}

	private String cutCommand(String str) {
		int index = str.indexOf(' ');
		return index == -1 ? str : str.substring(0, index);
	}
}