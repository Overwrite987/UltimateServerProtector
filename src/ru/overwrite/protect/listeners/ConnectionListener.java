package ru.overwrite.protect.listeners;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.overwrite.protect.ServerProtector;
import ru.overwrite.protect.utils.Utils;

import java.util.Date;
import java.util.List;

public class ConnectionListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        Date date = new Date();
        FileConfiguration config = ServerProtector.getInstance().getConfig();
        Player p = e.getPlayer();
        Bukkit.getScheduler().runTaskAsynchronously(ServerProtector.getInstance(), () -> {
            if (!(config.getBoolean("secure-settings.enable-excluded-players") && config.getStringList("excluded-players").contains(p.getName()))) {
                if (ServerProtector.getInstance().isPermissions(p)) {
                    if (!ServerProtector.getInstance().ips.contains(p.getName()+Utils.getIp(p)) && config.getBoolean("session-settings.session")) {
                    	ServerProtector.getInstance().login.put(p, 0);
                        if (config.getBoolean("effect-settings.enable-effects")) {
                            giveEffect(ServerProtector.getInstance(), p);
                        }
                    }
                    for (String s : config.getStringList("ip-whitelist")) {
                      if (config.getBoolean("secure-settings.enable-ip-whitelist") && !Utils.getIp(p).startsWith(s)) {
                          checkFail(ServerProtector.getInstance(), p, config.getStringList("commands.not-admin-ip"));
                      }
                    }
                    if (config.getBoolean("logging-settings.logging-join")) {
                    	ServerProtector.getInstance().logAction("log-format.joined", p, date);
                    }
                    String msg = ServerProtector.getMessagePrefixed("broadcasts.joined", s -> s.replace("%player%", p.getName()).replace("%ip%", Utils.getIp(p)));
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ServerProtector.getInstance().login.remove(player);
        ServerProtector.getInstance().time.remove(player);
    }

    private static void checkFail(ServerProtector plugin, Player p, List<String> command) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (String c : command) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c.replace("%player%", p.getName()));
            }
        });
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
}
