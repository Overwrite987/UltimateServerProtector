package ru.overwrite.protect.bukkit;

import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.messaging.Messenger;

import java.time.LocalDateTime;

public final class ServerProtector extends ServerProtectorManager {

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        saveDefaultConfig();
        final FileConfiguration config = getConfig();
        final ConfigurationSection mainSettings = config.getConfigurationSection("main-settings");
        setupLogger(config);
        setupProxy(config);
        loadConfigs(config);
        PluginManager pluginManager = server.getPluginManager();
        checkSafe(pluginManager);
        checkPaper();
        registerListeners(pluginManager);
        registerCommands(pluginManager, mainSettings);
        startTasks(config);
        logEnableDisable(getPluginConfig().getLogMessages().enabled(), LocalDateTime.now());
        if (mainSettings.getBoolean("enable-metrics", true)) {
            new Metrics(this, 13347);
        }
        checkForUpdates(mainSettings);
        long endTime = System.currentTimeMillis();
        getPluginLogger().info("Plugin started in " + (endTime - startTime) + " ms");
    }

    @Override
    public void onDisable() {
        if (getMessageFile() != null) {
            logEnableDisable(getPluginConfig().getLogMessages().disabled(), LocalDateTime.now());
        }
        if (getPluginConfig().getMessageSettings().enableBroadcasts()) {
            for (Player onlinePlayer : server.getOnlinePlayers()) {
                if (onlinePlayer.hasPermission("serverprotector.admin") && getMessageFile() != null) {
                    onlinePlayer.sendMessage(getPluginConfig().getBroadcasts().disabled());
                }
            }
        }
        getRunner().cancelTasks();
        if (getPluginMessage() != null) {
            Messenger messenger = server.getMessenger();
            messenger.unregisterOutgoingPluginChannel(this);
            messenger.unregisterIncomingPluginChannel(this);
        }
        if (getConfig().getBoolean("secure-settings.shutdown-on-disable")) {
            server.shutdown();
        }
    }
}
