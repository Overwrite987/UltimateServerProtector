package ru.overwrite.protect.bukkit.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ru.overwrite.protect.bukkit.PasswordHandler;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.api.ServerProtectorLogoutEvent;
import ru.overwrite.protect.bukkit.utils.Config;
import ru.overwrite.protect.bukkit.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UspCommand implements CommandExecutor, TabCompleter {

	private final ServerProtectorManager instance;
	private final ServerProtectorAPI api;
	private final PasswordHandler passwordHandler;
	private final Config pluginConfig;

	public UspCommand(ServerProtectorManager plugin) {
		instance = plugin;
		api = plugin.getPluginAPI();
		pluginConfig = plugin.getPluginConfig();
		passwordHandler = plugin.getPasswordHandler();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			sendHelp(sender, label);
			return true;
		}
		FileConfiguration config = instance.getConfig();
		switch (args[0].toLowerCase()) {
			case ("logout"): {
				if (!(sender instanceof Player)) {
					sender.sendMessage(pluginConfig.uspmsg_playeronly);
					return false;
				}
				Player p = (Player)sender;
				if (!p.hasPermission("serverprotector.protect")) {
					sendHelp(sender, label);
					return false;
				}
				if (api.isAuthorised(p)) {
					instance.getRunner().run(() -> {
						new ServerProtectorLogoutEvent(p, Utils.getIp(p)).callEvent();
						api.deauthorisePlayer(p);
					});
					p.kickPlayer(pluginConfig.uspmsg_logout);
					return true;
				}
				break;
			}
			case ("reload"): {
				if (pluginConfig.secure_settings_only_console_usp && !(sender instanceof ConsoleCommandSender)) {
					sender.sendMessage(pluginConfig.uspmsg_consoleonly);
					return false;
				}
				if (!sender.hasPermission("serverprotector.reload")) {
					sendHelp(sender, label);
					return false;
				}
				instance.reloadConfigs();
				sender.sendMessage(pluginConfig.uspmsg_reloaded);
				return true;
			}
			case ("reboot"): {
				if (pluginConfig.secure_settings_only_console_usp && !(sender instanceof ConsoleCommandSender)) {
					sender.sendMessage(pluginConfig.uspmsg_consoleonly);
					return false;
				}
				if (!sender.hasPermission("serverprotector.reboot")) {
					return false;
				}
				instance.getRunner().cancelTasks();
				instance.reloadConfigs();
				FileConfiguration newconfig = instance.getConfig();
				instance.time.clear();
				instance.login.clear();
				api.ips.clear();
				api.saved.clear();
				if (Utils.bossbar != null) {
					Utils.bossbar.removeAll();
				}
				passwordHandler.attempts.clear();
				instance.startTasks(newconfig);
				sender.sendMessage(pluginConfig.uspmsg_rebooted);
				return true;
			}
			case ("encrypt"): {
				if (pluginConfig.secure_settings_only_console_usp && !(sender instanceof ConsoleCommandSender)) {
					sender.sendMessage(pluginConfig.uspmsg_consoleonly);
					return false;
				}
				if (!sender.hasPermission("serverprotector.encrypt")) {
					return false;
				}
				if (pluginConfig.encryption_settings_enable_encryption && args.length == 2) {
					sender.sendMessage(Utils.encryptPassword(true, args[1], pluginConfig.encryption_settings_encrypt_methods));
					return true;
				}
				break;
			}
			case ("setpass"): {
				if (pluginConfig.secure_settings_only_console_usp && !(sender instanceof ConsoleCommandSender)) {
					sender.sendMessage(pluginConfig.uspmsg_consoleonly);
					return false;
				}
				if (!sender.hasPermission("serverprotector.setpass")) {
					return false;
				}
				if (args.length > 1) {
					OfflinePlayer targetPlayer = Bukkit.getOfflinePlayerIfCached(args[1]);
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
						addAdmin(nickname, args[2]);
						sender.sendMessage(pluginConfig.uspmsg_playeradded.replace("%nick%", nickname));
						return true;
					}
				}
				sendCmdMessage(sender, pluginConfig.uspmsg_setpassusage, label);
				return true;
			}
			case ("addop"): {
				if (pluginConfig.secure_settings_only_console_usp && !(sender instanceof ConsoleCommandSender)) {
					sender.sendMessage(pluginConfig.uspmsg_consoleonly);
					return false;
				}
				if (!sender.hasPermission("serverprotector.addop")) {
					return false;
				}
				if (args.length > 1) {
					OfflinePlayer targetPlayer = Bukkit.getOfflinePlayerIfCached(args[1]);
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
				if (pluginConfig.secure_settings_only_console_usp && !(sender instanceof ConsoleCommandSender)) {
					sender.sendMessage(pluginConfig.uspmsg_consoleonly);
					return false;
				}
				if (!sender.hasPermission("serverprotector.addip")) {
					return false;
				}
				if (args.length > 2 && (args[1] != null && args[2] != null)) {
					List<String> ipwl = pluginConfig.ip_whitelist.get(args[1]);
					if (ipwl.isEmpty()) {
						sender.sendMessage(pluginConfig.uspmsg_playernotfound.replace("%nick%", args[1]));
					}
					ipwl.add(args[2]);
					config.set("ip-whitelist." + args[1], ipwl);
					instance.saveConfig();
					sender.sendMessage(pluginConfig.uspmsg_ipadded.replace("%nick%", args[1]).replace("%ip%", args[2]));
					return true;
				}
				sendCmdMessage(sender, pluginConfig.uspmsg_addipusage, label);
				return true;
			}
			case ("rempass"): {
				if (pluginConfig.secure_settings_only_console_usp && !(sender instanceof ConsoleCommandSender)) {
					sender.sendMessage(pluginConfig.uspmsg_consoleonly);
					return false;
				}
				if (!sender.hasPermission("serverprotector.rempass")) {
					return false;
				}
				if (args.length > 1) {
					if (!instance.isAdmin(args[1])) {
						sender.sendMessage(pluginConfig.uspmsg_notinconfig);
						return true;
					}
					if (args.length < 3) {
						removeAdmin(args[1]);
						sender.sendMessage(pluginConfig.uspmsg_playerremoved);
						return true;
					}
				}
				sendCmdMessage(sender, pluginConfig.uspmsg_rempassusage, label);
				return true;
			}
			case ("remop"): {
				if (pluginConfig.secure_settings_only_console_usp && !(sender instanceof ConsoleCommandSender)) {
					sender.sendMessage(pluginConfig.uspmsg_consoleonly);
					return false;
				}
				if (!sender.hasPermission("serverprotector.remop")) {
					break;
				}
				if (args.length > 1) {
					OfflinePlayer targetPlayer = Bukkit.getOfflinePlayerIfCached(args[1]);
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
				if (pluginConfig.secure_settings_only_console_usp && !(sender instanceof ConsoleCommandSender)) {
					sender.sendMessage(pluginConfig.uspmsg_consoleonly);
					return false;
				}
				if (!sender.hasPermission("serverprotector.remip")) {
					break;
				}
				if (args.length > 2 && (args[1] != null && args[2] != null)) {
					List<String> ipwl = pluginConfig.ip_whitelist.get(args[1]);
					if (ipwl.isEmpty()) {
						sender.sendMessage(pluginConfig.uspmsg_playernotfound.replace("%nick%", args[1]));
					}
					ipwl.remove(args[2]);
					config.set("ip-whitelist." + args[1], ipwl);
					instance.saveConfig();
					sender.sendMessage(pluginConfig.uspmsg_ipremoved.replace("%nick%", args[1]).replace("%ip%", args[2]));
					return true;
				}
				sendCmdMessage(sender, pluginConfig.uspmsg_remipusage, label);
				return true;
			}
		}
		if (sender.hasPermission("serverprotector.protect")) {
			sendHelp(sender, label);
			return true;
		}
		sender.sendMessage("§6❖ §7Running §c§lUltimateServerProtector " + instance.getDescription().getVersion()
				+ "§7 by §5OverwriteMC");
		return true;
	}

	private void addAdmin(String nick, String pas) {
		FileConfiguration dataFile;
		dataFile = pluginConfig.getFile(instance.path, instance.dataFileName);
		if (!pluginConfig.encryption_settings_enable_encryption) {
			dataFile.set("data." + nick + ".pass", pas);
		} else {
			String encryptedPas = Utils.encryptPassword(false, pas, pluginConfig.encryption_settings_encrypt_methods);
			dataFile.set("data." + nick + ".encrypted-pass", encryptedPas);
		}
		pluginConfig.save(instance.path, dataFile, instance.dataFileName);
		instance.dataFile = dataFile;
	}

	private void removeAdmin(String nick) {
		FileConfiguration dataFile;
		dataFile = pluginConfig.getFile(instance.path, instance.dataFileName);
		if (!pluginConfig.encryption_settings_enable_encryption) {
			dataFile.set("data." + nick + ".pass", null);
			dataFile.set("data." + nick, null);
		} else {
			dataFile.set("data." + nick + ".encrypted-pass", null);
		}
		dataFile.set("data." + nick, null);
		pluginConfig.save(instance.path, dataFile, instance.dataFileName);
		instance.dataFile = dataFile;
	}

	private void sendHelp(CommandSender sender, String label) {
		sendCmdMessage(sender, pluginConfig.uspmsg_usage, label, "serverprotector.protect");
		sendCmdMessage(sender, pluginConfig.uspmsg_usage_logout, label, "serverprotector.protect");
		if (!sender.hasPermission("serverprotector.admin")) {
			return;
		}
		sendCmdMessage(sender, pluginConfig.uspmsg_usage_reload, label, "serverprotector.reload");
		sendCmdMessage(sender, pluginConfig.uspmsg_usage_reboot, label, "serverprotector.reboot");
		if (pluginConfig.encryption_settings_enable_encryption) {
			sendCmdMessage(sender, pluginConfig.uspmsg_usage_encrypt, label, "serverprotector.encrypt");
		}
		if (!pluginConfig.main_settings_enable_admin_commands) {
			sender.sendMessage(pluginConfig.uspmsg_otherdisabled);
			return;
		}
		sendCmdMessage(sender, pluginConfig.uspmsg_usage_setpass, label, "serverprotector.setpass");
		sendCmdMessage(sender, pluginConfig.uspmsg_usage_rempass, label, "serverprotector.rempass");
		sendCmdMessage(sender, pluginConfig.uspmsg_usage_addop, label, "serverprotector.addop");
		sendCmdMessage(sender, pluginConfig.uspmsg_usage_remop, label, "serverprotector.remop");
		sendCmdMessage(sender, pluginConfig.uspmsg_usage_addip, label, "serverprotector.addip");
		sendCmdMessage(sender, pluginConfig.uspmsg_usage_remip, label, "serverprotector.remip");
	}

	private void sendCmdMessage(CommandSender sender, String msg, String label, String permission) {
		if (sender.hasPermission(permission)) {
			sender.sendMessage(msg.replace("%cmd%", label));
		}
	}

	private void sendCmdMessage(CommandSender sender, String msg, String label) {
		sender.sendMessage(msg.replace("%cmd%", label));
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (pluginConfig.secure_settings_only_console_usp && !(sender instanceof ConsoleCommandSender)) {
			return Collections.emptyList();
		}
		List<String> completions = new ArrayList<>();
		if (args.length == 1) {
			completions.add("logout");
			completions.add("reload");
			completions.add("reboot");
			if (pluginConfig.encryption_settings_enable_encryption) {
				completions.add("encrypt");
			}
			if (pluginConfig.main_settings_enable_admin_commands) {
				completions.add("setpass");
				completions.add("rempass");
				completions.add("addop");
				completions.add("remop");
				completions.add("addip");
				completions.add("remip");
			}
		}
		List<String> result = new ArrayList<>();
		for (String c : completions) {
			if (c.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
				result.add(c);
		}
		return result;
	}
}