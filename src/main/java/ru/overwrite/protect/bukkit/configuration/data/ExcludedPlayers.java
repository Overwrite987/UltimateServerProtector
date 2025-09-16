package ru.overwrite.protect.bukkit.configuration.data;

import java.util.List;

public record ExcludedPlayers(
        List<String> adminPass,
        List<String> opWhitelist,
        List<String> ipWhitelist,
        List<String> blacklistedPerms,
        List<String> alert
) {
}
