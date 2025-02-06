package ru.overwrite.protect.bukkit;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.api.events.ServerProtectorPasswordEnterEvent;
import ru.overwrite.protect.bukkit.api.events.ServerProtectorPasswordFailEvent;
import ru.overwrite.protect.bukkit.api.events.ServerProtectorPasswordSuccessEvent;
import ru.overwrite.protect.bukkit.configuration.Config;
import ru.overwrite.protect.bukkit.utils.Utils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PasswordHandler {

    private final ServerProtectorManager plugin;
    private final ServerProtectorAPI api;
    private final Config pluginConfig;

    @Getter
    private final Map<String, Integer> attempts = new HashMap<>();

    @Getter
    private final Map<String, BossBar> bossbars = new HashMap<>();

    public PasswordHandler(ServerProtectorManager plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
        this.api = plugin.getApi();
    }

    public void checkPassword(Player player, String input, boolean resync) {
        Runnable run = () -> {
            ServerProtectorPasswordEnterEvent enterEvent = new ServerProtectorPasswordEnterEvent(player, input);
            if (pluginConfig.getApiSettings().callEventOnPasswordEnter()) {
                enterEvent.callEvent();
            }
            if (enterEvent.isCancelled()) {
                return;
            }
            String playerPass = pluginConfig.getPerPlayerPasswords().get(player.getName());
            if (playerPass == null) {
                this.failedPassword(player);
                return;
            }
            String salt = playerPass.split(":")[0];
            String pass = pluginConfig.getEncryptionSettings().enableEncryption()
                    ? Utils.encryptPassword(input, salt, pluginConfig.getEncryptionSettings().encryptMethods())
                    : input;
            if (pass.equals(playerPass)) {
                this.correctPassword(player);
                return;
            }
            if (pluginConfig.getEncryptionSettings().enableEncryption() && !pluginConfig.getEncryptionSettings().oldEncryptMethods().isEmpty()) {
                for (List<String> oldEncryptMethod : pluginConfig.getEncryptionSettings().oldEncryptMethods()) {
                    String oldgenPass = Utils.encryptPassword(input, salt, oldEncryptMethod);
                    if (oldgenPass.equals(playerPass)) {
                        this.correctPassword(player);
                        return;
                    }
                }
            }
            this.failedPassword(player);
            if (pluginConfig.getPunishSettings().enableAttempts() && isAttemptsMax(player.getName())) {
                plugin.checkFail(player.getName(), pluginConfig.getCommands().failedPass());
            }
        };
        if (resync) {
            plugin.getRunner().runPlayer(run, player);
        } else {
            run.run();
        }
    }

    private boolean isAttemptsMax(String playerName) {
        int playerAttempts = attempts.getOrDefault(playerName, 0);
        return playerAttempts >= pluginConfig.getPunishSettings().maxAttempts();
    }

    public void failedPassword(Player player) {
        if (!plugin.isCalledFromAllowedApplication()) {
            return;
        }
        String playerName = player.getName();
        if (pluginConfig.getPunishSettings().enableAttempts()) {
            attempts.put(playerName, attempts.getOrDefault(playerName, 0) + 1);
        }
        ServerProtectorPasswordFailEvent failEvent = new ServerProtectorPasswordFailEvent(player, attempts.get(playerName));
        if (!failEvent.callEvent()) {
            return;
        }
        player.sendMessage(pluginConfig.getMessages().incorrect());
        if (pluginConfig.getMessageSettings().sendTitle()) {
            Utils.sendTitleMessage(pluginConfig.getTitles().incorrect(), player);
        }
        if (pluginConfig.getSoundSettings().enableSounds()) {
            Utils.sendSound(pluginConfig.getSoundSettings().onPasFail(), player);
        }
        if (pluginConfig.getLoggingSettings().loggingPas()) {
            plugin.logAction(pluginConfig.getLogMessages().failed(), player, LocalDateTime.now());
        }
        plugin.sendAlert(player, pluginConfig.getBroadcasts().failed());
    }

    public void correctPassword(Player player) {
        if (!plugin.isCalledFromAllowedApplication()) {
            return;
        }
        ServerProtectorPasswordSuccessEvent successEvent = new ServerProtectorPasswordSuccessEvent(player);
        if (!successEvent.callEvent()) {
            return;
        }
        String playerName = player.getName();
        api.uncapturePlayer(playerName);
        player.sendMessage(pluginConfig.getMessages().correct());
        if (pluginConfig.getMessageSettings().sendTitle()) {
            Utils.sendTitleMessage(pluginConfig.getTitles().correct(), player);
        }
        plugin.getPerPlayerTime().remove(playerName);
        if (pluginConfig.getSoundSettings().enableSounds()) {
            Utils.sendSound(pluginConfig.getSoundSettings().onPasCorrect(), player);
        }
        if (pluginConfig.getEffectSettings().enableEffects()) {
            plugin.removeEffects(player);
        }
        this.showPlayer(player);
        api.authorisePlayer(player);
        if (pluginConfig.getSessionSettings().sessionTimeEnabled()) {
            plugin.getRunner().runDelayedAsync(() -> {
                if (!api.isAuthorised(player)) {
                    api.deauthorisePlayer(player);
                }
            }, pluginConfig.getSessionSettings().sessionTime() * 20L);
        }
        if (pluginConfig.getLoggingSettings().loggingPas()) {
            plugin.logAction(pluginConfig.getLogMessages().passed(), player, LocalDateTime.now());
        }
        if (pluginConfig.getBossbarSettings().enableBossbar() && bossbars.get(playerName) != null) {
            bossbars.get(playerName).removeAll();
        }
        plugin.sendAlert(player, pluginConfig.getBroadcasts().passed());
    }

    private void showPlayer(Player player) {
        if (pluginConfig.getBlockingSettings().hideOnEntering()) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!onlinePlayer.equals(player)) {
                    onlinePlayer.showPlayer(plugin, player);
                }
            }
        }
        if (pluginConfig.getBlockingSettings().hideOtherOnEntering()) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                player.showPlayer(plugin, onlinePlayer);
            }
        }
    }
}
