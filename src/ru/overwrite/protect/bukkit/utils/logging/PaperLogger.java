package ru.overwrite.protect.bukkit.utils.logging;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import ru.overwrite.protect.bukkit.Logger;
import ru.overwrite.protect.bukkit.ServerProtectorManager;

public class PaperLogger implements Logger {
	
	private final ServerProtectorManager instance;
	
	private final LegacyComponentSerializer legacySection = LegacyComponentSerializer.legacySection();
	
	public PaperLogger(ServerProtectorManager plugin) {
		instance = plugin;
	}
	
	public void info(String msg) {
		instance.getComponentLogger().info(legacySection.deserialize(msg));
	}
	
	public void warn(String msg) {
		instance.getComponentLogger().warn(legacySection.deserialize(msg));
	}

}