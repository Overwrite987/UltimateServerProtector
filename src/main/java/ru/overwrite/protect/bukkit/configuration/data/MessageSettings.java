package ru.overwrite.protect.bukkit.configuration.data;

public record MessageSettings(
        boolean sendTitle,
        boolean enableBroadcasts,
        boolean enableConsoleBroadcasts
) {
}
