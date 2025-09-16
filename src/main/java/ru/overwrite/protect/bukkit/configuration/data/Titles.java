package ru.overwrite.protect.bukkit.configuration.data;

public record Titles(
        String[] message,
        String[] incorrect,
        String[] correct
) {
}
