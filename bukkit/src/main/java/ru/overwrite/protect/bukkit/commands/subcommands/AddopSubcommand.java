package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import ru.overwrite.protect.bukkit.ServerProtectorManager;

import java.util.List;

public class AddopSubcommand extends AbstractSubCommand {

    public AddopSubcommand(ServerProtectorManager plugin) {
        super(plugin, "addop", "serverprotector.addop", true);
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
            wl.add(nickname);
            plugin.getConfig().set("op-whitelist", wl);
            plugin.saveConfig();
            sender.sendMessage(pluginConfig.getUspMessages().playerAdded().replace("%nick%", nickname));
            return true;
        }
        sendCmdUsage(sender, pluginConfig.getUspMessages().addOpUsage(), label);
        return true;
    }
}