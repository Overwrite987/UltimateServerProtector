package ru.overwrite.protect.bukkit.utils.logging;

import ru.overwrite.protect.bukkit.ServerProtectorManager;

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
