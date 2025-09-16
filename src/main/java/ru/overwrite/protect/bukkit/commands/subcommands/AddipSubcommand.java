package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.command.CommandSender;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.configuration.data.UspMessages;

import java.util.List;

public class AddipSubcommand extends AbstractSubCommand {

    public AddipSubcommand(ServerProtectorManager plugin) {
        super(plugin, "addip", "serverprotector.addip", true);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        UspMessages uspMessages = pluginConfig.getUspMessages();
        if (args.length > 2) {
            String nickname = args[1];
            List<String> ipwl = pluginConfig.getAccessData().ipWhitelist().get(nickname);
            if (ipwl == null || ipwl.isEmpty()) {
                sender.sendMessage(uspMessages.playerNotFound().replace("%nick%", nickname));
                return true;
            }
            List<String> ips = List.of(args).subList(2, args.length);
            ipwl.addAll(ips);
            plugin.getConfig().set("ip-whitelist." + nickname, ipwl);
            plugin.saveConfig();
            sender.sendMessage(uspMessages.ipAdded().replace("%nick%", nickname).replace("%ip%", ips.toString()));
            return true;
        }
        sendCmdUsage(sender, uspMessages.addIpUsage(), label);
        return true;
    }
}
