package ru.overwrite.protect.folia.listeners;

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
import ru.overwrite.protect.folia.ServerProtector;
import ru.overwrite.protect.folia.utils.Config;
import ru.overwrite.protect.folia.utils.Utils;

import java.util.Date;
import java.util.List;

public class ConnectionListener implements Listener {
	
	private final ServerProtector instance = ServerProtector.getInstance();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        Date date = new Date();
        Player p = e.getPlayer();
        Bukkit.getAsyncScheduler().runNow(instance, (j) -> {
        	FileConfiguration config = instance.getConfig();
            if (!instance.isExcluded(p)) {
                if (instance.isPermissions(p)) {
                    if (!instance.ips.contains(p.getName() + Utils.getIp(p)) && Config.session_settings_session) {
                    	instance.login.add(p.getName());
                        if (Config.effect_settings_enable_effects) {
                            giveEffect(instance, p);
                        }
                    }
                    if (Config.secure_settings_enable_ip_whitelist) {
                    	for (String s : config.getStringList("ip-whitelist")) {
                    		if (!Utils.getIp(p).startsWith(s)) {
                    			checkFail(instance, p, config.getStringList("commands.not-admin-ip"));
                    		}
                    	}
                    }
                    if (Config.logging_settings_logging_join) {
                    	instance.logAction("log-format.joined", p, date);
                    }
                    String msg = Config.broadcasts_joined.replace("%player%", p.getName()).replace("%ip%", Utils.getIp(p));
                    if (Config.message_settings_enable_console_broadcasts) {
                        Bukkit.getConsoleSender().sendMessage(msg);
                    }
                    if (Config.message_settings_enable_broadcasts) {
                    	if (p.hasPermission("serverprotector.admin")) {
                    		p.sendMessage(msg);
                    	}
                    }
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        instance.time.remove(player);
        instance.login.remove(player.getName());
        instance.saved.remove(player.getName());
    }

    private void checkFail(ServerProtector plugin, Player p, List<String> command) {
    	Bukkit.getGlobalRegionScheduler().run(plugin, (cf) -> {
            for (String c : command) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c.replace("%player%", p.getName()));
            }
        });
    }

    private void giveEffect(ServerProtector plugin, Player p) {
    	p.getScheduler().run(plugin, (ge) -> {
            for (String s : Config.effect_settings_effects) {
                PotionEffectType types = PotionEffectType.getByName(s.split(":")[0].toUpperCase());
                int level = Integer.parseInt(s.split(":")[1]) - 1;
                p.addPotionEffect(new PotionEffect(types, 99999, level));
            }
        }, null);
    }
}
