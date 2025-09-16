package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.command.CommandSender;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.configuration.data.UspMessages;

import java.util.List;

public class RemopSubcommand extends AbstractSubCommand {

    public RemopSubcommand(ServerProtectorManager plugin) {
        super(plugin, "remop", "serverprotector.remop", true);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        UspMessages uspMessages = pluginConfig.getUspMessages();
        if (args.length > 1) {
            String nickname = args[1];
            List<String> wl = pluginConfig.getAccessData().opWhitelist();
            if (!wl.remove(nickname)) {
                sender.sendMessage(uspMessages.playerNotFound().replace("%nick%", nickname));
                return true;
            }
            plugin.getConfig().set("op-whitelist", wl);
            plugin.saveConfig();
            sender.sendMessage(uspMessages.playerRemoved().replace("%nick%", nickname));
            return true;
        }
        sendCmdUsage(sender, uspMessages.remOpUsage(), label);
        return true;
    }
}
