package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import ru.overwrite.protect.bukkit.ServerProtectorManager;

import java.util.List;

public class RemopSubcommand extends AbstractSubCommand {

    public RemopSubcommand(ServerProtectorManager plugin) {
        super(plugin, "remop", "serverprotector.remop", true);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length > 1) {
            OfflinePlayer targetPlayer = Bukkit.getOfflinePlayerIfCached(args[1]);
            if (targetPlayer == null) {
                sender.sendMessage(pluginConfig.uspmsg_playernotfound.replace("%nick%", args[1]));
                return true;
            }
            String nickname = targetPlayer.getName();
            List<String> wl = pluginConfig.op_whitelist;
            wl.remove(nickname);
            plugin.getConfig().set("op-whitelist", wl);
            plugin.saveConfig();
            sender.sendMessage(pluginConfig.uspmsg_playerremoved.replace("%nick%", nickname));
            return true;
        }
        sendCmdUsage(sender, pluginConfig.uspmsg_remopusage, label);
        return true;
    }
}