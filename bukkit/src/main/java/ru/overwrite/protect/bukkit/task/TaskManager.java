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
import ru.overwrite.protect.bukkit.api.events.ServerProtectorCaptureEvent;
import ru.overwrite.protect.bukkit.utils.configuration.Config;
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
                if (pluginConfig.getExcludedPlayers() == null || !plugin.isExcluded(p, pluginConfig.getExcludedPlayers().adminPass())) {
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
                    if (pluginConfig.getApiSettings().callEventOnCapture()) {
                        captureEvent.callEvent();
                    }
                    if (captureEvent.isCancelled()) {
                        continue;
                    }
                    api.capturePlayer(p);
                    if (pluginConfig.getSoundSettings().enableSounds()) {
                        Utils.sendSound(pluginConfig.getSoundSettings().onCapture(), p);
                    }
                    if (pluginConfig.getEffectSettings().enableEffects()) {
                        plugin.giveEffect(p);
                    }
                    plugin.applyHide(p);
                    if (pluginConfig.getLoggingSettings().loggingPas()) {
                        plugin.logAction("log-format.captured", p, new Date());
                    }
                    plugin.sendAlert(p, pluginConfig.getBroadcasts().captured());
                }
            }
        }, 20L, interval >= 0 ? interval : 40L);
    }

    public void startAdminCheck() {
        runner.runPeriodicalAsync(() -> {
            if (!api.isAnybodyCaptured())
                return;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (api.isCaptured(p) && !plugin.isAdmin(p.getName())) {
                    plugin.checkFail(p.getName(), pluginConfig.getCommands().notInConfig());
                }
            }
        }, 0L, 20L);
    }

    public void startCapturesMessages(FileConfiguration config) {
        runner.runPeriodicalAsync(() -> {
            if (!api.isAnybodyCaptured())
                return;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (api.isCaptured(p)) {
                    p.sendMessage(this.pluginConfig.getMessages().message());
                    if (this.pluginConfig.getMessageSettings().sendTitle()) {
                        Utils.sendTitleMessage(this.pluginConfig.getTitles().message(), p);
                    }
                }
            }
        }, 0L, config.getInt("message-settings.delay") * 20L);
    }

    public void startOpCheck() {
        runner.runPeriodicalAsync(() -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.isOp()
                        && !this.pluginConfig.getAccessData().opWhitelist().contains(p.getName())
                        && (pluginConfig.getExcludedPlayers() == null || !plugin.isExcluded(p, this.pluginConfig.getExcludedPlayers().opWhitelist()))) {
                    plugin.checkFail(p.getName(), pluginConfig.getCommands().notInOpWhitelist());
                }
            }
        }, 0L, 20L);
    }

    public void startPermsCheck() {
        runner.runPeriodicalAsync(() -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                for (String badperm : this.pluginConfig.getAccessData().blacklistedPerms()) {
                    if (p.hasPermission(badperm)
                            && (pluginConfig.getExcludedPlayers() == null || !plugin.isExcluded(p, this.pluginConfig.getExcludedPlayers().blacklistedPerms()))) {
                        plugin.checkFail(p.getName(), pluginConfig.getCommands().haveBlacklistedPerm());
                    }
                }
            }
        }, 5L, 20L);
    }

    public void startCapturesTimer() {
        runner.runPeriodicalAsync(() -> {
            if (!api.isAnybodyCaptured())
                return;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!api.isCaptured(p)) {
                    return;
                }
                String playerName = p.getName();
                if (!plugin.getPerPlayerTime().containsKey(playerName)) {
                    plugin.getPerPlayerTime().put(playerName, 0);
                    if (this.pluginConfig.getBossbarSettings().enableBossbar()) {
                        BossBar bossbar = Bukkit.createBossBar(
                                this.pluginConfig.getBossbarSettings().bossbarMessage()
                                        .replace("%time%", Integer.toString(this.pluginConfig.getPunishSettings().time())),
                                BarColor.valueOf(this.pluginConfig.getBossbarSettings().barColor()),
                                BarStyle.valueOf(this.pluginConfig.getBossbarSettings().barStyle()));
                        bossbar.addPlayer(p);
                        passwordHandler.getBossbars().put(playerName, bossbar);
                    }
                } else {
                    plugin.getPerPlayerTime().compute(playerName, (k, currentTime) -> currentTime + 1);
                    int newTime = plugin.getPerPlayerTime().get(playerName);
                    if (this.pluginConfig.getBossbarSettings().enableBossbar() && passwordHandler.getBossbars().get(playerName) != null) {
                        passwordHandler.getBossbars().get(playerName).setTitle(this.pluginConfig.getBossbarSettings().bossbarMessage()
                                .replace("%time%", Integer.toString(this.pluginConfig.getPunishSettings().time() - newTime)));
                        double percents = (this.pluginConfig.getPunishSettings().time() - newTime)
                                / (double) this.pluginConfig.getPunishSettings().time();
                        if (percents > 0) {
                            passwordHandler.getBossbars().get(playerName).setProgress(percents);
                            passwordHandler.getBossbars().get(playerName).addPlayer(p);
                        }
                    }
                }
                if (!noTimeLeft(playerName) && this.pluginConfig.getPunishSettings().enableTime()) {
                    plugin.checkFail(playerName, pluginConfig.getCommands().failedTime());
                    passwordHandler.getBossbars().get(playerName).removePlayer(p);
                }
            }
        }, 0L, 20L);
    }

    private boolean noTimeLeft(String playerName) {
        return !plugin.getPerPlayerTime().containsKey(playerName) || plugin.getPerPlayerTime().get(playerName) < pluginConfig.getPunishSettings().time();
    }
}