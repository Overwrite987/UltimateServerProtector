package ru.Overwrite.protect.listeners;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import ru.Overwrite.protect.Main;
import ru.Overwrite.protect.utils.*;

public class JoinListener implements Listener {
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	  public void onJoin(PlayerJoinEvent e) {
		Date date = new Date();
		FileConfiguration message = Config.getFile("message.yml");
		SimpleDateFormat formatter = new SimpleDateFormat("[dd-MM-yyy] HH:mm:ss -");
		FileConfiguration config = Main.getInstance().getConfig();
	    Player p = e.getPlayer();
	    Bukkit.getServer().getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
	    if (!(config.getBoolean("secure-settings.enable-excluded-players") && config.getStringList("excluded-players").contains(p.getName()))) {
	     if (Main.getInstance().isPermissions(p)) {
	       if (!Main.getInstance().ips.containsKey(p.getName()+p.getAddress().getAddress().getHostAddress()) && config.getBoolean("session-settings.session")) {
		      Main.getInstance().login.put(p, 0);
		      if (config.getBoolean("effect-settings.enable-effects")) {
		    	  giveEffect(Main.getInstance(), p);
		        }
	        }
	       if (!config.getStringList("ip-whitelist").contains(p.getAddress().getAddress().getHostAddress()) && config.getBoolean("secure-settings.enable-ip-whitelist")) {
	    	   checkFail(Main.getInstance(), p, config.getStringList("commands.not-admin-ip"));
		    }
	      if (config.getBoolean("logging-settings.logging-join")) {
	    	  Main.getInstance().logToFile((message.getString("log-format.joined").replace("%player%", p.getName()).replace("%ip%", p.getAddress().getAddress().getHostAddress())
	     			  .replace("%date%", formatter.format(date))));
	        }
	      if (config.getBoolean("message-settings.enable-console-broadcasts")) {
	     	  Bukkit.getConsoleSender().sendMessage(RGBcolors.translate(config.getString("main-settings.prefix") + 
	    	  	((message.getString("broadcasts.joined").replace("%player%", p.getName()).replace("%ip%", p.getAddress().getAddress().getHostAddress())))));
	        }
	      if (config.getBoolean("message-settings.enable-broadcasts")) {
	     	   Bukkit.broadcast(RGBcolors.translate(config.getString("main-settings.prefix") + 
	     		((message.getString("broadcasts.joined").replace("%player%", p.getName()).replace("%ip%", p.getAddress().getAddress().getHostAddress())))), "serverprotector.admin");
	        }
	      }
	    }   
	  });
	}
	
	private static void checkFail(Main plugin, Player p, List<String> command) {
	    Bukkit.getServer().getScheduler().runTask(plugin, () -> {
	    	for (String c : command) {
	            Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), c.replace("%player%", p.getName())); 
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
