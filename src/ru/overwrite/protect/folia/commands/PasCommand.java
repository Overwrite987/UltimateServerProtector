package ru.overwrite.protect.folia.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.overwrite.protect.folia.ServerProtector;
import ru.overwrite.protect.folia.utils.Config;

public class PasCommand implements CommandExecutor {
	
	private final ServerProtector instance = ServerProtector.getInstance();
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			Bukkit.getLogger().info(Config.msg_playeronly);
			return true;
		}
		
		Player p = (Player)sender;
		if (!instance.login.contains(p.getName())) {
			sender.sendMessage(Config.msg_noneed);
			return true;
		}
		
		if (args.length == 0) {
			sender.sendMessage(Config.msg_cantbenull);
			return true;
		}
		
		instance.passwordHandler.checkPassword(p, args[0], false);
		return true;
	}
}
