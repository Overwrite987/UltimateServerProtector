package ru.overwrite.protect.bukkit.configuration.data;

public record LogMessages(
        String enabled,
        String disabled,
        String failed,
        String passed,
        String joined,
        String captured,
        String command
) {
}
