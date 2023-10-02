package ru.overwrite.protect.bukkit;

import java.util.Date;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import ru.overwrite.protect.bukkit.utils.Metrics;
import ru.overwrite.protect.bukkit.utils.Utils;

public final class ServerProtector extends ServerProtectorManager {

	@Override
	public void onEnable() {
		long startTime = System.currentTimeMillis();
		checkPaper();
		saveDefaultConfig();
		FileConfiguration config = getConfig();
		setupProxy(config);
		loadConfigs(config);
		PluginManager pluginManager = server.getPluginManager();
		registerListeners(pluginManager);
		registerCommands(pluginManager, config);
		startRunners(config);
		setupLogger(config);
		logEnableDisable(message.getString("log-format.enabled"), new Date(startTime));
		if (config.getBoolean("main-settings.enable-metrics")) {
			new Metrics(this, 13347);
		}
		checkForUpdates(config);
		long endTime = System.currentTimeMillis();
		loggerInfo("Plugin started in " + (endTime - startTime) + " ms");
	}

	@Override
	public void onDisable() {
		logEnableDisable(message.getString("log-format.disabled"), new Date());
		if (Utils.bossbar != null) {
			Utils.bossbar.removeAll();
		}
		if (getConfig().getBoolean("message-settings.enable-broadcasts")) {
			for (Player ps : server.getOnlinePlayers()) {
				if (ps.hasPermission("serverprotector.admin")) {
					ps.sendMessage(getMessage(message.getConfigurationSection("broadcasts"), "disabled"));
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
		if (getConfig().getBoolean("secure-settings.shutdown-on-disable")) {
			server.shutdown();
		}
	}
}