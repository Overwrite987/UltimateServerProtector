package ru.overwrite.protect.folia.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;

import ru.overwrite.protect.folia.Runner;
import ru.overwrite.protect.folia.ServerProtector;
import ru.overwrite.protect.folia.ServerProtectorManager;
import ru.overwrite.protect.folia.utils.Config;

public class UspCommand implements CommandExecutor {
	
	private final ServerProtector instance = ServerProtector.getInstance();
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender.hasPermission("serverprotector.admin")) {
            	FileConfiguration config = instance.getConfig();
        	    if (Config.secure_settings_only_console_usp && !(sender instanceof ConsoleCommandSender)) {
        	    	sender.sendMessage(Config.uspmsg_consoleonly);
        	    	return false;
        	    }
                if (args.length == 0) {
                    sendHelp(sender, label);
                    return true;
                }
                if (args[0].equalsIgnoreCase("reload")) {
                	instance.reloadConfigs();
                    sender.sendMessage(Config.uspmsg_reloaded);
                    return true;
                }
                if (args[0].equalsIgnoreCase("reboot")) {
                	instance.reloadConfigs();
                	Bukkit.getGlobalRegionScheduler().cancelTasks(instance);
                	Bukkit.getAsyncScheduler().cancelTasks(instance);
                    instance.login.clear();
                    instance.ips.clear();
                    Runner runner = new Runner();
                    runner.run();
                    runner.startMSG();
                    if (Config.punish_settings_enable_time) {
                    	runner.startTimer();
                    }
                    if (Config.secure_settings_enable_notadmin_punish) {
                    	runner.adminCheck();
                    }
                    if (Config.secure_settings_enable_op_whitelist) {
                    	runner.startOpCheck();
                    }
                    if (Config.secure_settings_enable_permission_blacklist) {
                    	runner.startPermsCheck();
                    }
                    instance.checkForUpdates();
                    sender.sendMessage(Config.uspmsg_rebooted);
                    return true;
                }
            if (Config.main_settings_enable_admin_commands) {
            	
                if (args[0].equalsIgnoreCase("setpass")) {
                	Player targetPlayer = Bukkit.getPlayerExact(args[1]);
                	if (targetPlayer == null) {
                		sender.sendMessage(Config.uspmsg_playernotfound.replace("%nick%", args[1]));
                		return true;
                	}
                    String nickname = targetPlayer.getName();
                    if (instance.isAdmin(nickname)) {
                        sender.sendMessage(Config.uspmsg_alreadyinconfig);
                        return true;
                    }
                    if (args.length < 4) {
                        addAdmin(nickname, args[2]);
                        sender.sendMessage(Config.uspmsg_playeradded.replace("%nick%", nickname));
                        return true;
                    }
                    sender.sendMessage(Config.uspmsg_setpassusage.replace("%cmd%", label));
                    return true;
                }
                if (args[0].equalsIgnoreCase("addop")) {
                    if (args.length > 1) {
                    	Player targetPlayer = Bukkit.getPlayerExact(args[1]);
                    	if (targetPlayer == null) {
                    		sender.sendMessage(Config.uspmsg_playernotfound.replace("%nick%", args[1]));
                    		return true;
                    	}
                    	String nickname = targetPlayer.getName();
                        List<String> wl = config.getStringList("op-whitelist");
                        wl.add(nickname);
                        config.set("op-whitelist", wl);
                        instance.saveConfig();
                        sender.sendMessage(Config.uspmsg_playeradded.replace("%nick%", nickname));
                        return true;
                    }
                    sender.sendMessage(Config.uspmsg_addopusage.replace("%cmd%", label));
                    return true;
                }
                if (args[0].equalsIgnoreCase("addip")) {
                    if (args.length > 1 && args[1] != null) {
                        List<String> ipwl = config.getStringList("ip-whitelist");
                        ipwl.add(args[1]);
                        config.set("ip-whitelist", ipwl);
                        sender.sendMessage(Config.uspmsg_ipadded.replace("%nick%", args[1]));
                        return true;
                    }
                    sender.sendMessage(Config.uspmsg_addipusage.replace("%cmd%", label));
                    return true;
                }
            }
              sendHelp(sender, label);
              return true;
            } else {
                sender.sendMessage("§6❖ §7Running §c§lUltimateServerProtector v19§7 by §5OverwriteMC");
            }
        return true;
    }
    
	private void sendHelp(CommandSender sender, String label) {
		sender.sendMessage(Config.uspmsg_usage.replace("%cmd%", label));
		sender.sendMessage(Config.uspmsg_usage_reload.replace("%cmd%", label));
		sender.sendMessage(Config.uspmsg_usage_reboot.replace("%cmd%", label));
		if (!Config.main_settings_enable_admin_commands) {
			sender.sendMessage("§7Прочие команды отключены.");
			sender.sendMessage("§7Для их включения выставьте §6enable-admin-commands: §atrue");
		} else {
			sender.sendMessage(Config.uspmsg_usage_setpass.replace("%cmd%", label));
			sender.sendMessage(Config.uspmsg_usage_addop.replace("%cmd%", label));
			sender.sendMessage(Config.uspmsg_usage_addip.replace("%cmd%", label));
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
        ServerProtectorManager.data = data;
    }

}
