package ru.overwrite.protect.bukkit;

import com.google.common.collect.ImmutableList;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import java.util.Date;

public final class ServerProtector extends ServerProtectorManager {
	
	private final ImmutableList<String> forceshutdown = ImmutableList.of("PlugMan", "PlugManX", "PluginManager", "ServerUtils");

	@Override
	public void onEnable() {
		long startTime = System.currentTimeMillis();
		saveDefaultConfig();
		final FileConfiguration config = getConfig();
		setupLogger(config);
		setupProxy(config);
		loadConfigs(config);
		PluginManager pluginManager = server.getPluginManager();
		if (!isSafe(messageFile, pluginManager)) {
			return;
		}
		paper = checkPaper(messageFile);
		registerListeners(pluginManager);
		registerCommands(pluginManager, config);
		startTasks(config);
		logEnableDisable(messageFile.getString("log-format.enabled"), new Date(startTime));
		if (config.getBoolean("main-settings.enable-metrics")) {
			new Metrics(this, 13347);
		}
		checkForUpdates(config, messageFile);
		long endTime = System.currentTimeMillis();
		getPluginLogger().info("Plugin started in " + (endTime - startTime) + " ms");
	}

	@Override
	public void onDisable() {
		if (messageFile != null) {
			logEnableDisable(messageFile.getString("log-format.disabled"), new Date());
		}
		final FileConfiguration config = getConfig();
		if (config.getBoolean("message-settings.enable-broadcasts")) {
			for (Player ps : server.getOnlinePlayers()) {
				if (ps.hasPermission("serverprotector.admin") && messageFile != null) {
					ps.sendMessage(getPluginConfig().getMessage(messageFile.getConfigurationSection("broadcasts"),
							"disabled"));
				}
			}
		}
		getRunner().cancelTasks();
		if (proxy) {
			server.getMessenger().unregisterOutgoingPluginChannel(this);
			server.getMessenger().unregisterIncomingPluginChannel(this);
		}
		if (config.getBoolean("secure-settings.shutdown-on-disable")) {
			if (!config.getBoolean("secure-settings.shutdown-on-disable-only-if-plugman")) {
				server.shutdown();
				return;
			}
			PluginManager pluginManager = server.getPluginManager();
			for (String s : forceshutdown) {
				if (pluginManager.isPluginEnabled(s)) {
					server.shutdown();
				}
			}
		}
	}
}