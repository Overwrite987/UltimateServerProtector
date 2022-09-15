package ru.Overwrite.protect;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import ru.Overwrite.protect.utils.Utils;

import java.util.Date;
import java.util.List;

public class Runner {
    public static void run() {
        FileConfiguration config = Main.getInstance().getConfig();
        for (Player p : Bukkit.getOnlinePlayers()) {
            Date date = new Date();
            if (Main.getInstance().login.containsKey(p)) {
                continue;
            }
            if (Main.getInstance().isPermissions(p) &&
                    !(config.getBoolean("secure-settings.enable-excluded-players") && config.getStringList("excluded-players").contains(p.getName())) &&
                    !Main.getInstance().ips.contains(p.getName()+Utils.getIp(p))) {
                Main.getInstance().login.put(p, 0);
                if (config.getBoolean("sound-settings.enable-sounds")) {
                    p.playSound(p.getLocation(), Sound.valueOf(config.getString("sound-settings.on-capture")),
                            (float)config.getDouble("sound-settings.volume"), (float)config.getDouble("sound-settings.pitch"));
                }
                if (config.getBoolean("effect-settings.enable-effects")) {
                    giveEffect(Main.getInstance(), p);
                }
                if (config.getBoolean("logging-settings.logging-pas")) {
                    Main.getInstance().logAction("log-format.captured", p, date);
                }
                String msg = Main.getMessageFull("broadcasts.captured", s -> s.replace("%player%", p.getName()).replace("%ip%", Utils.getIp(p)));
                if (config.getBoolean("message-settings.enable-broadcasts")) {
                    Bukkit.broadcast(msg, "serverprotector.admin");
                }
                if (config.getBoolean("message-settings.enable-console-broadcasts")) {
                    Bukkit.getConsoleSender().sendMessage(msg);
                }
            }
        }
    }

    private static void giveEffect(Main plugin, Player p) {
        FileConfiguration config = Main.getInstance().getConfig();
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (String s : config.getStringList("effect-settings.effects")) {
                PotionEffectType types = PotionEffectType.getByName(s.split(":")[0].toUpperCase());
                int level = Integer.parseInt(s.split(":")[1]) - 1;
                p.addPotionEffect(new PotionEffect(types, 99999, level));
            }
        });
    }

    public static void adminCheck() {
        FileConfiguration config = Main.getInstance().getConfig();
        (new BukkitRunnable() {
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (Main.getInstance().login.containsKey(p) && !Main.getInstance().isAdmin(p.getName()))
                        checkFail(Main.getInstance(), p, config.getStringList("commands.not-in-config"));
                }
            }
        }).runTaskTimerAsynchronously(Main.getInstance(), 0L, 20L);
    }

    public static void startMSG() {
        FileConfiguration config = Main.getInstance().getConfig();
        (new BukkitRunnable() {
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (Main.getInstance().login.containsKey(p)) {
                        p.sendMessage(Main.getMessage("msg.message"));
                        if (config.getBoolean("message-settings.send-titles"))
                            p.sendTitle(Main.getMessage("titles.title"), Main.getMessage("titles.subtitle"));
                        return;
                    }
                }
            }
        }).runTaskTimerAsynchronously(Main.getInstance(), 0L, config.getInt("message-settings.delay") * 20L);
    }

    public static void startOpCheck() {
        FileConfiguration config = Main.getInstance().getConfig();
        (new BukkitRunnable() {
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.isOp() && !config.getStringList("op-whitelist").contains(p.getName())) {
                        checkFail(Main.getInstance(), p, config.getStringList("commands.not-in-opwhitelist"));
                    }
                }
            }
        }).runTaskTimerAsynchronously(Main.getInstance(), 0L, 20L);
    }

    public static void startPermsCheck() {
        FileConfiguration config = Main.getInstance().getConfig();
        (new BukkitRunnable() {
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    for (String badperms : config.getStringList("blacklisted-perms"))
                        if (p.hasPermission(badperms) && !config.getStringList("excluded-players").contains(p.getName())) {
                            checkFail(Main.getInstance(), p, config.getStringList("commands.have-blacklisted-perm"));
                        }
                }
            }
        }).runTaskTimerAsynchronously(Main.getInstance(), 0L, 20L);
    }

    public static void checkFail(Main plugin, Player p, List<String> command) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (String c : command) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c.replace("%player%", p.getName()));
            }
        });
    }

    public static void startTimer() {
        FileConfiguration config = Main.getInstance().getConfig();
        (new BukkitRunnable() {
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (Main.getInstance().login.containsKey(p) && !Main.getInstance().time.containsKey(p)) {
                        Main.getInstance().time.put(p, 1);
                    } else if (Main.getInstance().login.containsKey(p)) {
                        Main.getInstance().time.put(p, Main.getInstance().time.get(p) + 1);
                    }
                    if (!noTimeLeft(p) && config.getBoolean("punish-settings.enable-time"))
                        checkFail(Main.getInstance(), p, config.getStringList("commands.failed-time"));
                }
            }
        }).runTaskTimerAsynchronously(Main.getInstance(), 0L, 20L);
    }

    public static boolean noTimeLeft(Player p) {
        FileConfiguration config = Main.getInstance().getConfig();
        if (!Main.getInstance().time.containsKey(p))
            return true;
        return (Main.getInstance().time.get(p) < config.getInt("punish-settings.time"));
    }
}
