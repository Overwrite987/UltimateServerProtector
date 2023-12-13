package ru.overwrite.protect.bukkit.utils;

import java.io.IOException;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import ru.overwrite.protect.bukkit.ServerProtectorManager;

import static net.md_5.bungee.api.ChatColor.COLOR_CHAR;

public final class Utils {

	public static BossBar bossbar;

	public static final int SUB_VERSION = Integer
			.parseInt(Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3]
					.replace("1_", "").replaceAll("_R\\d", "").replace("v", ""));
	private static final Pattern HEX_PATTERN = Pattern.compile("&#([a-fA-F\\d]{6})");

	public static final boolean FOLIA;

	static {
		boolean folia;
		try {
			Class.forName("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");
			folia = true;
		} catch (ClassNotFoundException e) {
			folia = false;
		}
		FOLIA = folia;
	}

	public static String getIp(Player player) {
		return player.getAddress().getAddress().getHostAddress();
	}
	
	public static void sendTitleMessage(String[] titleMessages, Player p) {
		String title = titleMessages[0];
		String subtitle = (titleMessages.length > 1 && titleMessages[1] != null) ? titleMessages[1] : "";
		int fadeIn = (titleMessages.length > 2 && titleMessages[2] != null) ? Integer.parseInt(titleMessages[2]) : 10;
		int stay = (titleMessages.length > 3 && titleMessages[3] != null) ? Integer.parseInt(titleMessages[3]) : 70;
		int fadeOut = (titleMessages.length > 4 && titleMessages[4] != null) ? Integer.parseInt(titleMessages[4]) : 20;
		p.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
	}

	public static String colorize(String message) {
		switch (ServerProtectorManager.serialiser) {
			case "LEGACY": {
				if (SUB_VERSION >= 16) {
					Matcher matcher = HEX_PATTERN.matcher(message);
					StringBuilder builder = new StringBuilder(message.length() + 4 * 8);
					while (matcher.find()) {
						String group = matcher.group(1);
						matcher.appendReplacement(builder,
								COLOR_CHAR + "x" + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1) + COLOR_CHAR
										+ group.charAt(2) + COLOR_CHAR + group.charAt(3) + COLOR_CHAR + group.charAt(4)
										+ COLOR_CHAR + group.charAt(5));
					}
					message = matcher.appendTail(builder).toString();
				}
				return ChatColor.translateAlternateColorCodes('&', message);
			}
			case "MINIMESSAGE": {
				Component component = MiniMessage.miniMessage().deserialize(message);
				return LegacyComponentSerializer.legacySection().serialize(component);
			}
			default: {
				return message;
			}
		}
	}

	public static void checkUpdates(Plugin plugin, Consumer<String> consumer) {
		Runnable run = () -> {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(
					new URL("https://raw.githubusercontent.com/Overwrite987/UltimateServerProtector/master/VERSION")
							.openStream()))) {
				consumer.accept(reader.readLine().trim());
			} catch (IOException exception) {
				plugin.getLogger().warning("Can't check for updates: " + exception.getMessage());
			}
		};
		if (Utils.FOLIA) {
			Bukkit.getAsyncScheduler().runNow(plugin, (cu) -> run.run());
		} else {
			Bukkit.getScheduler().runTaskAsynchronously(plugin, run);
		}
	}
}
