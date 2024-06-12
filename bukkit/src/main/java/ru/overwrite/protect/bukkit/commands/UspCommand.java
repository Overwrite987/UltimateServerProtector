package ru.overwrite.protect.bukkit.commands;

import org.bukkit.command.*;
import ru.overwrite.protect.bukkit.PasswordHandler;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.commands.subcommands.*;
import ru.overwrite.protect.bukkit.utils.Config;

import java.util.*;

public class UspCommand implements CommandExecutor, TabCompleter {

	private final ServerProtectorManager plugin;
	private final Config pluginConfig;

	private final Map<String, SubCommand> subCommands = new HashMap<>();

	public UspCommand(ServerProtectorManager plugin) {
		this.plugin = plugin;
		pluginConfig = plugin.getPluginConfig();
		registerSub(new LogoutSubcommand(plugin));
		registerSub(new ReloadSubcommand(plugin));
		registerSub(new RebootSubcommand(plugin));
		registerSub(new EncryptSubcommand(plugin));
		registerSub(new SetpassSubcommand(plugin));
		registerSub(new AddopSubcommand(plugin));
		registerSub(new AddipSubcommand(plugin));
		registerSub(new RempassSubcommand(plugin));
		registerSub(new RemopSubcommand(plugin));
		registerSub(new RemipSubcommand(plugin));
	}

	private void registerSub(AbstractSubCommand subCmd) {
		subCommands.put(subCmd.getName(), subCmd);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			sendHelp(sender, label);
			return true;
		}
		SubCommand subCommand = subCommands.get(args[0].toLowerCase());
		if (subCommand != null) {
			return subCommand.execute(sender, label, args);
		}
		if (sender.hasPermission("serverprotector.protect")) {
			sendHelp(sender, label);
			return true;
		}
		sender.sendMessage("§6❖ §7Running §c§lUltimateServerProtector " + plugin.getDescription().getVersion()
				+ "§7 by §5OverwriteMC");
		return true;
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