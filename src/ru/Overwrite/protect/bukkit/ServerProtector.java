package ru.overwrite.protect.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
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
        reloadConfigs();
        registerListeners();
        registerCommands(this);
        startRunners();
        logEnableDisable(message.getString("log-format.enabled"), date);
        if (getConfig().getBoolean("main-settings.enable-metrics")) {
            new Metrics(this, 13347);
        }
        checkForUpdates();
        long endTime = System.currentTimeMillis();
        getLogger().info("Plugin started in " + (endTime - startTime) + " ms");
    }

    @Override
    public void onDisable() {
        Date date = new Date();
        FileConfiguration message = Config.getFile("message.yml");
        Bukkit.getScheduler().cancelTasks(this);
        instance = null;
        login.clear();
        ips.clear();
        time.clear();
        passwordHandler.clearAttempts();
        logEnableDisable(message.getString("log-format.disabled"), date);
        if (getConfig().getBoolean("message-settings.enable-broadcasts")) {
            Bukkit.broadcast(getMessage("broadcasts.disabled"), "serverprotector.admin");
        }
        if (getConfig().getBoolean("secure-settings.shutdown-on-disable")) {
            Bukkit.shutdown();
        }
    }
}
  
