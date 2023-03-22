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
            if (instance.login.contains(p.getName())) {
                continue;
            }
            FileConfiguration config = instance.getConfig();
            if (instance.isPermissions(p) &&
                    !(config.getBoolean("secure-settings.enable-excluded-players") && config.getStringList("excluded-players").contains(p.getName())) &&
                    !instance.ips.contains(p.getName()+Utils.getIp(p))) {
            	instance.login.add(p.getName());
                if (config.getBoolean("sound-settings.enable-sounds")) {
                    p.playSound(p.getLocation(), Sound.valueOf(config.getString("sound-settings.on-capture")),
                            (float)config.getDouble("sound-settings.volume"), (float)config.getDouble("sound-settings.pitch"));
                }
                if (config.getBoolean("effect-settings.enable-effects")) {
                    giveEffect(instance, p);
                }
                if (config.getBoolean("logging-settings.logging-pas")) {
                	instance.logAction("log-format.captured", p, date);
                }
                String msg = Config.broadcasts_captured.replace("%player%", p.getName()).replace("%ip%", Utils.getIp(p));
                if (config.getBoolean("message-settings.enable-broadcasts")) {
                	if (p.hasPermission("serverprotector.admin")) {
                		p.sendMessage(msg);
                	}
                }
                if (config.getBoolean("message-settings.enable-console-broadcasts")) {
                    Bukkit.getConsoleSender().sendMessage(msg);
                }
            }
        }
    }

    private void giveEffect(ServerProtector plugin, Player p) {
        Bukkit.getScheduler().runTask(plugin, () -> {
        	FileConfiguration config = instance.getConfig();
            for (String s : config.getStringList("effect-settings.effects")) {
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
                        if (config.getBoolean("message-settings.send-titles")) {
                            p.sendTitle(Config.titles_title, Config.titles_subtitle, 10, 70, 20);
                            return;
                        }
                    }
                }
            }
        }).runTaskTimerAsynchronously(instance, 0L, config.getInt("message-settings.delay") * 20L);
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
        }).runTaskTimerAsynchronously(instance, 0L, 20L);
    }

    public void startPermsCheck() {
        FileConfiguration config = instance.getConfig();
        (new BukkitRunnable() {
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    for (String badperms : config.getStringList("blacklisted-perms")) {
                        if (p.hasPermission(badperms) && !config.getStringList("excluded-players").contains(p.getName())) {
                            checkFail(instance, p, config.getStringList("commands.have-blacklisted-perm"));
                        }
                    }
                }
            }
        }).runTaskTimerAsynchronously(instance, 0L, 20L);
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
                    	if (config.getBoolean("bossbar-settings.enable-bossbar")) {
                    		bossbar = Bukkit.createBossBar(Config.bossbar_message.replace("%time%", config.getString("punish-settings.time")), 
                    			BarColor.valueOf(config.getString("bossbar-settings.bar-color")), 
                    			BarStyle.valueOf(config.getString("bossbar-settings.bar-style")));
                    		bossbar.addPlayer(p);
                    	}
                    } else if (instance.login.contains(p.getName())) {
                    	instance.time.put(p, instance.time.get(p) + 1);
                    	if (config.getBoolean("bossbar-settings.enable-bossbar")) {
                    		bossbar.setTitle(ServerProtector.getMessage("bossbar.message").replace("%time%",
                    	    	Integer.toString(config.getInt("punish-settings.time") - instance.time.get(p))));
                    		double percents = (config.getInt("punish-settings.time") - instance.time.get(p))/config.getDouble("punish-settings.time");
                    		bossbar.setProgress(percents);
                    		bossbar.addPlayer(p);
                    	}
                    }
                    if (!noTimeLeft(p) && config.getBoolean("punish-settings.enable-time")) {
                        checkFail(instance, p, config.getStringList("commands.failed-time"));
                        bossbar.removePlayer(p);
                    }
                }
            }
        }).runTaskTimerAsynchronously(instance, 0L, 20L);
    }

    private boolean noTimeLeft(Player p) {
        FileConfiguration config = instance.getConfig();
        if (!instance.time.containsKey(p)) {
            return true;
        }
        return (instance.time.get(p) < config.getInt("punish-settings.time"));
    }
}
