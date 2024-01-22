package ru.overwrite.protect.bukkit;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import ru.overwrite.protect.bukkit.utils.Metrics;
import ru.overwrite.protect.bukkit.utils.Utils;

public final class ServerProtector extends ServerProtectorManager {
	
	private final List<String> forceshutdown = Arrays.asList(new String[] { "PlugMan", "PlugManX", "PluginManager", "ServerUtils" });

	@Override
	public void onEnable() {
		long startTime = System.currentTimeMillis();
		if (!isPaper()) {
			return;
		}
		saveDefaultConfig();
		FileConfiguration config = getConfig();
		setupProxy(config);
		PluginManager pluginManager = server.getPluginManager();
		if (!isSafe(pluginManager)) {
			return;
		}
		loadConfigs(config);
		registerListeners(pluginManager);
		registerCommands(pluginManager, config);
		startRunners(config);
		setupLogger(config);
		logEnableDisable(messageFile.getString("log-format.enabled"), new Date(startTime));
		if (config.getBoolean("main-settings.enable-metrics")) {
			new Metrics(this, 13347);
		}
		checkForUpdates(config);
		long endTime = System.currentTimeMillis();
		loggerInfo("Plugin started in " + (endTime - startTime) + " ms");
	}

	@Override
	public void onDisable() {
		if (messageFile != null) {
			logEnableDisable(messageFile.getString("log-format.disabled"), new Date());
		}
		if (Utils.bossbar != null) {
			Utils.bossbar.removeAll();
		}
		FileConfiguration config = getConfig();
		if (config.getBoolean("message-settings.enable-broadcasts")) {
			for (Player ps : server.getOnlinePlayers()) {
				if (ps.hasPermission("serverprotector.admin") && messageFile != null) {
					ps.sendMessage(getPluginConfig().getMessage(messageFile.getConfigurationSection("broadcasts"),
							"disabled"));
				}
			}
		}
		if (Utils.FOLIA) {
			server.getAsyncScheduler().cancelTasks(this);
		} else {
			server.getScheduler().cancelTasks(this);
		}
		if (proxy) {
			server.getMessenger().unregisterOutgoingPluginChannel(this);
			server.getMessenger().unregisterIncomingPluginChannel(this);
		}
		if (config.getBoolean("secure-settings.shutdown-on-disable")) {
			if (!config.getBoolean("secure-settings.shutdown-on-disable-only-if-plugman")) {
				server.shutdown();
			} else {
				PluginManager pluginManager = server.getPluginManager();
				for (String s : forceshutdown) {
					if (pluginManager.isPluginEnabled(s)) {
						server.shutdown();
					}
				}
			}
		}
	}
}