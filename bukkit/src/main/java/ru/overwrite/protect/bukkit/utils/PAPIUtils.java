package ru.overwrite.protect.bukkit.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class PAPIUtils {

    public static String parsePlaceholders(Player p, String message) {
        return Utils.COLORIZER.colorize(PlaceholderAPI.setPlaceholders(p, message));
    }
}
