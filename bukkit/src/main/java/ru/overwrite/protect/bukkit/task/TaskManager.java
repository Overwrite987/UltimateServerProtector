package ru.overwrite.protect.bukkit.task;

import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ru.overwrite.protect.bukkit.PasswordHandler;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.api.CaptureReason;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.api.events.ServerProtectorCaptureEvent;
import ru.overwrite.protect.bukkit.configuration.Config;
import ru.overwrite.protect.bukkit.configuration.data.BossbarSettings;
import ru.overwrite.protect.bukkit.utils.Utils;

import java.time.LocalDateTime;
import java.util.Map;

public final class TaskManager {

    private final ServerProtectorManager plugin;
    private final ServerProtectorAPI api;
    private final PasswordHandler passwordHandler;
    private final Config pluginConfig;
    private final Runner runner;

    public TaskManager(ServerProtectorManager plugin) {
        this.plugin = plugin;
        this.api = plugin.getApi();
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
                    captureEvent.callEvent();
                    if (pluginConfig.getApiSettings().allowCancelCaptureEvent() && captureEvent.isCancelled()) {
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
                        plugin.logAction(pluginConfig.getLogMessages().captured(), onlinePlayer, LocalDateTime.now());
                    }
                    if (pluginConfig.getBroadcasts() != null) {
                        plugin.sendAlert(onlinePlayer, pluginConfig.getBroadcasts().captured());
                    }
                }
            }
        }, 20L, interval >= 0 ? interval : 40L);
    }

    public void startAdminCheck() {
        runner.runPeriodicalAsync(() -> {
            if (!api.isAnybodyCaptured()) {
                return;
            }
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (api.isCaptured(onlinePlayer) && !plugin.isAdmin(onlinePlayer.getName())) {
                    plugin.checkFail(onlinePlayer.getName(), pluginConfig.getCommands().notInConfig());
                }
            }
        }, 5L, 20L);
    }

    public void startCapturesMessages(FileConfiguration config) {
        runner.runPeriodicalAsync(() -> {
            if (!api.isAnybodyCaptured()) {
                return;
            }
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
            if (!api.isAnybodyCaptured()) {
                return;
            }
            BossbarSettings bossbarSettings = pluginConfig.getBossbarSettings();
            int time = pluginConfig.getPunishSettings().time();
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.isDead() || !api.isCaptured(onlinePlayer)) {
                    return;
                }
                String playerName = onlinePlayer.getName();
                Map<String, Integer> perPlayerTime = plugin.getPerPlayerTime();
                if (!perPlayerTime.containsKey(playerName)) {
                    perPlayerTime.put(playerName, 0);
                    if (bossbarSettings.enableBossbar()) {
                        BossBar bossbar = Bukkit.createBossBar(
                                bossbarSettings.bossbarMessage().replace("%time%", Integer.toString(time)),
                                bossbarSettings.barColor(),
                                bossbarSettings.barStyle());
                        bossbar.addPlayer(onlinePlayer);
                        passwordHandler.getBossbars().put(playerName, bossbar);
                    }
                } else {
                    int newTime = perPlayerTime.compute(playerName, (k, currentTime) -> currentTime + 1);
                    BossBar bossBar = passwordHandler.getBossbars().get(playerName);
                    if (bossbarSettings.enableBossbar() && bossBar != null) {
                        bossBar.setTitle(bossbarSettings.bossbarMessage().replace("%time%", Integer.toString(time - newTime)));
                        double percents = (time - newTime)
                                / (double) time;
                        if (percents > 0) {
                            bossBar.setProgress(percents);
                            bossBar.addPlayer(onlinePlayer);
                        }
                    }
                    if (time - newTime <= 0) {
                        plugin.checkFail(playerName, pluginConfig.getCommands().failedTime());
                        passwordHandler.getBossbars().get(playerName).removePlayer(onlinePlayer);
                    }
                }
            }
        }, 5L, 20L);
    }
}
