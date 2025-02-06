package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.configuration.data.EncryptionSettings;
import ru.overwrite.protect.bukkit.utils.Utils;

public class SetpassSubcommand extends AbstractSubCommand {

    public SetpassSubcommand(ServerProtectorManager plugin) {
        super(plugin, "setpass", "serverprotector.setpass", true);
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
            if (plugin.isAdmin(nickname)) {
                sender.sendMessage(pluginConfig.getUspMessages().alreadyInConfig());
                return true;
            }
            if (args.length < 4) {
                addAdmin(nickname, args[2]);
                sender.sendMessage(pluginConfig.getUspMessages().playerAdded().replace("%nick%", nickname));
                return true;
            }
        }
        sendCmdUsage(sender, pluginConfig.getUspMessages().setPassUsage(), label);
        return true;
    }

    private void addAdmin(String nick, String pas) {
        FileConfiguration dataFile = pluginConfig.getFile(plugin.getDataFilePath(), plugin.getDataFileName());
        EncryptionSettings encryptionSettings = pluginConfig.getEncryptionSettings();
        if (!encryptionSettings.enableEncryption()) {
            dataFile.set("data." + nick + ".pass", pas);
        } else if (encryptionSettings.autoEncryptPasswords()) {
            String encryptedPas = Utils.encryptPassword(pas, Utils.generateSalt(encryptionSettings.saltLength()), encryptionSettings.encryptMethods());
            dataFile.set("data." + nick + ".encrypted-pass", encryptedPas);
        } else {
            dataFile.set("data." + nick + ".encrypted-pass", pas);
        }
        pluginConfig.save(plugin.getDataFilePath(), dataFile, plugin.getDataFileName());
        plugin.setDataFile(dataFile);
    }
}
