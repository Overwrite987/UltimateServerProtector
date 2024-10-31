package ru.overwrite.protect.bukkit.utils.configuration.data;

public record SessionSettings(
        boolean session,
        boolean sessionTimeEnabled,
        int sessionTime
) {
}
