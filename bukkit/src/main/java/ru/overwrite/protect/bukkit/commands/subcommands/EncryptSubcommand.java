package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.command.CommandSender;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.utils.Utils;

public class EncryptSubcommand extends AbstractSubCommand {

    public EncryptSubcommand(ServerProtectorManager plugin) {
        super(plugin, "encrypt", "serverprotector.encrypt", false);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (pluginConfig.encryption_settings_enable_encryption && args.length == 2) {
            sender.sendMessage(
                    Utils.encryptPassword(args[1],
                    Utils.generateSalt(pluginConfig.encryption_settings_salt_length),
                    pluginConfig.encryption_settings_encrypt_methods));
            return true;
        }
        return false;
    }
}