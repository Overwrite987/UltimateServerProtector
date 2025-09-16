package ru.overwrite.protect.bukkit.configuration.data;

public record SecureSettings(
        boolean enableOpWhitelist,
        boolean enableNotAdminPunish,
        boolean enablePermissionBlacklist,
        boolean enableIpWhitelist,
        boolean onlyConsoleUsp,
        boolean enableExcludedPlayers
) {
}
