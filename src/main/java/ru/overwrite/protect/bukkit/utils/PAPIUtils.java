package ru.overwrite.protect.bukkit.utils;

import lombok.experimental.UtilityClass;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

@UtilityClass
public class PAPIUtils {

    public String parsePlaceholders(Player player, String message) {
        return Utils.COLORIZER.colorize(PlaceholderAPI.setPlaceholders(player, message));
    }
}
