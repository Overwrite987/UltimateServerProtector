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
            List<String> ipwl = pluginConfig.getAccessData().ipWhitelist().get(args[1]);
            if (ipwl.isEmpty()) {
                sender.sendMessage(pluginConfig.getUspMessages().playerNotFound().replace("%nick%", args[1]));
            }
            ipwl.add(args[2]);
            plugin.getConfig().set("ip-whitelist." + args[1], ipwl);
            plugin.saveConfig();
            sender.sendMessage(pluginConfig.getUspMessages().ipAdded().replace("%nick%", args[1]).replace("%ip%", args[2]));
            return true;
        }
        sendCmdUsage(sender, pluginConfig.getUspMessages().addIpUsage(), label);
        return true;
    }
}
