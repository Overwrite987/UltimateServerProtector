package ru.overwrite.protect.bukkit.utils.color.impl;

import ru.overwrite.protect.bukkit.utils.Utils;
import ru.overwrite.protect.bukkit.utils.color.Colorizer;

public class VanillaColorizer implements Colorizer {

    @Override
    public String colorize(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        return Utils.translateAlternateColorCodes('&', message);
    }
}
