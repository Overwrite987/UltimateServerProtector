package ru.overwrite.protect.bukkit.checker;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.Runner;
import ru.overwrite.protect.bukkit.utils.Config;
import ru.overwrite.protect.bukkit.utils.Utils;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.api.ServerProtectorCaptureEvent;

public class PaperRunner implements Runner {
	
	private final ServerProtectorManager instance;
	private final ServerProtectorAPI api;
	private final Config pluginConfig;
	
	public PaperRunner(ServerProtectorManager plugin) {
        instance = plugin;
        api = plugin.getPluginAPI();
        pluginConfig = plugin.getPluginConfig();
    }
	
    public void mainCheck() {
    	Bukkit.getAsyncScheduler().runAtFixedRate(instance, (tt) -> {
    		for (Player p : Bukkit.getOnlinePlayers()) {
    			if (instance.isExcluded(p)) {
    				return;
    			}
    			if (api.isCaptured(p)) {
    				return;
    			}
    			if (!instance.isPermissions(p)) {
        			return;
        		}
        		String playerName = p.getName();
        		if (!instance.ips.contains(playerName + Utils.getIp(p)) && !instance.isAuthorised(p)) {
    				ServerProtectorCaptureEvent captureEvent = new ServerProtectorCaptureEvent(p);
    				captureEvent.callEvent();
    				if (captureEvent.isCancelled()) {
    					return;
    				}
    				api.capturePlayer(p);
    				if (pluginConfig.sound_settings_enable_sounds) {
    					p.playSound(p.getLocation(), Sound.valueOf(pluginConfig.sound_settings_on_capture),
    							pluginConfig.sound_settings_volume, pluginConfig.sound_settings_pitch);
    				}
    				if (pluginConfig.effect_settings_enable_effects) {
    					instance.giveEffect(instance, p);
    				}
    				if (pluginConfig.logging_settings_logging_pas) {
    					instance.logAction("log-format.captured", p, new Date());
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
    		
    	}, 5L, 40L * 50L, TimeUnit.MILLISECONDS);
    }

    public void adminCheck(FileConfiguration config) {
    	Bukkit.getAsyncScheduler().runAtFixedRate(instance, (tt) -> {
    		if (instance.login.isEmpty()) return;
    		for (Player p : Bukkit.getOnlinePlayers()) {
    			if (api.isCaptured(p) && !instance.isAdmin(p.getName())) {
                    instance.checkFail(p, config.getStringList("commands.not-in-config"));
                }
    		}
        }, 0L, 20L * 50L, TimeUnit.MILLISECONDS);
    }

    public void startMSG(FileConfiguration config) {
    	Bukkit.getAsyncScheduler().runAtFixedRate(instance, (tt) -> {
    		if (instance.login.isEmpty()) return;
    		for (Player p : Bukkit.getOnlinePlayers()) {
    			if (api.isCaptured(p)) {
    				p.sendMessage(pluginConfig.msg_message);
    				if (pluginConfig.message_settings_send_title) {
    					p.sendTitle(pluginConfig.titles_title, pluginConfig.titles_subtitle, 10, 70, 20);
    					return;
    				}
    			}
    		}
    	}, 0L, config.getInt("message-settings.delay") * 20L * 50L, TimeUnit.MILLISECONDS);
    }

    public void startOpCheck(FileConfiguration config) {
    	Bukkit.getAsyncScheduler().runAtFixedRate(instance, (tt) -> {
    		for (Player p : Bukkit.getOnlinePlayers()) {
    			if (p.isOp() && !pluginConfig.op_whitelist.contains(p.getName())) {
    				instance.checkFail(p, config.getStringList("commands.not-in-opwhitelist"));
    			}
    		}
    	}, 0L, 20L * 50L, TimeUnit.MILLISECONDS);
    }

    public void startPermsCheck(FileConfiguration config) {
    	Bukkit.getAsyncScheduler().runAtFixedRate(instance, (tt) -> {
    		for (Player p : Bukkit.getOnlinePlayers()) {
    			for (String badperms : pluginConfig.blacklisted_perms) {
    				if (p.hasPermission(badperms) && !instance.isExcluded(p)) {
    					instance.checkFail(p, config.getStringList("commands.have-blacklisted-perm"));
    				}
    			}
    		}
    	}, 5L, 20L * 50L, TimeUnit.MILLISECONDS);
    }
    
    public void startTimer(FileConfiguration config) {
    	Bukkit.getAsyncScheduler().runAtFixedRate(instance, (tt) -> {
    		if (instance.login.isEmpty()) return;
    		for (Player p : Bukkit.getOnlinePlayers()) {
    			if (api.isCaptured(p)) {
    				if (!instance.time.containsKey(p)) {
    					instance.time.put(p, 0);
    					if (pluginConfig.bossbar_settings_enable_bossbar) {
    						Utils.bossbar = Bukkit.createBossBar(pluginConfig.bossbar_message.replace("%time%", String.valueOf(pluginConfig.punish_settings_time)), 
    								BarColor.valueOf(pluginConfig.bossbar_settings_bar_color), 
    								BarStyle.valueOf(pluginConfig.bossbar_settings_bar_style));
    						Utils.bossbar.addPlayer(p);
    					}
    				} else {
    					instance.time.put(p, instance.time.get(p) + 1);
    					if (pluginConfig.bossbar_settings_enable_bossbar && Utils.bossbar != null) {
    						Utils.bossbar.setTitle(pluginConfig.bossbar_message.replace("%time%",
    								Integer.toString(pluginConfig.punish_settings_time - instance.time.get(p))));
    						double percents = (pluginConfig.punish_settings_time - instance.time.get(p))/Double.valueOf(pluginConfig.punish_settings_time);
    						Utils.bossbar.setProgress(percents);
    						Utils.bossbar.addPlayer(p);
    					}
    				}
    				if (!noTimeLeft(p) && pluginConfig.punish_settings_enable_time) {
    					instance.checkFail(p, config.getStringList("commands.failed-time"));
    					Utils.bossbar.removePlayer(p);
    				}
    			}
    		}
    	}, 0L, 20L * 50L, TimeUnit.MILLISECONDS);
    }

    private boolean noTimeLeft(Player p) {
        return !instance.time.containsKey(p) ? true : instance.time.get(p) < pluginConfig.punish_settings_time;
    }
}
