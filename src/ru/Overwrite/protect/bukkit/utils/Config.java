package ru.overwrite.protect.bukkit.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.overwrite.protect.bukkit.ServerProtector;

import java.io.File;
import java.io.IOException;

public class Config {

    public static FileConfiguration getFile(String fileName) {
        File file = new File(ServerProtector.getInstance().getDataFolder(), fileName);
        if (ServerProtector.getInstance().getResource(fileName) == null) {
            return save(YamlConfiguration.loadConfiguration(file), fileName);
        }
        if (!file.exists()) {
        	ServerProtector.getInstance().saveResource(fileName, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }
    
    public static FileConfiguration getFileFullPath(String fileName) {
        File file = new File(ServerProtector.getInstance().getConfig().getString("file-settings.data-file-path"), fileName);
        if (ServerProtector.getInstance().getResource(fileName) == null) {
            return save(YamlConfiguration.loadConfiguration(file), fileName);
        }
        if (!file.exists()) {
        	ServerProtector.getInstance().saveResource(fileName, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public static FileConfiguration save(FileConfiguration config, String fileName) {
        try {
            config.save(new File(ServerProtector.getInstance().getDataFolder(), fileName));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return config;
    }
    
    public static FileConfiguration saveFullPath(FileConfiguration config, String fileName) {
        try {
            config.save(new File(ServerProtector.getInstance().getConfig().getString("file-settings.data-file-path"), fileName));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return config;
    }
}
