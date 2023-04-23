package ru.overwrite.protect.bukkit.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.PasswordHandler;
import ru.overwrite.protect.bukkit.utils.Config;

public class PasCommand implements CommandExecutor {
	
	private final ServerProtectorManager instance;
	private final PasswordHandler passwordHandler;
	private final Config pluginConfig;
	
	public PasCommand(ServerProtectorManager plugin) {
        this.instance = plugin;
        passwordHandler = new PasswordHandler(plugin);
        pluginConfig = plugin.getPluginConfig();
    }
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			instance.logger.info(pluginConfig.msg_playeronly);
			return true;
		}
		Player p = (Player)sender;
		if (!instance.login.contains(p.getName())) {
			sender.sendMessage(pluginConfig.msg_noneed);
			return true;
		}
		if (args.length == 0) {
			sender.sendMessage(pluginConfig.msg_cantbenull);
			return true;
		}
		passwordHandler.checkPassword(p, args[0], false);
		return true;
	}
}
