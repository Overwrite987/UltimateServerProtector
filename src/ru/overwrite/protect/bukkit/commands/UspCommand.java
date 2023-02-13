package ru.overwrite.protect.bukkit.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;

import ru.overwrite.protect.bukkit.Runner;
import ru.overwrite.protect.bukkit.ServerProtector;
import ru.overwrite.protect.bukkit.utils.Config;

public class UspCommand implements CommandExecutor {
	
	private final ServerProtector instance = ServerProtector.getInstance();
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        	 FileConfiguration config = instance.getConfig();
             if (sender.hasPermission("serverprotector.admin")) {
            	 
        	    if (config.getBoolean("secure-settings.only-console-usp") && !(sender instanceof ConsoleCommandSender)) {
        	    	sender.sendMessage(ServerProtector.getMessage("uspmsg.consoleonly"));
        	    	return false;
        	    }
                if (args.length == 0) {
                    sendHelp(sender, label);
                    return true;
                }
                if (args[0].equalsIgnoreCase("reload")) {
                	instance.reloadConfigs();
                    sender.sendMessage(ServerProtector.getMessage("uspmsg.reloaded"));
                    return true;
                }
                if (args[0].equalsIgnoreCase("reboot")) {
                	instance.reloadConfigs();
                    Bukkit.getScheduler().cancelTasks(instance);
                    instance.login.clear();
                    instance.ips.clear();
                    Runner runner = new Runner();
                    runner.runTaskTimerAsynchronously(instance, 5L, 40L);
                    runner.startMSG();
                    if (config.getBoolean("punish-settings.enable-time")) {
                    	runner.startTimer();
                    }
                    if (config.getBoolean("punish-settings.notadmin-punish")) {
                    	runner.adminCheck();
                    }
                    if (config.getBoolean("secure-settings.enable-op-whitelist")) {
                    	runner.startOpCheck();
                    }
                    instance.checkForUpdates();
                    sender.sendMessage(ServerProtector.getMessage("uspmsg.rebooted"));
                    return true;
                }
            if (config.getBoolean("main-settings.enable-admin-commands")) {
            	
                if (args[0].equalsIgnoreCase("setpass")) {
                	Player targetPlayer = Bukkit.getServer().getPlayer(args[1]);
                	if (targetPlayer == null) {
                		sender.sendMessage(ServerProtector.getMessage("uspmsg.playernotfound", s -> s.replace("%nick%", args[1])));
                		return true;
                	}
                    String nickname = targetPlayer.getName();
                    if (instance.isAdmin(nickname)) {
                        sender.sendMessage(ServerProtector.getMessage("uspmsg.alreadyinconfig"));
                        return true;
                    }
                    if (args.length < 4) {
                        addAdmin(nickname, args[2]);
                        sender.sendMessage(ServerProtector.getMessage("uspmsg.playeradded", s -> s.replace("%nick%", nickname)));
                        return true;
                    }
                    sender.sendMessage(ServerProtector.getMessage("uspmsg.setpassusage", s -> s.replace("%cmd%", label)));
                    return true;
                }
                if (args[0].equalsIgnoreCase("addop")) {
                    if (args.length > 1) {
                    	Player targetPlayer = Bukkit.getServer().getPlayer(args[1]);
                    	if (targetPlayer == null) {
                    		sender.sendMessage(ServerProtector.getMessage("uspmsg.playernotfound", s -> s.replace("%nick%", args[1])));
                    		return true;
                    	}
                    	String nickname = targetPlayer.getName();
                        List<String> wl = config.getStringList("op-whitelist");
                        wl.add(nickname);
                        config.set("op-whitelist", wl);
                        instance.saveConfig();
                        sender.sendMessage(ServerProtector.getMessage("uspmsg.playeradded", s -> s.replace("%nick%", nickname)));
                        return true;
                    }
                    sender.sendMessage(ServerProtector.getMessage("uspmsg.addopusage", s -> s.replace("%cmd%", label)));
                    return true;
                }
                if (args[0].equalsIgnoreCase("addip")) {
                    if (args.length > 1 && args[1] != null) {
                        List<String> ipwl = config.getStringList("ip-whitelist");
                        ipwl.add(args[1]);
                        config.set("ip-whitelist", ipwl);
                        sender.sendMessage(ServerProtector.getMessage("uspmsg.ipadded", s -> s.replace("%nick%", args[1])));
                        return true;
                    }
                    sender.sendMessage(ServerProtector.getMessage("uspmsg.addip", s -> s.replace("%cmd%", label)));
                    return true;
                }
            }
              sendHelp(sender, label);
              return true;
            } else {
                sender.sendMessage("§7This server is using §cUltimateServerProtector §7- the most powerful security plugin made by §5Overwrite");
            }
        return true;
    }
    
	private void sendHelp(CommandSender sender, String label) {
		FileConfiguration config = instance.getConfig();
		sender.sendMessage(ServerProtector.getMessage("uspmsg.usage", s -> s.replace("%cmd%", label)));
		sender.sendMessage(ServerProtector.getMessage("uspmsg.usage-reload", s -> s.replace("%cmd%", label)));
		sender.sendMessage(ServerProtector.getMessage("uspmsg.usage-reboot", s -> s.replace("%cmd%", label)));
		if (!config.getBoolean("main-settings.enable-admin-commands")) {
			sender.sendMessage("§7Other commands are disabled.");
			sender.sendMessage("§7To enable them, set §6enable-admin-commands: §atrue");
		} else {
			sender.sendMessage(ServerProtector.getMessage("uspmsg.usage-setpass", s -> s.replace("%cmd%", label)));
			sender.sendMessage(ServerProtector.getMessage("uspmsg.usage-addop", s -> s.replace("%cmd%", label)));
			sender.sendMessage(ServerProtector.getMessage("uspmsg.usage-addip", s -> s.replace("%cmd%", label)));
		}
	}

    public void addAdmin(String nick, String pas) {
        FileConfiguration data;
        FileConfiguration config = instance.getConfig();
        if (ServerProtector.fullpath) {
        	data = Config.getFileFullPath(config.getString("file-settings.data-file"));
        	data.set("data." + nick + ".pass", pas);
        	Config.saveFullPath(data, config.getString("file-settings.data-file"));
        } else {
        	data = Config.getFile(config.getString("file-settings.data-file"));
        	data.set("data." + nick + ".pass", pas);
        	Config.save(data, config.getString("file-settings.data-file"));
        }
    }

}
