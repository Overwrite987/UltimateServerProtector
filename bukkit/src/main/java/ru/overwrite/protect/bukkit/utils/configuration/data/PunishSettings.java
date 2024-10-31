package ru.overwrite.protect.bukkit.utils.configuration.data;

public record PunishSettings(
        boolean enableAttempts,
        int maxAttempts,
        boolean enableTime,
        int time,
        boolean enableRejoin,
        int maxRejoins
) {
}
