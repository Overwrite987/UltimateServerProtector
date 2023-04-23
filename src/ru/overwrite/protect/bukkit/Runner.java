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
	private final ServerProtectorManager instance;
	private final Config pluginConfig;
	
	public Runner(ServerProtectorManager plugin) {
        this.instance = plugin;
        pluginConfig = plugin.getPluginConfig();
    }
	
    public void run() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            Date date = new Date();
            String playerName = p.getName();
            if (instance.login.contains(playerName)) {
                continue;
            }
            if (!instance.isExcluded(p) &&
            	    instance.isPermissions(p) &&
            	    !(!pluginConfig.session_settings_session && instance.saved.contains(playerName)) &&
            	    !instance.ips.contains(playerName + Utils.getIp(p))) {
            	instance.login.add(playerName);
                if (pluginConfig.sound_settings_enable_sounds) {
                    p.playSound(p.getLocation(), Sound.valueOf(pluginConfig.sound_settings_on_capture),
                    		pluginConfig.sound_settings_volume, pluginConfig.sound_settings_pitch);
                }
                if (pluginConfig.effect_settings_enable_effects) {
                    giveEffect(instance, p);
                }
                if (pluginConfig.logging_settings_logging_pas) {
                	instance.logAction("log-format.captured", p, date);
                }
                String msg = pluginConfig.broadcasts_captured.replace("%player%", playerName).replace("%ip%", Utils.getIp(p));
                if (pluginConfig.message_settings_enable_broadcasts) {
                	if (p.hasPermission("serverprotector.admin")) {
                		p.sendMessage(msg);
                	}
                }
                if (pluginConfig.message_settings_enable_console_broadcasts) {
                    Bukkit.getConsoleSender().sendMessage(msg);
                }
            }
        }
    }

    private void giveEffect(ServerProtectorManager plugin, Player p) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (String s : pluginConfig.effect_settings_effects) {
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
                        p.sendMessage(pluginConfig.msg_message);
                        if (pluginConfig.message_settings_send_title) {
                            p.sendTitle(pluginConfig.titles_title, pluginConfig.titles_subtitle, 10, 70, 20);
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
                    if (p.isOp() && !pluginConfig.op_whitelist.contains(p.getName())) {
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
                    for (String badperms : pluginConfig.blacklisted_perms) {
                        if (p.hasPermission(badperms) && !instance.isExcluded(p)) {
                            checkFail(instance, p, config.getStringList("commands.have-blacklisted-perm"));
                        }
                    }
                }
            }
        }).runTaskTimerAsynchronously(instance, 5L, 20L);
    }

    public static void checkFail(ServerProtectorManager plugin, Player p, List<String> command) {
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
                    	if (pluginConfig.bossbar_settings_enable_bossbar) {
                    		bossbar = Bukkit.createBossBar(pluginConfig.bossbar_message.replace("%time%", String.valueOf(pluginConfig.punish_settings_time)), 
                    			BarColor.valueOf(pluginConfig.bossbar_settings_bar_color), 
                    			BarStyle.valueOf(pluginConfig.bossbar_settings_bar_style));
                    		bossbar.addPlayer(p);
                    	}
                    } else if (instance.login.contains(p.getName())) {
                    	instance.time.put(p, instance.time.get(p) + 1);
                    	if (pluginConfig.bossbar_settings_enable_bossbar) {
                    		bossbar.setTitle(pluginConfig.bossbar_message.replace("%time%",
                    	    	Integer.toString(pluginConfig.punish_settings_time - instance.time.get(p))));
                    		double percents = (pluginConfig.punish_settings_time - instance.time.get(p))/Double.valueOf(pluginConfig.punish_settings_time);
                    		bossbar.setProgress(percents);
                    		bossbar.addPlayer(p);
                    	}
                    }
                    if (!noTimeLeft(p) && pluginConfig.punish_settings_enable_time) {
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
        return (instance.time.get(p) < pluginConfig.punish_settings_time);
    }

}
