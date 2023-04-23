package ru.overwrite.protect.bukkit.commands;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;

import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.utils.Config;

public class UspTabCompleter implements TabCompleter {
	
	private final Config pluginConfig;
	
	public UspTabCompleter(ServerProtectorManager plugin) {
        pluginConfig = plugin.getPluginConfig();
    }

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (!sender.hasPermission("serverprotector.admin")) {
	        return Collections.emptyList();
	    }
		if (pluginConfig.secure_settings_only_console_usp && !(sender instanceof ConsoleCommandSender)) {
	    	return Collections.emptyList();
	    }
	    List<String> completions = new ArrayList<>();
	    if (args.length == 1) {
	        completions.add("reload");
	        completions.add("reboot");
	        if (pluginConfig.main_settings_enable_admin_commands) {
	            completions.add("setpass");
	            completions.add("addop");
	            completions.add("addip");
	            completions.add("rempass");
	        }
	    }
	    return completions;
	}
}
