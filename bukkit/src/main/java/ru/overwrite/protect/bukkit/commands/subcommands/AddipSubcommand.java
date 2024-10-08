package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.command.CommandSender;
import ru.overwrite.protect.bukkit.ServerProtectorManager;

import java.util.List;

public class AddipSubcommand extends AbstractSubCommand {

    public AddipSubcommand(ServerProtectorManager plugin) {
        super(plugin, "addip", "serverprotector.addip", true);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
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
        sendCmdUsage(sender, pluginConfig.uspmsg_addipusage, label);
        return true;
    }
}