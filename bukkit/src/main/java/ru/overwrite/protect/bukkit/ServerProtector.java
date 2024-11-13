package ru.overwrite.protect.bukkit;

import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import java.time.LocalDateTime;

public final class ServerProtector extends ServerProtectorManager {

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        saveDefaultConfig();
        final FileConfiguration config = getConfig();
        setupLogger(config);
        setupProxy(config);
        loadConfigs(config);
        PluginManager pluginManager = server.getPluginManager();
        checkSafe(pluginManager);
        checkPaper();
        registerListeners(pluginManager);
        registerCommands(pluginManager, config);
        startTasks(config);
        logEnableDisable(getPluginConfig().getLogFormats().enabled(), LocalDateTime.now());
        if (config.getBoolean("main-settings.enable-metrics")) {
            new Metrics(this, 13347);
        }
        checkForUpdates(config);
        long endTime = System.currentTimeMillis();
        getPluginLogger().info("Plugin started in " + (endTime - startTime) + " ms");
    }

    @Override
    public void onDisable() {
        if (getMessageFile() != null) {
            logEnableDisable(getPluginConfig().getLogFormats().disabled(), LocalDateTime.now());
        }
        final FileConfiguration config = getConfig();
        if (config.getBoolean("message-settings.enable-broadcasts")) {
            for (Player ps : server.getOnlinePlayers()) {
                if (ps.hasPermission("serverprotector.admin") && getMessageFile() != null) {
                    ps.sendMessage(getPluginConfig().getBroadcasts().disabled());
                }
            }
        }
        getRunner().cancelTasks();
        if (isProxy()) {
            server.getMessenger().unregisterOutgoingPluginChannel(this);
            server.getMessenger().unregisterIncomingPluginChannel(this);
        }
        if (config.getBoolean("secure-settings.shutdown-on-disable")) {
            server.shutdown();
        }
    }
}
