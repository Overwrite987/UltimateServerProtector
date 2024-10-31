package ru.overwrite.protect.bukkit.utils.configuration.data;

public record Titles(
        String[] message,
        String[] incorrect,
        String[] correct
) {
}
