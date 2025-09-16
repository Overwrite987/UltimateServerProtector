package ru.overwrite.protect.bukkit.configuration.data;

public record Messages(
        String message,
        String incorrect,
        String correct,
        String noNeed,
        String cantBeNull,
        String playerOnly
) {
}
