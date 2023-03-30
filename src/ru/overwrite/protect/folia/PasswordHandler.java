package ru.overwrite.protect.folia;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import ru.overwrite.protect.folia.utils.Config;
import ru.overwrite.protect.folia.utils.Utils;

public class PasswordHandler {
    private final ServerProtector plugin;
    private final Map<Player, Integer> attempts;

    public PasswordHandler(ServerProtector plugin) {
        this.plugin = plugin;
        this.attempts = new ConcurrentHashMap<>();
    }

    public void checkPassword(Player player, String input, boolean resync) {
        FileConfiguration data = ServerProtectorManager.data;
        if (input.equals(data.getString("data." + player.getName() + ".pass"))) {
            if (resync) {
            	Bukkit.getAsyncScheduler().runNow(plugin, (cp) -> correctPassword(player));
            } else {
                correctPassword(player);
            }
        } else {
            player.sendMessage(Config.msg_incorrect);
            failedPassword(player);
            if (!isAttemptsMax(player) && Config.punish_settings_enable_attempts) {
                if (resync) {
                	player.getScheduler().run(plugin, (fp) -> failedPasswordCommands(player), null);
                    plugin.login.remove(player.getName());
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
        return (attempts.get(player) < Config.punish_settings_max_attempts);
    }

    private void failedPasswordCommands(Player player) {
        for (String command : plugin.getConfig().getStringList("commands.failed-pass")) {
        	Bukkit.getGlobalRegionScheduler().run(plugin, (tt) -> {
        		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
        	});
        }
    }

    private void failedPassword(Player player) {
        Date date = new Date();
        if (Config.punish_settings_enable_attempts) {
        	attempts.put(player, attempts.getOrDefault(player, 0) + 1);
        }
        if (Config.sound_settings_enable_sounds) {
            player.playSound(player.getLocation(), Sound.valueOf(Config.sound_settings_on_pas_fail),
            		Config.sound_settings_volume, Config.sound_settings_pitch);
        }
        if (Config.logging_settings_logging_pas) {
        	plugin.logAction("log-format.failed", player, date);
        }
        String msg = Config.broadcasts_failed.replace("%player%", player.getName()).replace("%ip%", Utils.getIp(player));
        if (Config.message_settings_enable_broadcasts) {
        	for (Player p : Bukkit.getOnlinePlayers()) {
        		if (p.hasPermission("serverprotector.admin")) {
        			p.sendMessage(msg);
        		}
        	}
        }
        if (Config.message_settings_enable_console_broadcasts) {
            Bukkit.getConsoleSender().sendMessage(msg);
        }
    }

    private void correctPassword(Player player) {
        Date date = new Date();
        String playerName = player.getName();
        plugin.login.remove(playerName);
        if (!Config.session_settings_session) {
        	plugin.saved.add(playerName);
        }
        player.sendMessage(Config.msg_correct);
        plugin.time.remove(player);
        if (Config.sound_settings_enable_sounds) {
            player.playSound(player.getLocation(), Sound.valueOf(Config.sound_settings_on_pas_correct),
            		Config.sound_settings_volume, Config.sound_settings_pitch);
        }
        if (Config.effect_settings_enable_effects) {
            for (PotionEffect s : player.getActivePotionEffects()) {
                player.removePotionEffect(s.getType());
            }
        }
        if (Config.session_settings_session) {
        	plugin.ips.add(playerName + Utils.getIp(player));
        }
        if (Config.session_settings_session_time_enabled) {
        	Bukkit.getAsyncScheduler().runDelayed(plugin, (tt) -> {
                if (!plugin.login.contains(playerName)) {
                	plugin.ips.remove(playerName + Utils.getIp(player));
                }
            }, Config.session_settings_session_time * 20L * 50, TimeUnit.MILLISECONDS);
        }
        if (Config.logging_settings_logging_pas) {
        	plugin.logAction("log-format.passed", player, date);
        }
        if (Config.bossbar_settings_enable_bossbar) {
        	if (Runner.bossbar == null) {
        		return;
        	}
        	if (Runner.bossbar.getPlayers().contains(player)) {
        		Runner.bossbar.removePlayer(player);
        	}
    	}
        String msg = Config.broadcasts_passed.replace("%player%", playerName).replace("%ip%", Utils.getIp(player));
        if (Config.message_settings_enable_broadcasts) {
        	for (Player p : Bukkit.getOnlinePlayers()) {
        		if (p.hasPermission("serverprotector.admin")) {
        			p.sendMessage(msg);
        		}
        	}
        }
        if (Config.message_settings_enable_console_broadcasts) {
            Bukkit.getConsoleSender().sendMessage(msg);
        }
    }
}
