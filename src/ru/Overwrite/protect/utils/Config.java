package ru.Overwrite.protect.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.Overwrite.protect.Main;

import java.io.File;
import java.io.IOException;

public class Config {

    public static FileConfiguration getFile(String fileName) {
        File file = new File(Main.getInstance().getDataFolder(), fileName);
        if (Main.getInstance().getResource(fileName) == null) {
            return save(YamlConfiguration.loadConfiguration(file), fileName);
        }
        if (!file.exists()) {
            Main.getInstance().saveResource(fileName, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public static FileConfiguration save(FileConfiguration config, String fileName) {
        try {
            config.save(new File(Main.getInstance().getDataFolder(), fileName));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return config;
    }
}
