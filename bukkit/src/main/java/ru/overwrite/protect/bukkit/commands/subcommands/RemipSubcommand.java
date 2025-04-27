package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.command.CommandSender;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.configuration.data.UspMessages;

import java.util.List;

public class RemipSubcommand extends AbstractSubCommand {

    public RemipSubcommand(ServerProtectorManager plugin) {
        super(plugin, "remip", "serverprotector.remip", true);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        UspMessages uspMessages = pluginConfig.getUspMessages();
        if (args.length > 2 && (args[1] != null && args[2] != null)) {
            String nickname = args[1];
            List<String> ipwl = pluginConfig.getAccessData().ipWhitelist().get(nickname);
            if (ipwl == null || ipwl.isEmpty()) {
                sender.sendMessage(uspMessages.playerNotFound().replace("%nick%", nickname));
                return true;
            }
            List<String> ips = List.of(args).subList(2, args.length);
            ipwl.removeAll(ips);
            plugin.getConfig().set("ip-whitelist." + nickname, ipwl);
            plugin.saveConfig();
            sender.sendMessage(uspMessages.ipRemoved().replace("%nick%", nickname).replace("%ip%", ips.toString()));
            return true;
        }
        sendCmdUsage(sender, uspMessages.remIpUsage(), label);
        return true;
    }
}
