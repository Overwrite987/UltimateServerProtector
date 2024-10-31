package ru.overwrite.protect.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.api.ServerProtectorPasswordEnterEvent;
import ru.overwrite.protect.bukkit.api.ServerProtectorPasswordFailEvent;
import ru.overwrite.protect.bukkit.api.ServerProtectorPasswordSuccessEvent;
import ru.overwrite.protect.bukkit.utils.Config;
import ru.overwrite.protect.bukkit.utils.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PasswordHandler {

    private final ServerProtectorManager plugin;
    private final ServerProtectorAPI api;
    private final Config pluginConfig;
    public final Map<String, Integer> attempts = new HashMap<>();
    public final Map<String, BossBar> bossbars = new HashMap<>();

    public PasswordHandler(ServerProtectorManager plugin) {
        this.plugin = plugin;
        pluginConfig = plugin.getPluginConfig();
        api = plugin.getPluginAPI();
    }

    public void checkPassword(Player p, String input, boolean resync) {
        Runnable run = () -> {
            ServerProtectorPasswordEnterEvent enterEvent = new ServerProtectorPasswordEnterEvent(p, input);
            if (pluginConfig.secure_settings_call_event_on_password_enter) {
                enterEvent.callEvent();
            }
            if (enterEvent.isCancelled()) {
                return;
            }
            String playerPass = pluginConfig.per_player_passwords.get(p.getName());
            if (playerPass == null) {
                failedPassword(p);
                return;
            }
            String salt = playerPass.split(":")[0];
            String pass = pluginConfig.encryption_settings_enable_encryption
                    ? Utils.encryptPassword(input, salt, pluginConfig.encryption_settings_encrypt_methods)
                    : input;
            if (pass.equals(playerPass)) {
                correctPassword(p);
                return;
            }
            if (pluginConfig.encryption_settings_enable_encryption && !pluginConfig.encryption_settings_old_encrypt_methods.isEmpty()) {
                for (List<String> oldEncryptMethod : pluginConfig.encryption_settings_old_encrypt_methods) {
                    String oldgenPass = Utils.encryptPassword(input, salt, oldEncryptMethod);
                    if (oldgenPass.equals(playerPass)) {
                        correctPassword(p);
                        return;
                    }
                }
            }
            failedPassword(p);
            if (pluginConfig.punish_settings_enable_attempts && isAttemptsMax(p.getName())) {
                plugin.checkFail(p.getName(), pluginConfig.commands_failed_pass);
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
        return attempts.get(playerName) >= pluginConfig.punish_settings_max_attempts;
    }

    public void failedPassword(Player p) {
        String playerName = p.getName();
        if (pluginConfig.punish_settings_enable_attempts) {
            attempts.put(playerName, attempts.getOrDefault(playerName, 0) + 1);
        }
        ServerProtectorPasswordFailEvent failEvent = new ServerProtectorPasswordFailEvent(p, attempts.get(playerName));
        failEvent.callEvent();
        if (failEvent.isCancelled()) {
            return;
        }
        p.sendMessage(pluginConfig.msg_incorrect);
        if (pluginConfig.message_settings_send_title) {
            Utils.sendTitleMessage(pluginConfig.titles_incorrect, p);
        }
        if (pluginConfig.sound_settings_enable_sounds) {
            Utils.sendSound(pluginConfig.sound_settings_on_pas_fail, p);
        }
        if (pluginConfig.logging_settings_logging_pas) {
            plugin.logAction("log-format.failed", p, new Date());
        }
        plugin.sendAlert(p, pluginConfig.broadcasts_failed);
    }

    public void correctPassword(Player p) {
        ServerProtectorPasswordSuccessEvent successEvent = new ServerProtectorPasswordSuccessEvent(p);
        successEvent.callEvent();
        if (successEvent.isCancelled()) {
            return;
        }
        api.uncapturePlayer(p);
        p.sendMessage(pluginConfig.msg_correct);
        if (pluginConfig.message_settings_send_title) {
            Utils.sendTitleMessage(pluginConfig.titles_correct, p);
        }
        String playerName = p.getName();
        plugin.time.remove(playerName);
        if (pluginConfig.sound_settings_enable_sounds) {
            Utils.sendSound(pluginConfig.sound_settings_on_pas_correct, p);
        }
        if (pluginConfig.effect_settings_enable_effects) {
            for (PotionEffect s : p.getActivePotionEffects()) {
                p.removePotionEffect(s.getType());
            }
        }
        this.showPlayer(p);
        api.authorisePlayer(p);
        if (pluginConfig.session_settings_session_time_enabled) {
            plugin.getRunner().runDelayedAsync(() -> {
                if (!api.isAuthorised(p)) {
                    api.deauthorisePlayer(p);
                }
            }, pluginConfig.session_settings_session_time * 20L);
        }
        if (pluginConfig.logging_settings_logging_pas) {
            plugin.logAction("log-format.passed", p, new Date());
        }
        if (pluginConfig.bossbar_settings_enable_bossbar && bossbars.get(playerName) != null) {
            bossbars.get(playerName).removeAll();
        }
        plugin.sendAlert(p, pluginConfig.broadcasts_passed);
    }

    private void showPlayer(Player p) {
        if (pluginConfig.blocking_settings_hide_on_entering) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!onlinePlayer.equals(p)) {
                    onlinePlayer.showPlayer(plugin, p);
                }
            }
        }
        if (pluginConfig.blocking_settings_hide_other_on_entering) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                p.showPlayer(plugin, onlinePlayer);
            }
        }
    }
}