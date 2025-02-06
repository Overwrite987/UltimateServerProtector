package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.configuration.data.SystemMessages;
import ru.overwrite.protect.bukkit.utils.Utils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

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
        plugin.getRunner().runAsync(() -> Utils.checkUpdates(plugin, version -> {
            SystemMessages systemMessages = pluginConfig.getSystemMessages();
            sender.sendMessage(systemMessages.baselineDefault());

            String currentVersion = plugin.getDescription().getVersion();

            if (currentVersion.equals(version)) {
                sender.sendMessage(systemMessages.updateLatest());
            } else {
                String currentJarName = new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
                String downloadUrl = "https://github.com/Overwrite987/UltimateServerProtector/releases/download/" + version + "/" + "UltimateServerProtector.jar";
                try {
                    File updateFolder = Bukkit.getUpdateFolderFile();
                    File targetFile = new File(updateFolder, currentJarName);

                    downloadFile(downloadUrl, targetFile, sender);

                    sender.sendMessage(systemMessages.updateSuccess1());
                    sender.sendMessage(pluginConfig.getSystemMessages().updateSuccess2());
                } catch (IOException ex) {
                    sender.sendMessage("Unable to download update: " + ex.getMessage());
                }
            }
            sender.sendMessage(systemMessages.baselineDefault());
        }));
    }

    public void downloadFile(String fileURL, File targetFile, CommandSender sender) throws IOException {
        URL url = new URL(fileURL);
        URLConnection connection = url.openConnection();
        int fileSize = connection.getContentLength();

        try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
             FileOutputStream out = new FileOutputStream(targetFile)) {

            byte[] data = new byte[1024];
            int bytesRead;
            int totalBytesRead = 0;
            int lastPercentage = 0;

            while ((bytesRead = in.read(data, 0, 1024)) != -1) {
                out.write(data, 0, bytesRead);
                totalBytesRead += bytesRead;
                int progressPercentage = (int) ((double) totalBytesRead / fileSize * 100);

                if (progressPercentage >= lastPercentage + 10) {
                    lastPercentage = progressPercentage;
                    int downloadedKB = totalBytesRead / 1024;
                    int fullSizeKB = fileSize / 1024;
                    sender.sendMessage(downloadedKB + "/" + fullSizeKB + "KB) (" + progressPercentage + "%)");
                }
            }
        }
    }
}
