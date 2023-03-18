package ru.overwrite.protect.bukkit.commands;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;

import ru.overwrite.protect.bukkit.ServerProtector;

public class UspTabCompleter implements TabCompleter {
	
	private final ServerProtector instance = ServerProtector.getInstance();

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (!sender.hasPermission("serverprotector.admin")) {
	        return Collections.emptyList();
	    }
		FileConfiguration config = instance.getConfig();
		if (config.getBoolean("secure-settings.only-console-usp") && !(sender instanceof ConsoleCommandSender)) {
	    	return Collections.emptyList();
	    }
	    List<String> completions = new ArrayList<>();
	    if (args.length == 1) {
	        completions.add("reload");
	        completions.add("reboot");
	        if (config.getBoolean("main-settings.enable-admin-commands")) {
	            completions.add("setpass");
	            completions.add("addop");
	            completions.add("addip");
	        }
	    }
	    return completions;
	}
}
