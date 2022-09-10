package ru.Overwrite.protect;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.lang.reflect.Constructor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.Overwrite.protect.listeners.*;
import ru.Overwrite.protect.utils.*;

public final class Main extends JavaPlugin {

  public HashMap<String, String> ips = new HashMap<>();

  public HashMap<Player, Integer> login = new HashMap<>();
  
  public HashMap<Player, Integer> time = new HashMap<>();
  
  public List<String> permissions;
  
  private static Main instance;
  
  public static Main getInstance() {
    return instance;
  }
  
  public void onEnable() {
	  if (getServer().getName() == "CraftBukkit") {
		  getLogger().info("§6============= §6! WARNING ! §c=============");
		  getLogger().info("§eЭтот плагин работает только на Paper и его форках!");
		  getLogger().info("§eСкачать Paper для новых версий: §ahttps://papermc.io/downloads");
		  getLogger().info("§eСкачать Paper для старых версий: §ahttps://papermc.io/legacy §7((в тесте выбирайте 2 вариант ответа))");
		  getLogger().info("§6============= §6! WARNING ! §c=============");
		  setEnabled(false);
		  return;
	  }
	  long startTime = System.currentTimeMillis();
	  Date date = new Date();
	  SimpleDateFormat formatter = new SimpleDateFormat("[dd-MM-yyyy] HH:mm:ss -");
	  instance = this;
	  saveDefaultConfig();
	    Bukkit.getPluginManager().registerEvents(new ChatListener(), this);
	    Bukkit.getPluginManager().registerEvents(new JoinListener(), this);
	    Bukkit.getPluginManager().registerEvents(new LeaveListener(), this);
	    Bukkit.getPluginManager().registerEvents(new MainListener(), this);
	    Bukkit.getPluginManager().registerEvents(new AdditionalListener(), this);
	    FileConfiguration data = Config.getFile(getConfig().getString("main-settings.data-file"));
	    Config.save(data, getConfig().getString("main-settings.data-file"));
	    FileConfiguration message = Config.getFile("message.yml");
	    Config.save(message, "message.yml");
	    if (getConfig().getBoolean("main-settings.use-command")) {
	        try {
	          PluginCommand command = null;
	          CommandMap map = null;
	          Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(new Class[] { String.class, Plugin.class });
	          c.setAccessible(true);
	          command = c.newInstance(new Object[] { getConfig().getString("main-settings.pas-command"), this });
	          if (Bukkit.getPluginManager() instanceof SimplePluginManager) {
	            Field f = SimplePluginManager.class.getDeclaredField("commandMap");
	            f.setAccessible(true);
	            map = (CommandMap)f.get(Bukkit.getPluginManager());
	          } 
	          if (map != null)
	            map.register(getDescription().getName(), (Command)command); 
	          if (command != null)
	            command.setExecutor((CommandExecutor)new CommandClass()); 
	        } catch (Exception e) {
	          getLogger().info("Невозможно определить команду. Вероятно поле pas-command пусто.");
	          Bukkit.getPluginManager().disablePlugin(this);
	        } 
	  } else {
		getLogger().info("Для ввода пароля используется чат!");
	  }
	    getCommand("ultimateserverprotector").setExecutor((CommandExecutor)new CommandClass());
	    permissions = getConfig().getStringList("permissions");
	    Bukkit.getScheduler().runTaskTimerAsynchronously(instance, Runner::run, 20L, 40L);
	    Runner.startMSG();
	    if (getConfig().getBoolean("main-settings.enable-metrics")) {
          new Metrics(this, 13347);
	    }
	    if (getConfig().getBoolean("main-settings.update-checker")) {
	    new UpdateChecker(this, 97585).getVersion(version -> {
            if (this.getDescription().getVersion().equals(version)) {
            	getLogger().info("========================================");
            	getLogger().info("");
                getLogger().info("Вы используете последнюю версию плагина!");
                getLogger().info("");
                getLogger().info("========================================");
            } else {
            	getLogger().info("========================================");
                getLogger().info("Вы используете устаревшую версию плагина. Загрузите новую версию по ссылке ниже");
                getLogger().info("http://rubukkit.org/threads/admin-sec-ultimateserverprotector-plagin-na-unikalnyj-admin-parol-dlja-kazhdogo-igroka.177400/");
                getLogger().info("========================================");
            }
          });
        }
	    if (getConfig().getBoolean("punish-settings.enable-time")) {
	      Runner.startTimer();
	    }
	    if (getConfig().getBoolean("punish-settings.notadmin-punish")) {
	      Runner.adminCheck();
	    }
	    if (getConfig().getBoolean("secure-settings.enable-op-whitelist")) {
	      Runner.startOpCheck();
		}
	    if (getConfig().getBoolean("secure-settings.enable-permission-blacklist")) {
		  Runner.startPermsCheck();
		}
	    if (getConfig().getBoolean("logging-settings.logging-enable-disable")) {
	      logToFile(message.getString("log-format.enabled").replace("%date%", formatter.format(date)));
 	  	}
	    long endTime = System.currentTimeMillis();
	    getLogger().info("Плагин включен за " + (endTime - startTime) + " милисекунд(ы)");
	  }
  
  public Boolean isPermissions(Player p) {
	    boolean bool = false;
	    for (String s : Main.getInstance().permissions) {
	    if (p.isOp() || p.hasPermission("serverprotector.protect") || p.hasPermission(s)) 
	        bool = true; 
	    } 
	    return Boolean.valueOf(bool);
	  }
  
  public Boolean isAdmin(String nick) {
	  FileConfiguration data = Config.getFile(getConfig().getString("main-settings.data-file"));
	    return Boolean.valueOf(data.contains("data.$nick".replace("$nick", nick)));
	}
  
  public void logToFile(String message) {
	    try {
	      File dataFolder = getDataFolder();
	      if (!dataFolder.exists()) {
	        dataFolder.mkdir(); 
	      }
	      File saveTo = new File(getDataFolder(), "log.yml");
	      if (!saveTo.exists()) {
	        saveTo.createNewFile();
	      }
	      FileWriter fw = new FileWriter(saveTo, true);
	      PrintWriter pw = new PrintWriter(fw);
	      pw.println(message);
	      pw.flush();
	      pw.close();
	    } catch (IOException e) {
	      e.printStackTrace();
	    } 
	  }
	    
  public void onDisable() {
	 Date date = new Date();
	 FileConfiguration message = Config.getFile("message.yml");
	 SimpleDateFormat formatter = new SimpleDateFormat("[dd-MM-yyy] HH:mm:ss -");
	Bukkit.getScheduler().cancelTasks(this);
	 login.clear();
	 ips.clear();
	 time.clear();
	 permissions.clear();
	 CommandClass.attemps.clear();
    if (getConfig().getBoolean("logging-settings.logging-enable-disable")) {
     logToFile(message.getString("log-format.disabled").replace("%date%", formatter.format(date)));
	}
    if (getConfig().getBoolean("message-settings.enable-broadcasts")) {
    	Bukkit.broadcast(RGBcolors.translate(getConfig().getString("main-settings.prefix") + (message.getString("broadcasts.disabled"))), "serverprotector.admin");
   	}
    if (getConfig().getBoolean("secure-settings.shutdown-on-disable")) {
    	Bukkit.shutdown();
   	}
  }
}
  
