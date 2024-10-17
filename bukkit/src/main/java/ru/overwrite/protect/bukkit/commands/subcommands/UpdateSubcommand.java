package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class UpdateSubcommand extends AbstractSubCommand {

    public UpdateSubcommand(ServerProtectorManager plugin) {
        super(plugin, "update", "serverprotector.update", true);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        checkAndUpdatePlugin(sender, plugin);
        return true;
    }

    public void checkAndUpdatePlugin(CommandSender sender, ServerProtectorManager plugin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Utils.checkUpdates(plugin, version -> {
                sender.sendMessage(plugin.messageFile.getString("system.baseline-default", "§6========================================"));

                String currentVersion = plugin.getDescription().getVersion();

                if (currentVersion.equals(version)) {
                    sender.sendMessage(plugin.messageFile.getString("system.update-latest", "§aYou are using latest version of the plugin!"));
                } else {
                    String currentJarName = new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
                    String downloadUrl = "https://github.com/Overwrite987/UltimateServerProtector/releases/download/" + version + "/" + "UltimateServerProtector.jar";
                    try {
                        File updateFolder = Bukkit.getUpdateFolderFile();
                        File targetFile = new File(updateFolder, currentJarName);

                        downloadFile(downloadUrl, targetFile);

                        sender.sendMessage(plugin.messageFile.getString("system.update-success-1", "§aUpdate was downloaded successfully!"));
                        sender.sendMessage(plugin.messageFile.getString("system.update-success-2", "§aRestart the server to apply the update."));
                    } catch (IOException ex) {
                        sender.sendMessage("Unable to download update: " + ex.getMessage());
                    }
                }
                sender.sendMessage(plugin.messageFile.getString("system.baseline-default", "§6========================================"));
            });
        });
    }

    private void downloadFile(String urlString, File destination) throws IOException {
        URL url = new URL(urlString);
        try (InputStream in = url.openStream()) {
            Files.copy(in, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
