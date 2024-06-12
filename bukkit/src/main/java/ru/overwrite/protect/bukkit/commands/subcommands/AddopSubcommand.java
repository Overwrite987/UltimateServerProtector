package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import ru.overwrite.protect.bukkit.ServerProtectorManager;

import java.util.List;

public class AddopSubcommand extends AbstractSubCommand {

    public AddopSubcommand(ServerProtectorManager plugin) {
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
        if (!sender.hasPermission("serverprotector.addop")) {
            sendHelp(sender, label);
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
            plugin.getConfig().set("op-whitelist", wl);
            plugin.saveConfig();
            sender.sendMessage(pluginConfig.uspmsg_playeradded.replace("%nick%", nickname));
            return true;
        }
        sendCmdMessage(sender, pluginConfig.uspmsg_addopusage, label);
        return true;
    }
}
