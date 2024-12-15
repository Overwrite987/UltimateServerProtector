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
import ru.overwrite.protect.bukkit.configuration.Config;
import ru.overwrite.protect.bukkit.utils.Utils;

import java.time.LocalDateTime;

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
            if (Bukkit.getOnlinePlayers().isEmpty()) {
                return;
            }
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (pluginConfig.getExcludedPlayers() != null && plugin.isExcluded(onlinePlayer, pluginConfig.getExcludedPlayers().adminPass())) {
                    continue;
                }
                if (api.isCaptured(onlinePlayer)) {
                    continue;
                }
                CaptureReason captureReason = plugin.checkPermissions(onlinePlayer);
                if (captureReason == null) {
                    continue;
                }
                if (!api.isAuthorised(onlinePlayer)) {
                    ServerProtectorCaptureEvent captureEvent = new ServerProtectorCaptureEvent(onlinePlayer, Utils.getIp(onlinePlayer), captureReason);
                    if (pluginConfig.getApiSettings().callEventOnCapture()) {
                        captureEvent.callEvent();
                    }
                    if (captureEvent.isCancelled()) {
                        continue;
                    }
                    api.capturePlayer(onlinePlayer);
                    if (pluginConfig.getSoundSettings().enableSounds()) {
                        Utils.sendSound(pluginConfig.getSoundSettings().onCapture(), onlinePlayer);
                    }
                    if (pluginConfig.getEffectSettings().enableEffects()) {
                        plugin.giveEffects(onlinePlayer);
                    }
                    plugin.applyHide(onlinePlayer);
                    if (pluginConfig.getLoggingSettings().loggingPas()) {
                        plugin.logAction(pluginConfig.getLogFormats().captured(), onlinePlayer, LocalDateTime.now());
                    }
                    plugin.sendAlert(onlinePlayer, pluginConfig.getBroadcasts().captured());
                }
            }
        }, 20L, interval >= 0 ? interval : 40L);
    }

    public void startAdminCheck() {
        runner.runPeriodicalAsync(() -> {
            if (!api.isAnybodyCaptured())
                return;
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (api.isCaptured(onlinePlayer) && !plugin.isAdmin(onlinePlayer.getName())) {
                    plugin.checkFail(onlinePlayer.getName(), pluginConfig.getCommands().notInConfig());
                }
            }
        }, 5L, 20L);
    }

    public void startCapturesMessages(FileConfiguration config) {
        runner.runPeriodicalAsync(() -> {
            if (!api.isAnybodyCaptured())
                return;
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (api.isCaptured(onlinePlayer)) {
                    onlinePlayer.sendMessage(pluginConfig.getMessages().message());
                    if (pluginConfig.getMessageSettings().sendTitle()) {
                        Utils.sendTitleMessage(pluginConfig.getTitles().message(), onlinePlayer);
                    }
                }
            }
        }, 5L, config.getInt("message-settings.delay") * 20L);
    }

    public void startOpCheck() {
        runner.runPeriodicalAsync(() -> {
            if (Bukkit.getOnlinePlayers().isEmpty()) {
                return;
            }
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.isOp()
                        && !pluginConfig.getAccessData().opWhitelist().contains(onlinePlayer.getName())
                        && (pluginConfig.getExcludedPlayers() == null || !plugin.isExcluded(onlinePlayer, pluginConfig.getExcludedPlayers().opWhitelist()))) {
                    plugin.checkFail(onlinePlayer.getName(), pluginConfig.getCommands().notInOpWhitelist());
                }
            }
        }, 5L, 20L);
    }

    public void startPermsCheck() {
        runner.runPeriodicalAsync(() -> {
            if (Bukkit.getOnlinePlayers().isEmpty()) {
                return;
            }
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                for (String blacklistedPerm : pluginConfig.getAccessData().blacklistedPerms()) {
                    if (onlinePlayer.hasPermission(blacklistedPerm) &&
                            (pluginConfig.getExcludedPlayers() == null || !plugin.isExcluded(onlinePlayer, pluginConfig.getExcludedPlayers().blacklistedPerms()))) {
                        plugin.checkFail(onlinePlayer.getName(), pluginConfig.getCommands().haveBlacklistedPerm());
                    }
                }
            }
        }, 5L, 20L);
    }

    public void startCapturesTimer() {
        runner.runPeriodicalAsync(() -> {
            if (!api.isAnybodyCaptured())
                return;
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!api.isCaptured(onlinePlayer)) {
                    return;
                }
                String playerName = onlinePlayer.getName();
                if (!plugin.getPerPlayerTime().containsKey(playerName)) {
                    plugin.getPerPlayerTime().put(playerName, 0);
                    if (pluginConfig.getBossbarSettings().enableBossbar()) {
                        BossBar bossbar = Bukkit.createBossBar(
                                pluginConfig.getBossbarSettings().bossbarMessage()
                                        .replace("%time%", Integer.toString(pluginConfig.getPunishSettings().time())),
                                BarColor.valueOf(pluginConfig.getBossbarSettings().barColor()),
                                BarStyle.valueOf(pluginConfig.getBossbarSettings().barStyle()));
                        bossbar.addPlayer(onlinePlayer);
                        passwordHandler.getBossbars().put(playerName, bossbar);
                    }
                } else {
                    plugin.getPerPlayerTime().compute(playerName, (k, currentTime) -> currentTime + 1);
                    int newTime = plugin.getPerPlayerTime().get(playerName);
                    if (pluginConfig.getBossbarSettings().enableBossbar() && passwordHandler.getBossbars().get(playerName) != null) {
                        passwordHandler.getBossbars().get(playerName).setTitle(pluginConfig.getBossbarSettings().bossbarMessage()
                                .replace("%time%", Integer.toString(pluginConfig.getPunishSettings().time() - newTime)));
                        double percents = (pluginConfig.getPunishSettings().time() - newTime)
                                / (double) pluginConfig.getPunishSettings().time();
                        if (percents > 0) {
                            passwordHandler.getBossbars().get(playerName).setProgress(percents);
                            passwordHandler.getBossbars().get(playerName).addPlayer(onlinePlayer);
                        }
                    }
                }
                if (!noTimeLeft(playerName) && pluginConfig.getPunishSettings().enableTime()) {
                    plugin.checkFail(playerName, pluginConfig.getCommands().failedTime());
                    passwordHandler.getBossbars().get(playerName).removePlayer(onlinePlayer);
                }
            }
        }, 5L, 20L);
    }

    private boolean noTimeLeft(String playerName) {
        return !plugin.getPerPlayerTime().containsKey(playerName) || plugin.getPerPlayerTime().get(playerName) < pluginConfig.getPunishSettings().time();
    }
}
