package ru.Overwrite.protect.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
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
import ru.Overwrite.protect.utils.RGBcolors;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ChatListener implements Listener {

    ArrayList<String> list;

    public static HashMap<Player, Integer> attemps = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onChat(AsyncPlayerChatEvent e) {
        FileConfiguration message = Config.getFile("message.yml");
        FileConfiguration config = Main.getInstance().getConfig();
        FileConfiguration data = Config.getFile(config.getString("main-settings.data-file"));
        Player p = e.getPlayer();
        String msg = e.getMessage();
        if (Main.getInstance().login.containsKey(p)) {
            if (!config.getBoolean("main-settings.use-command")) {
                if (msg.equals(data.getString("data." + p.getName() + ".pass"))) {
                    e.setCancelled(true);
                    correctPassword(p);
                } else {
                    p.sendMessage(RGBcolors.translate((config.getString("main-settings.prefix") + message.getString("msg.incorrect"))));
                    e.setCancelled(true);
                    onFail(p);
                    if (!attempsFULL(p) && config.getBoolean("punish-settings.enable-attemps")) {
                        e.setCancelled(true);
                        failedPass(Main.getInstance(), p);
                    }
                }
            } else {
                e.setCancelled(true);
            }
        }
    }

    private static void failedPass(Main plugin, Player p) {
        Bukkit.getServer().getScheduler().runTask(plugin, () -> {
            FileConfiguration config = Main.getInstance().getConfig();
            for (String c : config.getStringList("commands.failed-pass")) {
                Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), c.replace("%player%", p.getName()));
            }
        });
    }

    private void onFail(Player p) {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("[dd-MM-yyy] HH:mm:ss -");
        FileConfiguration config = Main.getInstance().getConfig();
        FileConfiguration message = Config.getFile("message.yml");
        if (!attemps.containsKey(p)) {
            attemps.put(p, 1);
            if (config.getBoolean("sound-settings.enable-sounds")) {
                p.playSound(p.getLocation(), Sound.valueOf(config.getString("sound-settings.on-pas-fail")),
                        (float)config.getDouble("sound-settings.volume"), (float)config.getDouble("sound-settings.pitch"));
            }
            if (config.getBoolean("message-settings.enable-broadcasts")) {
                Bukkit.broadcast(RGBcolors.translate(config.getString("main-settings.prefix") +
                        ((message.getString("broadcasts.failed").replace("%player%", p.getName()).replace("%ip%", p.getAddress().getAddress().getHostAddress())))), "serverprotector.admin");
            }
            if (config.getBoolean("logging-settings.logging-pas")) {
                Main.getInstance().logToFile((message.getString("log-format.failed").replace("%player%", p.getName()).replace("%ip%", p.getAddress().getAddress().getHostAddress())
                        .replace("%date%", formatter.format(date))));
            }
            if (config.getBoolean("message-settings.enable-console-broadcasts")) {
                Bukkit.getConsoleSender().sendMessage(RGBcolors.translate(config.getString("main-settings.prefix") +
                        ((message.getString("broadcasts.failed").replace("%player%", p.getName()).replace("%ip%", p.getAddress().getAddress().getHostAddress())))));
            }
        } else {
            attemps.put(p,(attemps.get(p)).intValue() + 1);
            if (config.getBoolean("sound-settings.enable-sounds")) {
                p.playSound(p.getLocation(), Sound.valueOf(config.getString("sound-settings.on-pas-fail")),
                        (float)config.getDouble("sound-settings.volume"), (float)config.getDouble("sound-settings.pitch"));
            }
            if (config.getBoolean("message-settings.enable-broadcasts")) {
                Bukkit.broadcast(RGBcolors.translate(config.getString("main-settings.prefix") +
                        ((message.getString("broadcasts.failed").replace("%player%", p.getName()).replace("%ip%", p.getAddress().getAddress().getHostAddress())))), "serverprotector.admin");
            }
            if (config.getBoolean("logging-settings.logging-pas")) {
                Main.getInstance().logToFile((message.getString("log-format.failed").replace("%player%", p.getName()).replace("%ip%", p.getAddress().getAddress().getHostAddress())
                        .replace("%date%", formatter.format(date))));
            }
            if (config.getBoolean("message-settings.enable-console-broadcasts")) {
                Bukkit.getConsoleSender().sendMessage(RGBcolors.translate(config.getString("main-settings.prefix") +
                        ((message.getString("broadcasts.failed").replace("%player%", p.getName()).replace("%ip%", p.getAddress().getAddress().getHostAddress())))));
            }
        }
    }


    public boolean attempsFULL(Player p) {
        if (!attemps.containsKey(p))
            return true;
        FileConfiguration config = Main.getInstance().getConfig();
        return (attemps.get(p) < config.getInt("punish-settings.max-attempts"));
    }

    public void correctPassword(Player p) {
        Date date = new Date();
        FileConfiguration message = Config.getFile("message.yml");
        SimpleDateFormat formatter = new SimpleDateFormat("[dd-MM-yyy] HH:mm:ss -");
        FileConfiguration config = Main.getInstance().getConfig();
        Bukkit.getServer().getScheduler().runTask(Main.getInstance(), () -> {
            Main.getInstance().login.remove(p, 0);
            p.sendMessage(RGBcolors.translate(config.getString("main-settings.prefix") + message.getString("msg.correct")));
            if (config.getBoolean("sound-settings.enable-sounds")) {
                p.playSound(p.getLocation(), Sound.valueOf(config.getString("sound-settings.on-pas-correct")),
                        (float)config.getDouble("sound-settings.volume"), (float)config.getDouble("sound-settings.pitch"));
            }
            if (config.getBoolean("effect-settings.enable-effects")) {
                for (PotionEffect s : p.getActivePotionEffects()) {
                    p.removePotionEffect(s.getType());
                }
            }
            if (config.getBoolean("message-settings.enable-broadcasts")) {
                Bukkit.broadcast(RGBcolors.translate(config.getString("main-settings.prefix") +
                        (RGBcolors.translate(message.getString("broadcasts.passed").replace("%player%", p.getName()).replace("%ip%", p.getAddress().getAddress().getHostAddress())))), "serverprotector.admin");
            }
            if (config.getBoolean("message-settings.enable-console-broadcasts")) {
                Bukkit.getConsoleSender().sendMessage(RGBcolors.translate(config.getString("main-settings.prefix") +
                        ((message.getString("broadcasts.passed").replace("%player%", p.getName()).replace("%ip%", p.getAddress().getAddress().getHostAddress())))));
            }
            if (config.getBoolean("session-settings.session")) {
                Main.getInstance().ips.put(p.getName()+p.getAddress().getAddress().getHostAddress(), "focku");
            }
            if (config.getBoolean("logging-settings.logging-pas")) {
                Main.getInstance().logToFile(message.getString("log-format.passed").replace("%player%", p.getName()).replace("%ip%", p.getAddress().getAddress().getHostAddress())
                        .replace("%date%", formatter.format(date)));
            }
            if (config.getBoolean("session-settings.session-time-enabled")) {
                Main.getInstance().getServer().getScheduler().scheduleAsyncDelayedTask(Main.getInstance(), new Runnable() {
                    public void run() {
                        if (!Main.getInstance().login.containsKey(p));
                        Main.getInstance().ips.remove(p.getName()+p.getAddress().getAddress().getHostAddress());
                    }
                }, config.getInt("session-settings.session-time")*20);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (Main.getInstance().login.containsKey(p)) {
            e.setCancelled(true);
        }
        FileConfiguration config = Main.getInstance().getConfig();
        list = (ArrayList<String>)config.getStringList("allowed-commands");
        if (config.getBoolean("main-settings.use-command")) {
            for (String command : list) {
                if ((e.getMessage().equalsIgnoreCase("/" + config.getString("main-settings.pas-command")) || e.getMessage().toLowerCase().startsWith("/"+ config.getString("main-settings.pas-command") + " "))
                        || (e.getMessage().toLowerCase().startsWith(command + " ") || e.getMessage().equalsIgnoreCase(command)))
                    e.setCancelled(false);
            }
        }
    }

}
