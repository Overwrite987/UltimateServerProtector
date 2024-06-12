package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.utils.Utils;

public class EncryptSubcommand extends AbstractSubCommand {

    public String getName() {
        return "encrypt";
    }

    public EncryptSubcommand(ServerProtectorManager plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (pluginConfig.secure_settings_only_console_usp && !(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(pluginConfig.uspmsg_consoleonly);
            return false;
        }
        if (!sender.hasPermission("serverprotector.encrypt")) {
            sendHelp(sender, label);
            return false;
        }
        if (pluginConfig.encryption_settings_enable_encryption && args.length == 2) {
            sender.sendMessage(Utils.encryptPassword(args[1], Utils.generateSalt(pluginConfig.encryption_settings_salt_length), pluginConfig.encryption_settings_encrypt_methods));
            return true;
        }
        return false;
    }
}
