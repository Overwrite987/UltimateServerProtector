package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.utils.Utils;

import java.util.List;

public class AddopSubcommand extends AbstractSubCommand {

    public AddopSubcommand(ServerProtectorManager plugin) {
        super(plugin, "addop", "serverprotector.addop", true);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length > 1) {
            String nickname = args[1];

            if (plugin.isPaper() && Utils.SUB_VERSION >= 16) {
                OfflinePlayer targetPlayer = Bukkit.getOfflinePlayerIfCached(nickname);
                if (targetPlayer == null) {
                    sender.sendMessage(pluginConfig.getUspMessages().playerNotFound().replace("%nick%", nickname));
                    return true;
                }
                nickname = targetPlayer.getName();
            }

            List<String> whitelist = pluginConfig.getAccessData().opWhitelist();
            whitelist.add(nickname);
            plugin.getConfig().set("op-whitelist", whitelist);
            plugin.saveConfig();
            sender.sendMessage(pluginConfig.getUspMessages().playerAdded().replace("%nick%", nickname));
            return true;
        }

        sendCmdUsage(sender, pluginConfig.getUspMessages().addOpUsage(), label);
        return true;
    }
}
