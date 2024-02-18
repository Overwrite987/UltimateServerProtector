package ru.overwrite.protect.bukkit.task;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.api.ServerProtectorCaptureEvent;
import ru.overwrite.protect.bukkit.utils.Config;
import ru.overwrite.protect.bukkit.utils.Utils;

import java.util.Date;

public final class TaskManager {
	private final ServerProtectorManager plugin;
	private final ServerProtectorAPI api;
	private final Config config;
	private final Runner runner;

	public TaskManager(ServerProtectorManager plugin) {
		this.plugin = plugin;
		this.api = plugin.getPluginAPI();
		this.config = plugin.getPluginConfig();
		this.runner = plugin.getRunner();
	}

	public void startMainCheck() {
		runner.runPeriodicalAsync(() -> {
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (plugin.isExcluded(p, config.excluded_admin_pass)) {
					continue;
				}
				if (api.isCaptured(p)) {
					continue;
				}
				if (!plugin.isPermissions(p)) {
					continue;
				}
				String playerName = p.getName();
				if (!api.isAuthorised(p)) {
					ServerProtectorCaptureEvent captureEvent = new ServerProtectorCaptureEvent(p, Utils.getIp(p));
					captureEvent.callEvent();
					if (captureEvent.isCancelled()) {
						continue;
					}
					api.capturePlayer(p);
					if (config.sound_settings_enable_sounds) {
						Utils.sendSound(config.sound_settings_on_capture, p);
					}
					if (config.effect_settings_enable_effects) {
						plugin.giveEffect(p);
					}
					if (config.logging_settings_logging_pas) {
						plugin.logAction("log-format.captured", p, new Date());
					}
					if (config.message_settings_enable_broadcasts) {
						String msg = config.broadcasts_captured.replace("%player%", playerName).replace("%ip%",
								Utils.getIp(p));
						plugin.sendAlert(p, msg);
					}
					if (config.message_settings_enable_console_broadcasts) {
						String msg = config.broadcasts_captured.replace("%player%", playerName).replace("%ip%",
								Utils.getIp(p));
						Bukkit.getConsoleSender().sendMessage(msg);
					}
				}
			}
		}, 5L, 40L);
	}

	public void startAdminCheck(FileConfiguration config) {
		runner.runPeriodicalAsync(() -> {
			if (plugin.login.isEmpty())
				return;
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (api.isCaptured(p) && !plugin.isAdmin(p.getName())) {
					plugin.checkFail(p.getName(), config.getStringList("commands.not-in-config"));
				}
			}
		}, 0L, 20L);
	}

	public void startCapturesMessages(FileConfiguration config) {
		runner.runPeriodicalAsync(() -> {
			if (plugin.login.isEmpty())
				return;
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (api.isCaptured(p)) {
					p.sendMessage(this.config.msg_message);
					if (this.config.message_settings_send_title) {
						Utils.sendTitleMessage(this.config.titles_message, p);
					}
				}
			}
		}, 0L, config.getInt("message-settings.delay") * 20L);
	}

	public void startOpCheck(FileConfiguration config) {
		runner.runPeriodicalAsync(() -> {
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.isOp() && !this.config.op_whitelist.contains(p.getName())) {
					plugin.checkFail(p.getName(), config.getStringList("commands.not-in-opwhitelist"));
				}
			}
		}, 0L, 20L);
	}

	public void startPermsCheck(FileConfiguration config) {
		runner.runPeriodicalAsync(() -> {
			for (Player p : Bukkit.getOnlinePlayers()) {
				for (String badperms : this.config.blacklisted_perms) {
					if (p.hasPermission(badperms) && !plugin.isExcluded(p, this.config.excluded_blacklisted_perms)) {
						plugin.checkFail(p.getName(), config.getStringList("commands.have-blacklisted-perm"));
					}
				}
			}
		}, 5L, 20L);
	}

	public void startCapturesTimer(FileConfiguration config) {
		runner.runPeriodicalAsync(() -> {
			if (plugin.login.isEmpty())
				return;
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (api.isCaptured(p)) {
					String playerName = p.getName();
					if (!plugin.time.containsKey(playerName)) {
						plugin.time.put(playerName, 0);
						if (this.config.bossbar_settings_enable_bossbar) {
							Utils.bossbar = Bukkit.createBossBar(
									this.config.bossbar_message.replace("%time%",
											Integer.toString(this.config.punish_settings_time)),
									BarColor.valueOf(this.config.bossbar_settings_bar_color),
									BarStyle.valueOf(this.config.bossbar_settings_bar_style));
							Utils.bossbar.addPlayer(p);
						}
					} else {
						int currentTime = plugin.time.get(playerName);
						plugin.time.put(playerName, currentTime + 1);
						int newTime = plugin.time.get(playerName);
						if (this.config.bossbar_settings_enable_bossbar && Utils.bossbar != null) {
							Utils.bossbar.setTitle(this.config.bossbar_message.replace("%time%",
									Integer.toString(this.config.punish_settings_time - newTime)));
							double percents = (this.config.punish_settings_time - newTime)
									/ (double) this.config.punish_settings_time;
							if (percents > 0) {
								Utils.bossbar.setProgress(percents);
								Utils.bossbar.addPlayer(p);
							} else {
								plugin.loggerInfo(String.valueOf(currentTime));
								plugin.loggerInfo(String.valueOf(this.config.punish_settings_time));
								plugin.loggerInfo(String.valueOf(newTime));
							}
						}
					}
					if (!noTimeLeft(playerName) && this.config.punish_settings_enable_time) {
						plugin.checkFail(playerName, config.getStringList("commands.failed-time"));
						Utils.bossbar.removePlayer(p);
					}
				}
			}
		}, 0L, 20L);
	}

	private boolean noTimeLeft(String playerName) {
		return !plugin.time.containsKey(playerName) || plugin.time.get(playerName) < config.punish_settings_time;
	}
}
