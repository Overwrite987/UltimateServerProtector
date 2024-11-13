package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.utils.Utils;

public class SetpassSubcommand extends AbstractSubCommand {

    public SetpassSubcommand(ServerProtectorManager plugin) {
        super(plugin, "setpass", "serverprotector.setpass", true);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length > 1) {
            OfflinePlayer targetPlayer = Bukkit.getOfflinePlayerIfCached(args[1]);
            if (targetPlayer == null) {
                sender.sendMessage(pluginConfig.getUspMessages().playerNotFound().replace("%nick%", args[1]));
                return true;
            }
            String nickname = targetPlayer.getName();
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
        if (!pluginConfig.getEncryptionSettings().enableEncryption()) {
            dataFile.set("data." + nick + ".pass", pas);
        } else if (pluginConfig.getEncryptionSettings().autoEncryptPasswords()) {
            String encryptedPas = Utils.encryptPassword(pas, Utils.generateSalt(pluginConfig.getEncryptionSettings().saltLength()), pluginConfig.getEncryptionSettings().encryptMethods());
            dataFile.set("data." + nick + ".encrypted-pass", encryptedPas);
        } else {
            dataFile.set("data." + nick + ".encrypted-pass", pas);
        }
        pluginConfig.save(plugin.getDataFilePath(), dataFile, plugin.getDataFileName());
        plugin.setDataFile(dataFile);
    }
}
