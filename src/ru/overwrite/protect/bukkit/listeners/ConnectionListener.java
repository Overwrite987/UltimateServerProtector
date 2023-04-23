package ru.overwrite.protect.bukkit.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.utils.Config;
import ru.overwrite.protect.bukkit.utils.Utils;

import java.util.Date;
import java.util.List;

public class ConnectionListener implements Listener {
	
	private final ServerProtectorManager instance;
	private final Config pluginConfig;
	
	public ConnectionListener(ServerProtectorManager plugin) {
        this.instance = plugin;
        pluginConfig = plugin.getPluginConfig();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        Date date = new Date();
        Player p = e.getPlayer();
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            if (!instance.isExcluded(p)) {
                if (instance.isPermissions(p)) {
                    if (!instance.ips.contains(p.getName() + Utils.getIp(p)) && pluginConfig.session_settings_session) {
                    	instance.login.add(p.getName());
                        if (pluginConfig.effect_settings_enable_effects) {
                            giveEffect(instance, p);
                        }
                    }
                    if (pluginConfig.secure_settings_enable_ip_whitelist) {
                    	for (String s : pluginConfig.ip_whitelist) {
                    		if (!Utils.getIp(p).startsWith(s)) {
                    			checkFail(instance, p, instance.getConfig().getStringList("commands.not-admin-ip"));
                    		}
                    	}
                    }
                    if (pluginConfig.logging_settings_logging_join) {
                    	instance.logAction("log-format.joined", p, date);
                    }
                    String msg = pluginConfig.broadcasts_joined.replace("%player%", p.getName()).replace("%ip%", Utils.getIp(p));
                    if (pluginConfig.message_settings_enable_console_broadcasts) {
                        Bukkit.getConsoleSender().sendMessage(msg);
                    }
                    if (pluginConfig.message_settings_enable_broadcasts) {
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

    private void checkFail(ServerProtectorManager plugin, Player p, List<String> command) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (String c : command) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c.replace("%player%", p.getName()));
            }
        });
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
}
