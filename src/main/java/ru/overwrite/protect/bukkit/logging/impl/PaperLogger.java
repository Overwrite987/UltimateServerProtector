package ru.overwrite.protect.bukkit.logging.impl;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.logging.Logger;

public class PaperLogger implements Logger {

    private final ServerProtectorManager plugin;

    private final LegacyComponentSerializer legacySection = LegacyComponentSerializer.legacySection();

    public PaperLogger(ServerProtectorManager plugin) {
        this.plugin = plugin;
    }

    @Override
    public void info(String msg) {
        plugin.getComponentLogger().info(legacySection.deserialize(msg));
    }

    @Override
    public void warn(String msg) {
        plugin.getComponentLogger().warn(legacySection.deserialize(msg));
    }

}
