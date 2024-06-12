package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import ru.overwrite.protect.bukkit.ServerProtectorManager;

import java.util.List;

public class AddipSubcommand extends AbstractSubCommand {

    public String getName() {
        return "addip";
    }

    public AddipSubcommand(ServerProtectorManager plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (pluginConfig.secure_settings_only_console_usp && !(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(pluginConfig.uspmsg_consoleonly);
            return false;
        }
        if (!pluginConfig.main_settings_enable_admin_commands) {
            sendHelp(sender, label);
            return false;
        }
        if (!sender.hasPermission("serverprotector.addip")) {
            sendHelp(sender, label);
            return false;
        }
        if (args.length > 2 && (args[1] != null && args[2] != null)) {
            List<String> ipwl = pluginConfig.ip_whitelist.get(args[1]);
            if (ipwl.isEmpty()) {
                sender.sendMessage(pluginConfig.uspmsg_playernotfound.replace("%nick%", args[1]));
            }
            ipwl.add(args[2]);
            plugin.getConfig().set("ip-whitelist." + args[1], ipwl);
            plugin.saveConfig();
            sender.sendMessage(pluginConfig.uspmsg_ipadded.replace("%nick%", args[1]).replace("%ip%", args[2]));
            return true;
        }
        sendCmdMessage(sender, pluginConfig.uspmsg_addipusage, label);
        return true;
    }
}
