package ru.overwrite.protect.bukkit.logging.impl;

import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.logging.Logger;

public class BukkitLogger implements Logger {

    private final ServerProtectorManager plugin;

    public BukkitLogger(ServerProtectorManager plugin) {
        this.plugin = plugin;
    }

    @Override
    public void info(String msg) {
        plugin.getLogger().info(msg);
    }

    @Override
    public void warn(String msg) {
        plugin.getLogger().warning(msg);
    }

}
