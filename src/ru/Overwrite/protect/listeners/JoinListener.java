package ru.Overwrite.protect.listeners;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.Overwrite.protect.Main;
import ru.Overwrite.protect.utils.Config;
import ru.Overwrite.protect.utils.Utils;

import java.util.Date;
import java.util.List;

import static ru.Overwrite.protect.Main.DATE_FORMAT;

public class JoinListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        Date date = new Date();
        FileConfiguration message = Config.getFile("message.yml");
        FileConfiguration config = Main.getInstance().getConfig();
        Player p = e.getPlayer();
        Bukkit.getServer().getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            if (!(config.getBoolean("secure-settings.enable-excluded-players") && config.getStringList("excluded-players").contains(p.getName()))) {
                if (Main.getInstance().isPermissions(p)) {
                    if (!Main.getInstance().ips.containsKey(p.getName()+Utils.getIp(p)) && config.getBoolean("session-settings.session")) {
                        Main.getInstance().login.put(p, 0);
                        if (config.getBoolean("effect-settings.enable-effects")) {
                            giveEffect(Main.getInstance(), p);
                        }
                    }
                    if (!config.getStringList("ip-whitelist").contains(Utils.getIp(p)) && config.getBoolean("secure-settings.enable-ip-whitelist")) {
                        checkFail(Main.getInstance(), p, config.getStringList("commands.not-admin-ip"));
                    }
                    if (config.getBoolean("logging-settings.logging-join")) {
                        Main.getInstance().logToFile((message.getString("log-format.joined").replace("%player%", p.getName()).replace("%ip%", Utils.getIp(p))
                                .replace("%date%", DATE_FORMAT.format(date))));
                    }
                    String msg = Main.getMessageFull("broadcasts.joined", s -> s.replace("%player%", p.getName()).replace("%ip%", Utils.getIp(p)));
                    if (config.getBoolean("message-settings.enable-console-broadcasts")) {
                        Bukkit.getConsoleSender().sendMessage(msg);
                    }
                    if (config.getBoolean("message-settings.enable-broadcasts")) {
                        Bukkit.broadcast(msg, "serverprotector.admin");
                    }
                }
            }
        });
    }

    private static void checkFail(Main plugin, Player p, List<String> command) {
        Bukkit.getServer().getScheduler().runTask(plugin, () -> {
            for (String c : command) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c.replace("%player%", p.getName()));
            }
        });
    }

    private static void giveEffect(Main plugin, Player p) {
        FileConfiguration config = Main.getInstance().getConfig();
        Bukkit.getServer().getScheduler().runTask(plugin, () -> {
            for (String s : config.getStringList("effect-settings.effects")) {
                PotionEffectType types = PotionEffectType.getByName(s.split(":")[0].toUpperCase());
                int level = Integer.parseInt(s.split(":")[1]) - 1;
                p.addPotionEffect(new PotionEffect(types, 99999, level));
            }
        });
    }
}
