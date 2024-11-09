package ru.overwrite.protect.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
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
    private final Map<String, Integer> attempts = new HashMap<>();

    public Map<String, Integer> getAttempts() {
        return this.attempts;
    }

    private final Map<String, BossBar> bossbars = new HashMap<>();

    public Map<String, BossBar> getBossbars() {
        return this.bossbars;
    }

    public PasswordHandler(ServerProtectorManager plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
        this.api = plugin.getPluginAPI();
    }

    public void checkPassword(Player p, String input, boolean resync) {
        Runnable run = () -> {
            ServerProtectorPasswordEnterEvent enterEvent = new ServerProtectorPasswordEnterEvent(p, input);
            if (pluginConfig.getApiSettings().callEventOnPasswordEnter()) {
                enterEvent.callEvent();
            }
            if (enterEvent.isCancelled()) {
                return;
            }
            String playerPass = pluginConfig.getPerPlayerPasswords().get(p.getName());
            if (playerPass == null) {
                failedPassword(p);
                return;
            }
            String salt = playerPass.split(":")[0];
            String pass = pluginConfig.getEncryptionSettings().enableEncryption()
                    ? Utils.encryptPassword(input, salt, pluginConfig.getEncryptionSettings().encryptMethods())
                    : input;
            if (pass.equals(playerPass)) {
                correctPassword(p);
                return;
            }
            if (pluginConfig.getEncryptionSettings().enableEncryption() && !pluginConfig.getEncryptionSettings().oldEncryptMethods().isEmpty()) {
                for (List<String> oldEncryptMethod : pluginConfig.getEncryptionSettings().oldEncryptMethods()) {
                    String oldgenPass = Utils.encryptPassword(input, salt, oldEncryptMethod);
                    if (oldgenPass.equals(playerPass)) {
                        correctPassword(p);
                        return;
                    }
                }
            }
            failedPassword(p);
            if (pluginConfig.getPunishSettings().enableAttempts() && isAttemptsMax(p.getName())) {
                plugin.checkFail(p.getName(), pluginConfig.getCommands().failedPass());
            }
        };
        if (resync) {
            plugin.getRunner().runPlayer(run, p);
        } else {
            run.run();
        }
    }

    private boolean isAttemptsMax(String playerName) {
        if (!attempts.containsKey(playerName))
            return false;
        return attempts.get(playerName) >= pluginConfig.getPunishSettings().maxAttempts();
    }

    public void failedPassword(Player p) {
        if (!api.isCalledFromAllowedApplication()) {
            return;
        }
        String playerName = p.getName();
        if (pluginConfig.getPunishSettings().enableAttempts()) {
            attempts.put(playerName, attempts.getOrDefault(playerName, 0) + 1);
        }
        ServerProtectorPasswordFailEvent failEvent = new ServerProtectorPasswordFailEvent(p, attempts.get(playerName));
        failEvent.callEvent();
        if (failEvent.isCancelled()) {
            return;
        }
        p.sendMessage(pluginConfig.getMessages().incorrect());
        if (pluginConfig.getMessageSettings().sendTitle()) {
            Utils.sendTitleMessage(pluginConfig.getTitles().incorrect(), p);
        }
        if (pluginConfig.getSoundSettings().enableSounds()) {
            Utils.sendSound(pluginConfig.getSoundSettings().onPasFail(), p);
        }
        if (pluginConfig.getLoggingSettings().loggingPas()) {
            plugin.logAction("log-format.failed", p, LocalDateTime.now());
        }
        plugin.sendAlert(p, pluginConfig.getBroadcasts().failed());
    }

    public void correctPassword(Player p) {
        if (!api.isCalledFromAllowedApplication()) {
            return;
        }
        ServerProtectorPasswordSuccessEvent successEvent = new ServerProtectorPasswordSuccessEvent(p);
        successEvent.callEvent();
        if (successEvent.isCancelled()) {
            return;
        }
        String playerName = p.getName();
        api.uncapturePlayer(playerName);
        p.sendMessage(pluginConfig.getMessages().correct());
        if (pluginConfig.getMessageSettings().sendTitle()) {
            Utils.sendTitleMessage(pluginConfig.getTitles().correct(), p);
        }
        plugin.getPerPlayerTime().remove(playerName);
        if (pluginConfig.getSoundSettings().enableSounds()) {
            Utils.sendSound(pluginConfig.getSoundSettings().onPasCorrect(), p);
        }
        if (pluginConfig.getEffectSettings().enableEffects()) {
            for (PotionEffect effect : p.getActivePotionEffects()) {
                p.removePotionEffect(effect.getType());
            }
        }
        this.showPlayer(p);
        api.authorisePlayer(p);
        if (pluginConfig.getSessionSettings().sessionTimeEnabled()) {
            plugin.getRunner().runDelayedAsync(() -> {
                if (!api.isAuthorised(p)) {
                    api.deauthorisePlayer(p);
                }
            }, pluginConfig.getSessionSettings().sessionTime() * 20L);
        }
        if (pluginConfig.getLoggingSettings().loggingPas()) {
            plugin.logAction("log-format.passed", p, LocalDateTime.now());
        }
        if (pluginConfig.getBossbarSettings().enableBossbar() && bossbars.get(playerName) != null) {
            bossbars.get(playerName).removeAll();
        }
        plugin.sendAlert(p, pluginConfig.getBroadcasts().passed());
    }

    private void showPlayer(Player p) {
        if (pluginConfig.getBlockingSettings().hideOnEntering()) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!onlinePlayer.equals(p)) {
                    onlinePlayer.showPlayer(plugin, p);
                }
            }
        }
        if (pluginConfig.getBlockingSettings().hideOtherOnEntering()) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                p.showPlayer(plugin, onlinePlayer);
            }
        }
    }
}