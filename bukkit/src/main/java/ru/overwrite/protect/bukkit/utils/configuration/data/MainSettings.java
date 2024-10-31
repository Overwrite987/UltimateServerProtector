package ru.overwrite.protect.bukkit.utils.configuration.data;

public record MainSettings(
        String prefix,
        String pasCommand,
        boolean useCommand,
        boolean enableAdminCommands,
        long checkInterval,
        boolean papiSupport
) {
}
