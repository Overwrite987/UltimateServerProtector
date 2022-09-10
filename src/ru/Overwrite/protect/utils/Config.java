package ru.Overwrite.protect.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.Overwrite.protect.Main;

import java.io.File;
import java.io.IOException;

public class Config {

    public static FileConfiguration getFile(String s) {
        File file = new File(Main.getInstance().getDataFolder(), s);
        if (Main.getInstance().getResource(s) == null) {
            return save((FileConfiguration)YamlConfiguration.loadConfiguration(file), s);
        }
        if (!file.exists()) {
            Main.getInstance().saveResource(s, false);
        }
        return (FileConfiguration)YamlConfiguration.loadConfiguration(file);
    }

    public static FileConfiguration save(FileConfiguration fileConfiguration, String s) {
        try {
            fileConfiguration.save(new File(Main.getInstance().getDataFolder(), s));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return fileConfiguration;
    }
}
