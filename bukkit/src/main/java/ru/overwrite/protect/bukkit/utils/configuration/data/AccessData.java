package ru.overwrite.protect.bukkit.utils.configuration.data;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record AccessData(
        Set<String> perms,
        List<String> allowedCommands,
        List<String> opWhitelist,
        Set<String> blacklistedPerms,
        Map<String, List<String>> ipWhitelist
) {
}
