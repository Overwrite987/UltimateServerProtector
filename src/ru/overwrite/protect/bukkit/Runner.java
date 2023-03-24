package ru.overwrite.protect.bukkit;

import java.util.Date;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import ru.overwrite.protect.bukkit.utils.Config;
import ru.overwrite.protect.bukkit.utils.Utils;

public class Runner extends BukkitRunnable {
	
	public static BossBar bossbar;
	private final ServerProtector instance = ServerProtector.getInstance();
	
    public void run() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            Date date = new Date();
            String playerName = p.getName();
            if (instance.login.contains(playerName)) {
                continue;
            }
            if (!instance.isExcluded(p) &&
            	    instance.isPermissions(p) &&
            	    !(!Config.session_settings_session && instance.saved.contains(playerName)) &&
            	    !instance.ips.contains(playerName + Utils.getIp(p))) {
            	instance.login.add(p.getName());
                if (Config.sound_settings_enable_sounds) {
                    p.playSound(p.getLocation(), Sound.valueOf(Config.sound_settings_on_capture),
                    		Config.sound_settings_volume, Config.sound_settings_pitch);
                }
                if (Config.effect_settings_enable_effects) {
                    giveEffect(instance, p);
                }
                if (Config.logging_settings_logging_pas) {
                	instance.logAction("log-format.captured", p, date);
                }
                String msg = Config.broadcasts_captured.replace("%player%", playerName).replace("%ip%", Utils.getIp(p));
                if (Config.message_settings_enable_broadcasts) {
                	if (p.hasPermission("serverprotector.admin")) {
                		p.sendMessage(msg);
                	}
                }
                if (Config.message_settings_enable_console_broadcasts) {
                    Bukkit.getConsoleSender().sendMessage(msg);
                }
            }
        }
    }

    private void giveEffect(ServerProtector plugin, Player p) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (String s : Config.effect_settings_effects) {
                PotionEffectType types = PotionEffectType.getByName(s.split(":")[0].toUpperCase());
                int level = Integer.parseInt(s.split(":")[1]) - 1;
                p.addPotionEffect(new PotionEffect(types, 99999, level));
            }
        });
    }

    public void adminCheck() {
        (new BukkitRunnable() {
            public void run() {
            	if (instance.login.isEmpty()) return;
            	FileConfiguration config = instance.getConfig();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (instance.login.contains(p.getName()) && !instance.isAdmin(p.getName())) {
                        checkFail(instance, p, config.getStringList("commands.not-in-config"));
                    }
                }
            }
        }).runTaskTimerAsynchronously(instance, 0L, 20L);
    }

    public void startMSG() {
    	FileConfiguration config = instance.getConfig();
        (new BukkitRunnable() {
            public void run() {
            	if (instance.login.isEmpty()) return;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (instance.login.contains(p.getName())) {
                        p.sendMessage(Config.msg_message);
                        if (Config.message_settings_send_title) {
                            p.sendTitle(Config.titles_title, Config.titles_subtitle, 10, 70, 20);
                            return;
                        }
                    }
                }
            }
        }).runTaskTimerAsynchronously(instance, 5L, config.getInt("message-settings.delay") * 20L);
    }

    public void startOpCheck() {
        FileConfiguration config = instance.getConfig();
        (new BukkitRunnable() {
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.isOp() && !config.getStringList("op-whitelist").contains(p.getName())) {
                        checkFail(instance, p, config.getStringList("commands.not-in-opwhitelist"));
                    }
                }
            }
        }).runTaskTimerAsynchronously(instance, 5L, 20L);
    }

    public void startPermsCheck() {
        FileConfiguration config = instance.getConfig();
        (new BukkitRunnable() {
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    for (String badperms : config.getStringList("blacklisted-perms")) {
                        if (p.hasPermission(badperms) && !instance.isExcluded(p)) {
                            checkFail(instance, p, config.getStringList("commands.have-blacklisted-perm"));
                        }
                    }
                }
            }
        }).runTaskTimerAsynchronously(instance, 5L, 20L);
    }

    public static void checkFail(ServerProtector plugin, Player p, List<String> command) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (String c : command) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c.replace("%player%", p.getName()));
            }
        });
    }
    
    public void startTimer() {
        FileConfiguration config = instance.getConfig();
        (new BukkitRunnable() {
            public void run() {
            	if (instance.login.isEmpty()) return;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (instance.login.contains(p.getName()) && !instance.time.containsKey(p)) {
                    	instance.time.put(p, 0);
                    	if (Config.bossbar_settings_enable_bossbar) {
                    		bossbar = Bukkit.createBossBar(Config.bossbar_message.replace("%time%", String.valueOf(Config.punish_settings_time)), 
                    			BarColor.valueOf(Config.bossbar_settings_bar_color), 
                    			BarStyle.valueOf(Config.bossbar_settings_bar_style));
                    		bossbar.addPlayer(p);
                    	}
                    } else if (instance.login.contains(p.getName())) {
                    	instance.time.put(p, instance.time.get(p) + 1);
                    	if (Config.bossbar_settings_enable_bossbar) {
                    		bossbar.setTitle(Config.bossbar_message.replace("%time%",
                    	    	Integer.toString(Config.punish_settings_time - instance.time.get(p))));
                    		double percents = (Config.punish_settings_time - instance.time.get(p))/Double.valueOf(Config.punish_settings_time);
                    		bossbar.setProgress(percents);
                    		bossbar.addPlayer(p);
                    	}
                    }
                    if (!noTimeLeft(p) && Config.punish_settings_enable_time) {
                        checkFail(instance, p, config.getStringList("commands.failed-time"));
                        bossbar.removePlayer(p);
                    }
                }
            }
        }).runTaskTimerAsynchronously(instance, 5L, 20L);
    }

    private boolean noTimeLeft(Player p) {
        if (!instance.time.containsKey(p)) {
            return true;
        }
        return (instance.time.get(p) < Config.punish_settings_time);
    }
}
