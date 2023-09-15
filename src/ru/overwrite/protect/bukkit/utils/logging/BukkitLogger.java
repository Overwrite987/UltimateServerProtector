package ru.overwrite.protect.bukkit.utils.logging;

import ru.overwrite.protect.bukkit.Logger;
import ru.overwrite.protect.bukkit.ServerProtectorManager;

public class BukkitLogger implements Logger {
	
	private final ServerProtectorManager instance;
	
	public BukkitLogger(ServerProtectorManager plugin) {
		instance = plugin;
	}
	
	public void info(String msg) {
		instance.getLogger().info(msg);
	}

}
