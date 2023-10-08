package ru.overwrite.protect.bukkit.checker;

import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.Runner;
import ru.overwrite.protect.bukkit.utils.Config;
import ru.overwrite.protect.bukkit.utils.Utils;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.api.ServerProtectorCaptureEvent;

public class BukkitRunner implements Runner {

	private final ServerProtectorManager instance;
	private final ServerProtectorAPI api;
	private final Config pluginConfig;

	public BukkitRunner(ServerProtectorManager plugin) {
		instance = plugin;
		api = plugin.getPluginAPI();
		pluginConfig = plugin.getPluginConfig();
	}

	public void mainCheck() {
		(new BukkitRunnable() {
			public void run() {
				for (Player p : Bukkit.getOnlinePlayers()) {
					if (instance.isExcluded(p, pluginConfig.excluded_admin_pass)) {
						continue;
					}
					if (api.isCaptured(p)) {
						continue;
					}
					if (!instance.isPermissions(p)) {
						continue;
					}
					String playerName = p.getName();
					if (!instance.ips.contains(playerName + Utils.getIp(p)) && !instance.isAuthorised(p)) {
						ServerProtectorCaptureEvent captureEvent = new ServerProtectorCaptureEvent(p);
						captureEvent.callEvent();
						if (captureEvent.isCancelled()) {
							continue;
						}
						api.capturePlayer(p);
						if (pluginConfig.sound_settings_enable_sounds) {
							p.playSound(p.getLocation(), Sound.valueOf(pluginConfig.sound_settings_on_capture),
									pluginConfig.sound_settings_volume, pluginConfig.sound_settings_pitch);
						}
						if (pluginConfig.effect_settings_enable_effects) {
							instance.giveEffect(instance, p);
						}
						if (pluginConfig.logging_settings_logging_pas) {
							instance.logAction("log-format.captured", p, new Date());
						}
						if (pluginConfig.message_settings_enable_broadcasts) {
							String msg = pluginConfig.broadcasts_captured.replace("%player%", playerName).replace("%ip%",
									Utils.getIp(p));
							instance.sendAlert(p, msg);
						}
						if (pluginConfig.message_settings_enable_console_broadcasts) {
							String msg = pluginConfig.broadcasts_captured.replace("%player%", playerName).replace("%ip%",
									Utils.getIp(p));
							Bukkit.getConsoleSender().sendMessage(msg);
						}
					}
				}
			}
		}).runTaskTimerAsynchronously(instance, 5L, 40L);
	}

	public void adminCheck(FileConfiguration config) {
		(new BukkitRunnable() {
			public void run() {
				if (instance.login.isEmpty())
					return;
				for (Player p : Bukkit.getOnlinePlayers()) {
					if (api.isCaptured(p) && !instance.isAdmin(p.getName())) {
						instance.checkFail(p.getName(), config.getStringList("commands.not-in-config"));
					}
				}
			}
		}).runTaskTimerAsynchronously(instance, 5L, 20L);
	}

	public void startMSG(FileConfiguration config) {
		(new BukkitRunnable() {
			public void run() {
				if (instance.login.isEmpty())
					return;
				for (Player p : Bukkit.getOnlinePlayers()) {
					if (api.isCaptured(p)) {
						p.sendMessage(pluginConfig.msg_message);
						if (pluginConfig.message_settings_send_title) {
							p.sendTitle(pluginConfig.titles_title, pluginConfig.titles_subtitle,
									pluginConfig.titles_fadeIn, pluginConfig.titles_stay, pluginConfig.titles_fadeOut);
							return;
						}
					}
				}
			}
		}).runTaskTimerAsynchronously(instance, 0L, config.getInt("message-settings.delay") * 20L);
	}

	public void startOpCheck(FileConfiguration config) {
		(new BukkitRunnable() {
			public void run() {
				for (Player p : Bukkit.getOnlinePlayers()) {
					if (p.isOp() && !pluginConfig.op_whitelist.contains(p.getName())) {
						instance.checkFail(p.getName(), config.getStringList("commands.not-in-opwhitelist"));
					}
				}
			}
		}).runTaskTimerAsynchronously(instance, 5L, 20L);
	}

	public void startPermsCheck(FileConfiguration config) {
		(new BukkitRunnable() {
			public void run() {
				for (Player p : Bukkit.getOnlinePlayers()) {
					for (String badperms : pluginConfig.blacklisted_perms) {
						if (p.hasPermission(badperms)
								&& !instance.isExcluded(p, pluginConfig.excluded_blacklisted_perms)) {
							instance.checkFail(p.getName(), config.getStringList("commands.have-blacklisted-perm"));
						}
					}
				}
			}
		}).runTaskTimerAsynchronously(instance, 5L, 20L);
	}

	public void startTimer(FileConfiguration config) {
		(new BukkitRunnable() {
			public void run() {
				if (instance.login.isEmpty())
					return;
				for (Player p : Bukkit.getOnlinePlayers()) {
					if (api.isCaptured(p)) {
						String playerName = p.getName();
						if (!instance.time.containsKey(playerName)) {
							instance.time.put(playerName, 0);
							if (pluginConfig.bossbar_settings_enable_bossbar) {
								Utils.bossbar = Bukkit.createBossBar(
										pluginConfig.bossbar_message.replace("%time%",
												Integer.toString(pluginConfig.punish_settings_time)),
										BarColor.valueOf(pluginConfig.bossbar_settings_bar_color),
										BarStyle.valueOf(pluginConfig.bossbar_settings_bar_style));
								Utils.bossbar.addPlayer(p);
							}
						} else {
							int currentTime = instance.time.get(playerName);
							instance.time.put(playerName, currentTime + 1);
							int newTime = instance.time.get(playerName);
							if (pluginConfig.bossbar_settings_enable_bossbar && Utils.bossbar != null) {
								Utils.bossbar.setTitle(pluginConfig.bossbar_message.replace("%time%",
										Integer.toString(pluginConfig.punish_settings_time - newTime)));
								double percents = (pluginConfig.punish_settings_time - newTime)
										/ Double.valueOf(pluginConfig.punish_settings_time);
								if (percents > 0) {
									Utils.bossbar.setProgress(percents);
									Utils.bossbar.addPlayer(p);
								} else {
									instance.loggerInfo(String.valueOf(currentTime));
									instance.loggerInfo(String.valueOf(pluginConfig.punish_settings_time));
									instance.loggerInfo(String.valueOf(newTime));
								}
							}
						}
						if (!noTimeLeft(playerName) && pluginConfig.punish_settings_enable_time) {
							instance.checkFail(playerName, config.getStringList("commands.failed-time"));
							Utils.bossbar.removePlayer(p);
						}
					}
				}
			}
		}).runTaskTimerAsynchronously(instance, 5L, 20L);
	}

	private boolean noTimeLeft(String playerName) {
		return !instance.time.containsKey(playerName) ? true
				: instance.time.get(playerName) < pluginConfig.punish_settings_time;
	}
}
