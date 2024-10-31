package ru.overwrite.protect.bukkit.utils.configuration.data;

public record Broadcasts(
        String failed,
        String passed,
        String joined,
        String captured
) {
}
