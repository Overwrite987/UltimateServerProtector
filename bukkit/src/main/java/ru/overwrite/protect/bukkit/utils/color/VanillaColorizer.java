package ru.overwrite.protect.bukkit.utils.color;

import ru.overwrite.protect.bukkit.utils.Utils;

public class VanillaColorizer implements Colorizer {

    @Override
    public String colorize(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        return Utils.translateAlternateColorCodes('&', message);
    }
}
