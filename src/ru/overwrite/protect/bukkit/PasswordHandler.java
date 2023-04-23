package ru.overwrite.protect.bukkit;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import ru.overwrite.protect.bukkit.utils.Config;
import ru.overwrite.protect.bukkit.utils.Utils;

public class PasswordHandler {
    private final ServerProtectorManager instance;
    private final Config pluginConfig;
    private final Map<Player, Integer> attempts;

    public PasswordHandler(ServerProtectorManager plugin) {
        this.instance = plugin;
        pluginConfig = plugin.getPluginConfig();
        attempts = new HashMap<>();
    }

    public void checkPassword(Player player, String input, boolean resync) {
        FileConfiguration data = instance.data;
        if (input.equals(data.getString("data." + player.getName() + ".pass"))) {
            if (resync) {
                Bukkit.getScheduler().runTask(instance, () -> correctPassword(player));
            } else {
                correctPassword(player);
            }
        } else {
            player.sendMessage(pluginConfig.msg_incorrect);
            failedPassword(player);
            if (!isAttemptsMax(player) && pluginConfig.punish_settings_enable_attempts) {
                if (resync) {
                    Bukkit.getScheduler().runTask(instance, () -> failedPasswordCommands(player));
                    instance.login.remove(player.getName());
                } else {
                    failedPasswordCommands(player);
                }
            }
        }
    }

    public void clearAttempts() {
        attempts.clear();
    }

    private boolean isAttemptsMax(Player player) {
        if (!attempts.containsKey(player)) return true;
        return (attempts.get(player) < pluginConfig.punish_settings_max_attempts);
    }

    private void failedPasswordCommands(Player player) {
        for (String command : instance.getConfig().getStringList("commands.failed-pass")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
        }
    }

    private void failedPassword(Player player) {
        Date date = new Date();
        if (pluginConfig.punish_settings_enable_attempts) {
        	attempts.put(player, attempts.getOrDefault(player, 0) + 1);
        }
        if (pluginConfig.sound_settings_enable_sounds) {
            player.playSound(player.getLocation(), Sound.valueOf(pluginConfig.sound_settings_on_pas_fail),
            		pluginConfig.sound_settings_volume, pluginConfig.sound_settings_pitch);
        }
        if (pluginConfig.logging_settings_logging_pas) {
        	instance.logAction("log-format.failed", player, date);
        }
        String msg = pluginConfig.broadcasts_failed.replace("%player%", player.getName()).replace("%ip%", Utils.getIp(player));
        if (pluginConfig.message_settings_enable_broadcasts) {
        	for (Player p : Bukkit.getOnlinePlayers()) {
        		if (p.hasPermission("serverprotector.admin")) {
        			p.sendMessage(msg);
        		}
        	}
        }
        if (pluginConfig.message_settings_enable_console_broadcasts) {
            Bukkit.getConsoleSender().sendMessage(msg);
        }
    }

    private void correctPassword(Player player) {
        Date date = new Date();
        String playerName = player.getName();
        instance.login.remove(playerName);
        if (!pluginConfig.session_settings_session) {
        	instance.saved.add(playerName);
        }
        player.sendMessage(pluginConfig.msg_correct);
        instance.time.remove(player);
        if (pluginConfig.sound_settings_enable_sounds) {
            player.playSound(player.getLocation(), Sound.valueOf(pluginConfig.sound_settings_on_pas_correct),
            		pluginConfig.sound_settings_volume, pluginConfig.sound_settings_pitch);
        }
        if (pluginConfig.effect_settings_enable_effects) {
            for (PotionEffect s : player.getActivePotionEffects()) {
                player.removePotionEffect(s.getType());
            }
        }
        if (pluginConfig.session_settings_session) {
        	instance.ips.add(playerName + Utils.getIp(player));
        }
        if (pluginConfig.session_settings_session_time_enabled) {
            instance.getServer().getScheduler().runTaskLaterAsynchronously(instance, () -> {
                if (!instance.login.contains(playerName)) {
                	instance.ips.remove(playerName + Utils.getIp(player));
                }
            }, pluginConfig.session_settings_session_time * 20L);
        }
        if (pluginConfig.logging_settings_logging_pas) {
        	instance.logAction("log-format.passed", player, date);
        }
        if (pluginConfig.bossbar_settings_enable_bossbar) {
        	if (Runner.bossbar == null) {
        		return;
        	}
        	if (Runner.bossbar.getPlayers().contains(player)) {
        		Runner.bossbar.removePlayer(player);
        	}
    	}
        String msg = pluginConfig.broadcasts_passed.replace("%player%", playerName).replace("%ip%", Utils.getIp(player));
        if (pluginConfig.message_settings_enable_broadcasts) {
        	for (Player p : Bukkit.getOnlinePlayers()) {
        		if (p.hasPermission("serverprotector.admin")) {
        			p.sendMessage(msg);
        		}
        	}
        }
        if (pluginConfig.message_settings_enable_console_broadcasts) {
            Bukkit.getConsoleSender().sendMessage(msg);
        }
    }
}
