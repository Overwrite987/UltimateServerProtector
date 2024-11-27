package ru.overwrite.protect.bukkit.utils.color;

import ru.overwrite.protect.bukkit.utils.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LegacyColorizer implements Colorizer {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([a-fA-F\\d]{6})");

    @Override
    public String colorize(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        final Matcher matcher = HEX_PATTERN.matcher(message);
        final StringBuilder builder = new StringBuilder(message.length() + 32);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(builder,
                    Utils.COLOR_CHAR + "x" +
                            Utils.COLOR_CHAR + group.charAt(0) +
                            Utils.COLOR_CHAR + group.charAt(1) +
                            Utils.COLOR_CHAR + group.charAt(2) +
                            Utils.COLOR_CHAR + group.charAt(3) +
                            Utils.COLOR_CHAR + group.charAt(4) +
                            Utils.COLOR_CHAR + group.charAt(5));
        }
        message = matcher.appendTail(builder).toString();
        return Utils.translateAlternateColorCodes('&', message);
    }
}