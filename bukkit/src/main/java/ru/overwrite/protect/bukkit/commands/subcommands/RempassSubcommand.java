package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import ru.overwrite.protect.bukkit.ServerProtectorManager;

public class RempassSubcommand extends AbstractSubCommand {

    public RempassSubcommand(ServerProtectorManager plugin) {
        super(plugin, "rempass", "serverprotector.rempass", true);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (pluginConfig.secure_settings_only_console_usp && !(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(pluginConfig.uspmsg_consoleonly);
            return false;
        }
        if (args.length > 1) {
            if (!plugin.isAdmin(args[1]) && !plugin.isAdmin(pluginConfig.geyser_prefix+args[1])) {
                sender.sendMessage(pluginConfig.uspmsg_notinconfig);
                return true;
            }
            if (args.length < 3) {
                removeAdmin(args[1]);
                sender.sendMessage(pluginConfig.uspmsg_playerremoved);
                return true;
            }
        }
        sendCmdUsage(sender, pluginConfig.uspmsg_rempassusage, label);
        return true;
    }

    private void removeAdmin(String nick) {
        FileConfiguration dataFile;
        dataFile = pluginConfig.getFile(plugin.path, plugin.dataFileName);
        if (!pluginConfig.encryption_settings_enable_encryption) {
            dataFile.set("data." + nick + ".pass", null);
            dataFile.set("data." + nick, null);
        } else {
            dataFile.set("data." + nick + ".encrypted-pass", null);
        }
        dataFile.set("data." + nick, null);
        pluginConfig.save(plugin.path, dataFile, plugin.dataFileName);
        plugin.dataFile = dataFile;
    }
}
