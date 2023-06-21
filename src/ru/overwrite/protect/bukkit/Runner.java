package ru.overwrite.protect.bukkit;

import org.bukkit.configuration.file.FileConfiguration;

public interface Runner {
    public void mainCheck();
    public void startMSG(FileConfiguration config);
    public void startTimer(FileConfiguration config);
    public void adminCheck(FileConfiguration config);
    public void startOpCheck(FileConfiguration config);
    public void startPermsCheck(FileConfiguration config);
}
