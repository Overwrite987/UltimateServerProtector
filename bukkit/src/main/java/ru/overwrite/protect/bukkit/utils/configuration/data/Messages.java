package ru.overwrite.protect.bukkit.utils.configuration.data;

public record Messages(
        String message,
        String incorrect,
        String correct,
        String noNeed,
        String cantBeNull,
        String playerOnly
) {
}
