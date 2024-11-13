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
                sender.sendMessage(pluginConfig.getUspMessages().playerNotFound().replace("%nick%", args[1]));
                return true;
            }
            String nickname = targetPlayer.getName();
            List<String> wl = pluginConfig.getAccessData().opWhitelist();
            wl.remove(nickname);
            plugin.getConfig().set("op-whitelist", wl);
            plugin.saveConfig();
            sender.sendMessage(pluginConfig.getUspMessages().playerRemoved().replace("%nick%", nickname));
            return true;
        }
        sendCmdUsage(sender, pluginConfig.getUspMessages().remOpUsage(), label);
        return true;
    }
}
