package ru.overwrite.protect;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import ru.overwrite.protect.utils.Utils;

import java.util.Date;
import java.util.List;

public class Runner extends BukkitRunnable {
    public void run() {
        FileConfiguration config = ServerProtector.getInstance().getConfig();
        for (Player p : Bukkit.getOnlinePlayers()) {
            Date date = new Date();
            if (ServerProtector.getInstance().login.containsKey(p)) {
                continue;
            }
            if (ServerProtector.getInstance().isPermissions(p) &&
                    !(config.getBoolean("secure-settings.enable-excluded-players") && config.getStringList("excluded-players").contains(p.getName())) &&
                    !ServerProtector.getInstance().ips.contains(p.getName()+Utils.getIp(p))) {
            	ServerProtector.getInstance().login.put(p, 0);
                if (config.getBoolean("sound-settings.enable-sounds")) {
                    p.playSound(p.getLocation(), Sound.valueOf(config.getString("sound-settings.on-capture")),
                            (float)config.getDouble("sound-settings.volume"), (float)config.getDouble("sound-settings.pitch"));
                }
                if (config.getBoolean("effect-settings.enable-effects")) {
                    giveEffect(ServerProtector.getInstance(), p);
                }
                if (config.getBoolean("logging-settings.logging-pas")) {
                	ServerProtector.getInstance().logAction("log-format.captured", p, date);
                }
                String msg = ServerProtector.getMessagePrefixed("broadcasts.captured", s -> s.replace("%player%", p.getName()).replace("%ip%", Utils.getIp(p)));
                if (config.getBoolean("message-settings.enable-broadcasts")) {
                    Bukkit.broadcast(msg, "serverprotector.admin");
                }
                if (config.getBoolean("message-settings.enable-console-broadcasts")) {
                    Bukkit.getConsoleSender().sendMessage(msg);
                }
            }
        }
    }

    private static void giveEffect(ServerProtector plugin, Player p) {
        FileConfiguration config = ServerProtector.getInstance().getConfig();
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (String s : config.getStringList("effect-settings.effects")) {
                PotionEffectType types = PotionEffectType.getByName(s.split(":")[0].toUpperCase());
                int level = Integer.parseInt(s.split(":")[1]) - 1;
                p.addPotionEffect(new PotionEffect(types, 99999, level));
            }
        });
    }

    public void adminCheck() {
        FileConfiguration config = ServerProtector.getInstance().getConfig();
        (new BukkitRunnable() {
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (ServerProtector.getInstance().login.containsKey(p) && !ServerProtector.getInstance().isAdmin(p.getName())) {
                        checkFail(ServerProtector.getInstance(), p, config.getStringList("commands.not-in-config"));
                    }
                }
            }
        }).runTaskTimerAsynchronously(ServerProtector.getInstance(), 0L, 20L);
    }

    public void startMSG() {
        FileConfiguration config = ServerProtector.getInstance().getConfig();
        (new BukkitRunnable() {
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (ServerProtector.getInstance().login.containsKey(p)) {
                        p.sendMessage(ServerProtector.getMessage("msg.message"));
                        if (config.getBoolean("message-settings.send-titles")) {
                            p.sendTitle(ServerProtector.getMessage("titles.title"), ServerProtector.getMessage("titles.subtitle"));
                            return;
                        }
                    }
                }
            }
        }).runTaskTimerAsynchronously(ServerProtector.getInstance(), 0L, config.getInt("message-settings.delay") * 20L);
    }

    public void startOpCheck() {
        FileConfiguration config = ServerProtector.getInstance().getConfig();
        (new BukkitRunnable() {
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.isOp() && !config.getStringList("op-whitelist").contains(p.getName())) {
                        checkFail(ServerProtector.getInstance(), p, config.getStringList("commands.not-in-opwhitelist"));
                    }
                }
            }
        }).runTaskTimerAsynchronously(ServerProtector.getInstance(), 0L, 20L);
    }

    public void startPermsCheck() {
        FileConfiguration config = ServerProtector.getInstance().getConfig();
        (new BukkitRunnable() {
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    for (String badperms : config.getStringList("blacklisted-perms")) {
                        if (p.hasPermission(badperms) && !config.getStringList("excluded-players").contains(p.getName())) {
                            checkFail(ServerProtector.getInstance(), p, config.getStringList("commands.have-blacklisted-perm"));
                        }
                    }
                }
            }
        }).runTaskTimerAsynchronously(ServerProtector.getInstance(), 0L, 20L);
    }

    public static void checkFail(ServerProtector plugin, Player p, List<String> command) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (String c : command) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c.replace("%player%", p.getName()));
            }
        });
    }

    public void startTimer() {
        FileConfiguration config = ServerProtector.getInstance().getConfig();
        (new BukkitRunnable() {
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (ServerProtector.getInstance().login.containsKey(p) && !ServerProtector.getInstance().time.containsKey(p)) {
                    	ServerProtector.getInstance().time.put(p, 1);
                    } else if (ServerProtector.getInstance().login.containsKey(p)) {
                    	ServerProtector.getInstance().time.put(p, ServerProtector.getInstance().time.get(p) + 1);
                    }
                    if (!noTimeLeft(p) && config.getBoolean("punish-settings.enable-time")) {
                        checkFail(ServerProtector.getInstance(), p, config.getStringList("commands.failed-time"));
                    }
                }
            }
        }).runTaskTimerAsynchronously(ServerProtector.getInstance(), 0L, 20L);
    }

    public static boolean noTimeLeft(Player p) {
        FileConfiguration config = ServerProtector.getInstance().getConfig();
        if (!ServerProtector.getInstance().time.containsKey(p))
            return true;
        return (ServerProtector.getInstance().time.get(p) < config.getInt("punish-settings.time"));
    }
}
