package ru.overwrite.protect.bukkit.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;

import ru.overwrite.protect.bukkit.PasswordHandler;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.utils.Config;
import ru.overwrite.protect.bukkit.utils.Utils;

public class UspCommand implements CommandExecutor, TabCompleter {

	private final ServerProtectorManager instance;
	private final PasswordHandler passwordHandler;
	private final Config pluginConfig;

	public UspCommand(ServerProtectorManager plugin) {
		instance = plugin;
		pluginConfig = plugin.getPluginConfig();
		passwordHandler = plugin.getPasswordHandler();
	}

	@Override
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
			FileConfiguration config = instance.getConfig();
			switch (args[0].toLowerCase()) {
				case ("reload"): {
					instance.reloadConfigs(config);
					sender.sendMessage(pluginConfig.uspmsg_reloaded);
					return true;
				}
				case ("reboot"): {
					instance.reloadConfigs(config);
					if (Utils.FOLIA) {
						Bukkit.getAsyncScheduler().cancelTasks(instance);
					} else {
						Bukkit.getScheduler().cancelTasks(instance);
					}
					instance.time.clear();
					instance.login.clear();
					instance.ips.clear();
					instance.saved.clear();
					if (Utils.bossbar != null) {
						Utils.bossbar.removeAll();
					}
					passwordHandler.attempts.clear();
					instance.startRunners(config);
					instance.checkForUpdates(config);
					sender.sendMessage(pluginConfig.uspmsg_rebooted);
					return true;
				}
			}
			if (pluginConfig.main_settings_enable_admin_commands) {
				switch (args[0].toLowerCase()) {
					case ("setpass"): {
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
						sendCmdMessage(sender, pluginConfig.uspmsg_setpassusage, label);
						return true;
					}
					case ("addop"): {
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
						sendCmdMessage(sender, pluginConfig.uspmsg_addopusage, label);
						return true;
					}
					case ("addip"): {
						if (args.length > 2 && (args[1] != null && args[2] != null)) {
							List<String> ipwl = pluginConfig.ip_whitelist.get(args[1]);
							if (ipwl.isEmpty()) {
								sender.sendMessage(pluginConfig.uspmsg_playernotfound.replace("%nick%", args[1]));
							}
							ipwl.add(args[2]);
							config.set("ip-whitelist." + args[1], ipwl);
							instance.saveConfig();
							sender.sendMessage(pluginConfig.uspmsg_ipadded.replace("%nick%", args[1]).replace("%ip%", args[1]));
							return true;
						}
						sendCmdMessage(sender, pluginConfig.uspmsg_addipusage, label);
						return true;
					}
					case ("rempass"): {
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
						sendCmdMessage(sender, pluginConfig.uspmsg_rempassusage, label);
						return true;
					}
					case ("remop"): {
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
						sendCmdMessage(sender, pluginConfig.uspmsg_remopusage, label);
						return true;
					}
					case ("remip"): {
						if (args.length > 2 && (args[1] != null && args[2] != null)) {
							List<String> ipwl = pluginConfig.ip_whitelist.get(args[1]);
							if (ipwl.isEmpty()) {
								sender.sendMessage(pluginConfig.uspmsg_playernotfound.replace("%nick%", args[1]));
							}
							ipwl.remove(args[2]);
							config.set("ip-whitelist." + args[1], ipwl);
							instance.saveConfig();
							sender.sendMessage(pluginConfig.uspmsg_ipremoved.replace("%nick%", args[1]).replace("%ip%", args[1]));
							return true;
						}
						sendCmdMessage(sender, pluginConfig.uspmsg_remipusage, label);
						return true;
					}
				}
			}
			sendHelp(sender, label);
			return true;
		} else {
			sender.sendMessage("§6❖ §7Running §c§lUltimateServerProtector " + instance.getDescription().getVersion()
					+ "§7 by §5OverwriteMC");
		}
		return true;
	}
	
	
	private void addAdmin(FileConfiguration config, String nick, String pas) {
		FileConfiguration data;
		String datafile = config.getString("file-settings.data-file");
		String path = instance.path;
		data = pluginConfig.getFile(path, datafile);
		data.set("data." + nick + ".pass", pas);
		pluginConfig.save(path, data, datafile);
		data = instance.data;
	}

	private void removeAdmin(FileConfiguration config, String nick) {
		FileConfiguration data;
		String datafile = config.getString("file-settings.data-file");
		String path = instance.path;
		data = pluginConfig.getFile(path, datafile);
		data.set("data." + nick + ".pass", null);
		data.set("data." + nick, null);
		pluginConfig.save(path, data, datafile);
		data = instance.data;
	}

	private void sendHelp(CommandSender sender, String label) {
		sendCmdMessage(sender, pluginConfig.uspmsg_usage, label);
		sendCmdMessage(sender, pluginConfig.uspmsg_usage_reload, label);
		sendCmdMessage(sender, pluginConfig.uspmsg_usage_reboot, label);
		if (!pluginConfig.main_settings_enable_admin_commands) {
			sender.sendMessage("§7Other commands are disabled.");
			sender.sendMessage("§7To enable them, set §6enable-admin-commands: §atrue");
		} else {
			sendCmdMessage(sender, pluginConfig.uspmsg_usage_setpass, label);
			sendCmdMessage(sender, pluginConfig.uspmsg_usage_rempass, label);
			sendCmdMessage(sender, pluginConfig.uspmsg_usage_addop, label);
			sendCmdMessage(sender, pluginConfig.uspmsg_usage_remop, label);
			sendCmdMessage(sender, pluginConfig.uspmsg_usage_addip, label);
			sendCmdMessage(sender, pluginConfig.uspmsg_usage_remip, label);
		}
	}

	private void sendCmdMessage(CommandSender sender, String msg, String label) {
		sender.sendMessage(msg.replace("%cmd%", label));
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
		List<String> result = new ArrayList<>();
		for (String c : completions) {
			if (c.toLowerCase().startsWith(args[0].toLowerCase()))
				result.add(c); 
		}
		return result;
	}
}