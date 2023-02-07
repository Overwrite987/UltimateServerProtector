package ru.overwrite.protect.bukkit.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import ru.overwrite.protect.bukkit.ServerProtector;

public class PasCommand implements CommandExecutor {
	
	private final ServerProtector instance = ServerProtector.getInstance();
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		FileConfiguration config = instance.getConfig();
		if (!cmd.getName().equalsIgnoreCase(config.getString("main-settings.pas-command"))) return true;
		if (!(sender instanceof Player)) {
			Bukkit.getLogger().info(ServerProtector.getMessage("msg.playeronly"));
			return true;
		}
		Player p = (Player)sender;
		if (!instance.login.containsKey(p.getPlayer())) {
			sender.sendMessage(ServerProtector.getMessage("msg.noneed"));
			return true;
		}
		
		if (args.length == 0) {
			sender.sendMessage(ServerProtector.getMessage("msg.cantbenull"));
			return true;
		}
		
		instance.passwordHandler.checkPassword(p, args[0], false);
		return true;
	}
}
