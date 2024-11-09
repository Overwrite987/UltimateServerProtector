package ru.overwrite.protect.bukkit;

import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import java.time.LocalDateTime;
import java.util.List;

public final class ServerProtector extends ServerProtectorManager {

    private final List<String> forceShutdown = List.of("PlugMan", "PlugManX", "PluginManager", "ServerUtils");

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        saveDefaultConfig();
        final FileConfiguration config = getConfig();
        setupLogger(config);
        setupProxy(config);
        loadConfigs(config);
        PluginManager pluginManager = server.getPluginManager();
        if (!isSafe(getMessageFile(), pluginManager)) {
            return;
        }
        checkPaper(getMessageFile());
        registerListeners(pluginManager);
        registerCommands(pluginManager, config);
        startTasks(config);
        logEnableDisable(getMessageFile().getString("log-format.enabled"), LocalDateTime.now());
        if (config.getBoolean("main-settings.enable-metrics")) {
            new Metrics(this, 13347);
        }
        checkForUpdates(config, getMessageFile());
        long endTime = System.currentTimeMillis();
        getPluginLogger().info("Plugin started in " + (endTime - startTime) + " ms");
    }

    @Override
    public void onDisable() {
        if (getMessageFile() != null) {
            logEnableDisable(getMessageFile().getString("log-format.disabled"), LocalDateTime.now());
        }
        final FileConfiguration config = getConfig();
        if (config.getBoolean("message-settings.enable-broadcasts")) {
            for (Player ps : server.getOnlinePlayers()) {
                if (ps.hasPermission("serverprotector.admin") && getMessageFile() != null) {
                    ps.sendMessage(getPluginConfig().getMessage(
                            getMessageFile().getConfigurationSection("broadcasts"), "disabled"));
                }
            }
        }
        getRunner().cancelTasks();
        if (isProxy()) {
            server.getMessenger().unregisterOutgoingPluginChannel(this);
            server.getMessenger().unregisterIncomingPluginChannel(this);
        }
        if (config.getBoolean("secure-settings.shutdown-on-disable")) {
            if (!config.getBoolean("secure-settings.shutdown-on-disable-only-if-plugman")) {
                server.shutdown();
                return;
            }
            PluginManager pluginManager = server.getPluginManager();
            for (String plugin : forceShutdown) {
                if (pluginManager.isPluginEnabled(plugin)) {
                    server.shutdown();
                }
            }
        }
    }
}