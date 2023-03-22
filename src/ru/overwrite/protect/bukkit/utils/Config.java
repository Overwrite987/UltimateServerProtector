package ru.overwrite.protect.bukkit.utils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.overwrite.protect.bukkit.ServerProtector;
import ru.overwrite.protect.bukkit.ServerProtectorManager;

import java.io.File;
import java.io.IOException;
import java.util.function.UnaryOperator;

public class Config {
	
	private static final ServerProtector instance = ServerProtector.getInstance();
	public static String uspmsg_consoleonly;
	public static String uspmsg_reloaded;
	public static String uspmsg_rebooted;
	public static String uspmsg_playernotfound;
	public static String uspmsg_alreadyinconfig;
	public static String uspmsg_playeradded;
	public static String uspmsg_ipadded;
	public static String uspmsg_setpassusage;
	public static String uspmsg_addopusage;
	public static String uspmsg_addipusage;
	public static String uspmsg_usage;
	public static String uspmsg_usage_reload;
	public static String uspmsg_usage_reboot;
	public static String uspmsg_usage_setpass;
	public static String uspmsg_usage_addop;
	public static String uspmsg_usage_addip;
	public static String msg_message;
	public static String msg_incorrect;
	public static String msg_correct;
	public static String msg_noneed;
	public static String msg_cantbenull;
	public static String msg_playeronly;
	public static String broadcasts_failed;
	public static String broadcasts_passed;
	public static String broadcasts_joined;
	public static String broadcasts_captured;
	public static String titles_title;
	public static String titles_subtitle;
	public static String bossbar_message;
	
	public static void loadUspMessages() {
		uspmsg_consoleonly = ServerProtector.getMessage("uspmsg.consoleonly");
		uspmsg_reloaded = ServerProtector.getMessage("uspmsg.reloaded");
		uspmsg_rebooted = ServerProtector.getMessage("uspmsg.rebooted");
		uspmsg_playernotfound = ServerProtector.getMessage("uspmsg.playernotfound");
		uspmsg_alreadyinconfig = ServerProtector.getMessage("uspmsg.alreadyinconfig");
		uspmsg_playeradded = ServerProtector.getMessage("uspmsg.playeradded");
		uspmsg_ipadded = ServerProtector.getMessage("uspmsg.ipadded");
		uspmsg_setpassusage = ServerProtector.getMessage("uspmsg.setpassusage");
		uspmsg_addopusage = ServerProtector.getMessage("uspmsg.addopusage");
		uspmsg_addipusage = ServerProtector.getMessage("uspmsg.addipusage");
		uspmsg_usage = ServerProtector.getMessage("uspmsg.usage");
		uspmsg_usage_reload = ServerProtector.getMessage("uspmsg.usage-reload");
		uspmsg_usage_reboot = ServerProtector.getMessage("uspmsg.usage-reboot");
		uspmsg_usage_setpass = ServerProtector.getMessage("uspmsg.usage-setpass");
		uspmsg_usage_addop = ServerProtector.getMessage("uspmsg.usage-addop");
		uspmsg_usage_addip = ServerProtector.getMessage("uspmsg.usage-addip");
	}
	
	public static void loadMsgMessages() {
		msg_message = ServerProtectorManager.getMessage("msg.message");
		msg_incorrect = ServerProtectorManager.getMessage("msg.imcorrect");
		msg_correct = ServerProtectorManager.getMessage("msg.correct");
		msg_noneed = ServerProtector.getMessage("msg.noneed");
		msg_cantbenull = ServerProtector.getMessage("msg.cantbenull");
		msg_playeronly = ServerProtector.getMessage("msg.playeronly");	
	}
	
	public static void loadBroadcastMessages() {
		broadcasts_failed = ServerProtector.getMessage("broadcasts.failed");
		broadcasts_passed = ServerProtector.getMessage("broadcasts.passed");
		broadcasts_joined = ServerProtector.getMessage("broadcasts.joined");
		broadcasts_captured = ServerProtector.getMessage("broadcasts.captured");
	}
	
	public static void loadTitleMessages() {
		titles_title = ServerProtector.getMessage("titles.title");
		titles_subtitle = ServerProtector.getMessage("titles.subtitle");
	}
	
	public static void loadBossbarMessages() {
		bossbar_message = ServerProtector.getMessage("bossbar.message");
	}

	public static FileConfiguration getFile(String fileName) {
	    File file = new File(instance.getDataFolder(), fileName);
	    if (!file.exists()) {
	    	instance.saveResource(fileName, false);
	    }
	    return YamlConfiguration.loadConfiguration(file);
	}
    
    public static FileConfiguration getFileFullPath(String fileName) {
        File file = new File(instance.getConfig().getString("file-settings.data-file-path"), fileName);
        if (!file.exists()) {
        	instance.saveResource(fileName, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public static void save(FileConfiguration config, String fileName) {
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            try {
                config.save(new File(instance.getDataFolder(), fileName));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
    	});
    }
    
    public static void saveFullPath(FileConfiguration config, String fileName) {
        Bukkit.getScheduler().runTaskAsynchronously(ServerProtector.getInstance(), () -> {
            try {
            	config.save(new File(instance.getConfig().getString("file-settings.data-file-path"), fileName));
            } catch (IOException ex) {
            	ex.printStackTrace();
            }
        });
    }
}
