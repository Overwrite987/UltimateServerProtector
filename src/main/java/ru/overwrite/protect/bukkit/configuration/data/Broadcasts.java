package ru.overwrite.protect.bukkit.configuration.data;

public record Broadcasts(
        String failed,
        String passed,
        String joined,
        String captured,
        String disabled
) {
}
