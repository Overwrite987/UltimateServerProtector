package ru.overwrite.protect.bukkit.utils.configuration.data;

public record MessageSettings(
        boolean sendTitle,
        boolean enableBroadcasts,
        boolean enableConsoleBroadcasts
) {
}
