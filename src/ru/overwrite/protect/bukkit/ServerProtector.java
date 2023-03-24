package ru.overwrite.protect.bukkit;

import org.bukkit.entity.Player;
import ru.overwrite.protect.bukkit.utils.Config;
import ru.overwrite.protect.bukkit.utils.Metrics;

import java.util.Date;

public final class ServerProtector extends ServerProtectorManager {
    public final PasswordHandler passwordHandler = new PasswordHandler(this);
    
    private static ServerProtector instance;

    public static ServerProtector getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        checkPaper();
        long startTime = System.currentTimeMillis();
        Date date = new Date(startTime);
        instance = this;
        saveConfigs();
        registerListeners();
        registerCommands();
        startRunners();
        logEnableDisable(message.getString("log-format.enabled"), date);
        if (getConfig().getBoolean("main-settings.enable-metrics")) {
            new Metrics(this, 13347);
        }
        checkForUpdates();
        long endTime = System.currentTimeMillis();
        logger.info("Plugin started in " + (endTime - startTime) + " ms");
    }

    @Override
    public void onDisable() {
        Date date = new Date();
        getServer().getScheduler().cancelTasks(this);
        instance = null;
        login.clear();
        ips.clear();
        time.clear();
        passwordHandler.clearAttempts();
        logEnableDisable(message.getString("log-format.disabled"), date);
        if (Config.message_settings_enable_broadcasts) {
        	for (Player p : getServer().getOnlinePlayers()) {
        		if (p.hasPermission("serverprotector.admin")) {
            		p.sendMessage(getMessage("broadcasts.disabled"));
            	}
        	}
        }
        if (getConfig().getBoolean("secure-settings.shutdown-on-disable")) {
        	getServer().shutdown();
        }
    }
}
  
