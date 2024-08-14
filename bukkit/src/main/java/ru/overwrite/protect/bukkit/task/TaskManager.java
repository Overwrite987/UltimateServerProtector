package ru.overwrite.protect.bukkit.task;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import ru.overwrite.protect.bukkit.PasswordHandler;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.api.CaptureReason;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.api.ServerProtectorCaptureEvent;
import ru.overwrite.protect.bukkit.utils.Config;
import ru.overwrite.protect.bukkit.utils.Utils;

import java.util.Date;

public final class TaskManager {

	private final ServerProtectorManager plugin;
	private final ServerProtectorAPI api;
	private final PasswordHandler passwordHandler;
	private final Config pluginConfig;
	private final Runner runner;

	public TaskManager(ServerProtectorManager plugin) {
		this.plugin = plugin;
		this.api = plugin.getPluginAPI();
		this.passwordHandler = plugin.getPasswordHandler();
		this.pluginConfig = plugin.getPluginConfig();
		this.runner = plugin.getRunner();
	}

	public void startMainCheck(long interval) {
		runner.runPeriodicalAsync(() -> {
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (plugin.isExcluded(p, pluginConfig.excluded_admin_pass)) {
					continue;
				}
				if (api.isCaptured(p)) {
					continue;
				}
				CaptureReason captureReason = plugin.checkPermissions(p);
				if (captureReason == null) {
					continue;
				}
				if (!api.isAuthorised(p)) {
					ServerProtectorCaptureEvent captureEvent = new ServerProtectorCaptureEvent(p, Utils.getIp(p), captureReason);
					captureEvent.callEvent();
					if (captureEvent.isCancelled()) {
						continue;
					}
					api.capturePlayer(p);
					if (pluginConfig.sound_settings_enable_sounds) {
						Utils.sendSound(pluginConfig.sound_settings_on_capture, p);
					}
					if (pluginConfig.effect_settings_enable_effects) {
						plugin.giveEffect(p);
					}
					plugin.applyHide(p);
					if (pluginConfig.logging_settings_logging_pas) {
						plugin.logAction("log-format.captured", p, new Date());
					}
					plugin.sendAlert(p, pluginConfig.broadcasts_captured);
				}
			}
		}, 20L, interval >= 0 ? interval : 40L);
	}

	public void startAdminCheck() {
		runner.runPeriodicalAsync(() -> {
			if (api.login.isEmpty())
				return;
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (api.isCaptured(p) && !plugin.isAdmin(p.getName())) {
					plugin.checkFail(p.getName(), pluginConfig.commands_not_in_config);
				}
			}
		}, 0L, 20L);
	}

	public void startCapturesMessages(FileConfiguration config) {
		runner.runPeriodicalAsync(() -> {
			if (api.login.isEmpty())
				return;
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (api.isCaptured(p)) {
					p.sendMessage(this.pluginConfig.msg_message);
					if (this.pluginConfig.message_settings_send_title) {
						Utils.sendTitleMessage(this.pluginConfig.titles_message, p);
					}
				}
			}
		}, 0L, config.getInt("message-settings.delay") * 20L);
	}

	public void startOpCheck() {
		runner.runPeriodicalAsync(() -> {
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.isOp() && !this.pluginConfig.op_whitelist.contains(p.getName())) {
					plugin.checkFail(p.getName(), pluginConfig.commands_not_in_opwhitelist);
				}
			}
		}, 0L, 20L);
	}

	public void startPermsCheck() {
		runner.runPeriodicalAsync(() -> {
			for (Player p : Bukkit.getOnlinePlayers()) {
				for (String badperm : this.pluginConfig.blacklisted_perms) {
					if (p.hasPermission(badperm) && !plugin.isExcluded(p, this.pluginConfig.excluded_blacklisted_perms)) {
						plugin.checkFail(p.getName(), pluginConfig.commands_have_blacklisted_perm);
					}
				}
			}
		}, 5L, 20L);
	}

	public void startCapturesTimer() {
		runner.runPeriodicalAsync(() -> {
			if (api.login.isEmpty())
				return;
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (!api.isCaptured(p)) {
					return;
				}
				String playerName = p.getName();
				if (!plugin.time.containsKey(playerName)) {
					plugin.time.put(playerName, 0);
					if (this.pluginConfig.bossbar_settings_enable_bossbar) {
						BossBar bossbar = Bukkit.createBossBar(
								this.pluginConfig.bossbar_message
										.replace("%time%", Integer.toString(this.pluginConfig.punish_settings_time)),
								BarColor.valueOf(this.pluginConfig.bossbar_settings_bar_color),
								BarStyle.valueOf(this.pluginConfig.bossbar_settings_bar_style));
						bossbar.addPlayer(p);
						passwordHandler.bossbars.put(playerName, bossbar);
					}
				} else {
					plugin.time.compute(playerName, (k, currentTime) -> currentTime + 1);
					int newTime = plugin.time.get(playerName);
					if (this.pluginConfig.bossbar_settings_enable_bossbar && passwordHandler.bossbars.get(playerName) != null) {
						passwordHandler.bossbars.get(playerName).setTitle(this.pluginConfig.bossbar_message
								.replace("%time%", Integer.toString(this.pluginConfig.punish_settings_time - newTime)));
						double percents = (this.pluginConfig.punish_settings_time - newTime)
								/ (double) this.pluginConfig.punish_settings_time;
						if (percents > 0) {
							passwordHandler.bossbars.get(playerName).setProgress(percents);
							passwordHandler.bossbars.get(playerName).addPlayer(p);
						}
					}
				}
				if (!noTimeLeft(playerName) && this.pluginConfig.punish_settings_enable_time) {
					plugin.checkFail(playerName, pluginConfig.commands_failed_time);
					passwordHandler.bossbars.get(playerName).removePlayer(p);
				}
			}
		}, 0L, 20L);
	}

	private boolean noTimeLeft(String playerName) {
		return !plugin.time.containsKey(playerName) || plugin.time.get(playerName) < pluginConfig.punish_settings_time;
	}
}
