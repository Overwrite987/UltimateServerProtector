package ru.Overwrite.protect.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.potion.PotionEffect;
import ru.Overwrite.protect.Main;
import ru.Overwrite.protect.utils.Config;
import ru.Overwrite.protect.utils.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ChatListener implements Listener {

    private static final Map<Player, Integer> attempts = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e) {
        FileConfiguration config = Main.getInstance().getConfig();
        FileConfiguration data = Config.getFile(config.getString("main-settings.data-file"));
        Player p = e.getPlayer();
        String msg = e.getMessage();
        if (Main.getInstance().login.containsKey(p)) {
            e.setCancelled(true);
            if (!config.getBoolean("main-settings.use-command")) {
                if (msg.equals(data.getString("data." + p.getName() + ".pass"))) {
                    correctPassword(p);
                } else {
                    p.sendMessage(Main.getMessageFull("msg.incorrect"));
                    onFail(p);
                    if (!attemptsMax(p) && config.getBoolean("punish-settings.enable-attemps")) {
                        failedPass(Main.getInstance(), p);
                    }
                }
            }
        }
    }

    private static void failedPass(Main plugin, Player p) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            FileConfiguration config = Main.getInstance().getConfig();
            for (String c : config.getStringList("commands.failed-pass")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c.replace("%player%", p.getName()));
            }
        });
    }

    private void onFail(Player p) {
        Date date = new Date();
        FileConfiguration config = Main.getInstance().getConfig();
        attempts.put(p, attempts.getOrDefault(p, 0) + 1);
        if (config.getBoolean("sound-settings.enable-sounds")) {
            p.playSound(p.getLocation(), Sound.valueOf(config.getString("sound-settings.on-pas-fail")),
                    (float)config.getDouble("sound-settings.volume"), (float)config.getDouble("sound-settings.pitch"));
        }
        if (config.getBoolean("logging-settings.logging-pas")) {
            Main.getInstance().logAction("log-format.failed", p, date);
        }
        String msg = Main.getMessageFull("broadcasts.failed", s -> s.replace("%player%", p.getName()).replace("%ip%", Utils.getIp(p)));
        if (config.getBoolean("message-settings.enable-broadcasts")) {
            Bukkit.broadcast(msg, "serverprotector.admin");
        }
        if (config.getBoolean("message-settings.enable-console-broadcasts")) {
            Bukkit.getConsoleSender().sendMessage(msg);
        }
    }


    public boolean attemptsMax(Player p) {
        if (!attempts.containsKey(p))
            return true;
        FileConfiguration config = Main.getInstance().getConfig();
        return (attempts.get(p) < config.getInt("punish-settings.max-attempts"));
    }

    public void correctPassword(Player p) {
        Date date = new Date();
        FileConfiguration config = Main.getInstance().getConfig();
        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
            Main.getInstance().login.remove(p, 0);
            p.sendMessage(Main.getMessageFull("msg.correct"));
            if (config.getBoolean("sound-settings.enable-sounds")) {
                p.playSound(p.getLocation(), Sound.valueOf(config.getString("sound-settings.on-pas-correct")),
                        (float)config.getDouble("sound-settings.volume"), (float)config.getDouble("sound-settings.pitch"));
            }
            if (config.getBoolean("effect-settings.enable-effects")) {
                for (PotionEffect s : p.getActivePotionEffects()) {
                    p.removePotionEffect(s.getType());
                }
            }
            if (config.getBoolean("session-settings.session")) {
                Main.getInstance().ips.put(p.getName()+Utils.getIp(p), "focku");
            }
            if (config.getBoolean("logging-settings.logging-pas")) {
                Main.getInstance().logAction("log-format.passed", p, date);
            }
            if (config.getBoolean("session-settings.session-time-enabled")) {
                Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), () -> {
                    if (!Main.getInstance().login.containsKey(p)) {
                        Main.getInstance().ips.remove(p.getName() + Utils.getIp(p));
                    }
                }, config.getInt("session-settings.session-time") * 20L);
            }
            String msg = Main.getMessageFull("broadcasts.passed", s -> s.replace("%player%", p.getName()).replace("%ip%", Utils.getIp(p)));
            if (config.getBoolean("message-settings.enable-broadcasts")) {
                Bukkit.broadcast(msg, "serverprotector.admin");
            }
            if (config.getBoolean("message-settings.enable-console-broadcasts")) {
                Bukkit.getConsoleSender().sendMessage(msg);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (!Main.getInstance().login.containsKey(p)) return;
        e.setCancelled(true);
        FileConfiguration config = Main.getInstance().getConfig();
        if (config.getBoolean("main-settings.use-command")) {
            String message = e.getMessage();
            String label = cutCommand(message).toLowerCase(Locale.ROOT);
            if (label.equals("/" + config.getString("main-settings.pas-command"))) {
                e.setCancelled(false);
            } else for (String command : config.getStringList("allowed-commands")) {
                if (label.equals(command) || message.equalsIgnoreCase(command)) {
                    e.setCancelled(false);
                    break;
                }
            }
        }
    }

    private static String cutCommand(String str) {
        int index = str.indexOf(' ');
        return index == -1 ? str : str.substring(0, index);
    }
}
