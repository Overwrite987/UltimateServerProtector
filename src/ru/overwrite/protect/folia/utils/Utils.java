package ru.overwrite.protect.folia.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.md_5.bungee.api.ChatColor.COLOR_CHAR;

public final class Utils {
	private static final int SUB_VERSION = Integer.parseInt(
            Bukkit.getServer().getClass().getPackage().getName()
                    .replace(".", ",")
                    .split(",")[3]
                    .replace("1_", "")
                    .replaceAll("_R\\d", "")
                    .replace("v", "")
    );
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([a-fA-F\\d]{6})");

    private Utils() {}

    public static String getIp(Player player) {
        return player.getAddress().getAddress().getHostAddress();
    }

    public static String colorize(String message) {
        if (SUB_VERSION >= 16) {
            Matcher matcher = HEX_PATTERN.matcher(message);
            StringBuilder builder = new StringBuilder(message.length() + 4 * 8);
            while (matcher.find()) {
                String group = matcher.group(1);
                matcher.appendReplacement(builder, COLOR_CHAR + "x"
                        + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                        + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                        + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5)
                );
            }
            message = matcher.appendTail(builder).toString();
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static void checkUpdates(Plugin plugin, Consumer<String> consumer) {
    	Bukkit.getAsyncScheduler().runNow(plugin, (cu) -> {
	        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("https://raw.githubusercontent.com/Overwrite987/UltimateServerProtector/master/VERSION").openStream()))) {
	            String version = reader.readLine();
	            if (version != null) {
	                consumer.accept(version.trim());
	            }
	        } catch (IOException exception) {
	            plugin.getLogger().info("Can't check for updates: " + exception.getMessage());
	        }
	    });
	}
}
