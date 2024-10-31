package ru.overwrite.protect.bukkit.utils.configuration.data;

public record LoggingSettings(
        boolean loggingPas,
        boolean loggingJoin,
        boolean loggingEnableDisable,
        boolean loggingCommandExecution
) {
}