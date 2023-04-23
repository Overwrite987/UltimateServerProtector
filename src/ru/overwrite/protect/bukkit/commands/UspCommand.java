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
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.utils.Config;

public class UspCommand implements CommandExecutor {
	
	private final ServerProtectorManager instance;
	private final Config pluginConfig;
	
	public UspCommand(ServerProtectorManager plugin) {
        this.instance = plugin;
        pluginConfig = plugin.getPluginConfig();
    }
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender.hasPermission("serverprotector.admin")) {
        	    if (pluginConfig.secure_settings_only_console_usp && !(sender instanceof ConsoleCommandSender)) {
        	    	sender.sendMessage(pluginConfig.uspmsg_consoleonly);
        	    	return false;
        	    }
                if (args.length == 0) {
                    sendHelp(sender, label);
                    return true;
                }
                if (args[0].equalsIgnoreCase("reload")) {
                	instance.reloadConfigs();
                    sender.sendMessage(pluginConfig.uspmsg_reloaded);
                    return true;
                }
                if (args[0].equalsIgnoreCase("reboot")) {
                	instance.reloadConfigs();
                    Bukkit.getScheduler().cancelTasks(instance);
                    instance.login.clear();
                    instance.ips.clear();
                    Runner runner = new Runner(instance);
                    runner.runTaskTimerAsynchronously(instance, 5L, 40L);
                    runner.startMSG();
                    if (pluginConfig.punish_settings_enable_time) {
                    	runner.startTimer();
                    }
                    if (pluginConfig.secure_settings_enable_notadmin_punish) {
                    	runner.adminCheck();
                    }
                    if (pluginConfig.secure_settings_enable_op_whitelist) {
                    	runner.startOpCheck();
                    }
                    if (pluginConfig.secure_settings_enable_permission_blacklist) {
                    	runner.startPermsCheck();
                    }
                    instance.checkForUpdates();
                    sender.sendMessage(pluginConfig.uspmsg_rebooted);
                    return true;
                }
            if (pluginConfig.main_settings_enable_admin_commands) {
            	FileConfiguration config = instance.getConfig();
                if (args[0].equalsIgnoreCase("setpass")) {
                	if (args.length > 1) {
                		Player targetPlayer = Bukkit.getPlayerExact(args[1]);
                		if (targetPlayer == null) {
                			sender.sendMessage(pluginConfig.uspmsg_playernotfound.replace("%nick%", args[1]));
                			return true;
                		}
                		String nickname = targetPlayer.getName();
                		if (instance.isAdmin(nickname)) {
                			sender.sendMessage(pluginConfig.uspmsg_alreadyinconfig);
                			return true;
                		}
                		if (args.length < 4) {
                			addAdmin(config, nickname, args[2]);
                			sender.sendMessage(pluginConfig.uspmsg_playeradded.replace("%nick%", nickname));
                			return true;
                		}
                	}
                	sender.sendMessage(pluginConfig.uspmsg_setpassusage.replace("%cmd%", label));
                	return true;
                }
                if (args[0].equalsIgnoreCase("addop")) {
                    if (args.length > 1) {
                    	Player targetPlayer = Bukkit.getPlayerExact(args[1]);
                    	if (targetPlayer == null) {
                    		sender.sendMessage(pluginConfig.uspmsg_playernotfound.replace("%nick%", args[1]));
                    		return true;
                    	}
                    	String nickname = targetPlayer.getName();
                        List<String> wl = pluginConfig.op_whitelist;
                        wl.add(nickname);
                        config.set("op-whitelist", wl);
                        instance.saveConfig();
                        sender.sendMessage(pluginConfig.uspmsg_playeradded.replace("%nick%", nickname));
                        return true;
                    }
                    sender.sendMessage(pluginConfig.uspmsg_addopusage.replace("%cmd%", label));
                    return true;
                }
                if (args[0].equalsIgnoreCase("addip")) {
                    if (args.length > 1 && args[1] != null) {
                        List<String> ipwl = pluginConfig.ip_whitelist;
                        ipwl.add(args[1]);
                        config.set("ip-whitelist", ipwl);
                        sender.sendMessage(pluginConfig.uspmsg_ipadded.replace("%nick%", args[1]));
                        return true;
                    }
                    sender.sendMessage(pluginConfig.uspmsg_addipusage.replace("%cmd%", label));
                    return true;
                }
                
                if (args[0].equalsIgnoreCase("rempass")) {
                	if (args.length > 1) {
                		if (!instance.isAdmin(args[1])) {
                			sender.sendMessage(pluginConfig.uspmsg_notinconfig);
                			return true;
                		}
                		if (args.length < 3) {
                			removeAdmin(config, args[1]);
                			sender.sendMessage(pluginConfig.uspmsg_playerremoved);
                			return true;
                		}
                	}
                    sender.sendMessage(pluginConfig.uspmsg_rempassusage.replace("%cmd%", label));
                    return true;
                }
                if (args[0].equalsIgnoreCase("remop")) {
                    if (args.length > 1) {
                    	Player targetPlayer = Bukkit.getPlayerExact(args[1]);
                    	if (targetPlayer == null) {
                    		sender.sendMessage(pluginConfig.uspmsg_playernotfound.replace("%nick%", args[1]));
                    		return true;
                    	}
                    	String nickname = targetPlayer.getName();
                        List<String> wl = pluginConfig.op_whitelist;
                        wl.remove(nickname);
                        config.set("op-whitelist", wl);
                        instance.saveConfig();
                        sender.sendMessage(pluginConfig.uspmsg_playerremoved.replace("%nick%", nickname));
                        return true;
                    }
                    sender.sendMessage(pluginConfig.uspmsg_remopusage.replace("%cmd%", label));
                    return true;
                }
                if (args[0].equalsIgnoreCase("remip")) {
                    if (args.length > 1 && args[1] != null) {
                        List<String> ipwl = pluginConfig.ip_whitelist;
                        ipwl.remove(args[1]);
                        config.set("ip-whitelist", ipwl);
                        sender.sendMessage(pluginConfig.uspmsg_ipremoved.replace("%nick%", args[1]));
                        return true;
                    }
                    sender.sendMessage(pluginConfig.uspmsg_remipusage.replace("%cmd%", label));
                    return true;
                }
                
            }
              sendHelp(sender, label);
              return true;
            } else {
                sender.sendMessage("§6❖ §7Running §c§lUltimateServerProtector v20§7 by §5OverwriteMC");
            }
        return true;
    }
    
	private void sendHelp(CommandSender sender, String label) {
		sender.sendMessage(pluginConfig.uspmsg_usage.replace("%cmd%", label));
		sender.sendMessage(pluginConfig.uspmsg_usage_reload.replace("%cmd%", label));
		sender.sendMessage(pluginConfig.uspmsg_usage_reboot.replace("%cmd%", label));
		if (!pluginConfig.main_settings_enable_admin_commands) {
			sender.sendMessage("§7Other commands are disabled.");
			sender.sendMessage("§7To enable it - set §6enable-admin-commands: §atrue");
		} else {
			sender.sendMessage(pluginConfig.uspmsg_usage_setpass.replace("%cmd%", label));
			sender.sendMessage(pluginConfig.uspmsg_usage_rempass.replace("%cmd%", label));
			sender.sendMessage(pluginConfig.uspmsg_usage_addop.replace("%cmd%", label));
			sender.sendMessage(pluginConfig.uspmsg_usage_remop.replace("%cmd%", label));
			sender.sendMessage(pluginConfig.uspmsg_usage_addip.replace("%cmd%", label));
			sender.sendMessage(pluginConfig.uspmsg_usage_remip.replace("%cmd%", label));
		}
	}

	public void addAdmin(FileConfiguration config, String nick, String pas) {
        FileConfiguration data;
        String datafile = config.getString("file-settings.data-file");
        if (instance.fullpath) {
        	data = pluginConfig.getFileFullPath(datafile);
        	data.set("data." + nick + ".pass", pas);
        	pluginConfig.saveFullPath(data, datafile);
        } else {
        	data = pluginConfig.getFile(datafile);
        	data.set("data." + nick + ".pass", pas);
        	pluginConfig.save(data, datafile);
        }
        instance.data = data;
    }
	
	public void removeAdmin(FileConfiguration config, String nick) {
		FileConfiguration data;
		String datafile = config.getString("file-settings.data-file");
        if (instance.fullpath) {
        	data = pluginConfig.getFileFullPath(datafile);
        	data.set("data." + nick + ".pass", null);
        	data.set("data." + nick, null);
        	pluginConfig.saveFullPath(data, datafile);
        } else {
        	data = pluginConfig.getFile(datafile);
        	data.set("data." + nick + ".pass", null);
        	data.set("data." + nick, null);
        	pluginConfig.save(data, datafile);
        }
        instance.data = data;
    }

}
