package ru.overwrite.protect.bukkit.configuration.data;

public record SessionSettings(
        boolean session,
        boolean sessionTimeEnabled,
        int sessionTime
) {
}
