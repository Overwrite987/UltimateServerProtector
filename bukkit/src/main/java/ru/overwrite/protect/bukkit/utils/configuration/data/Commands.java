package ru.overwrite.protect.bukkit.utils.configuration.data;

import java.util.List;

public record Commands(
        List<String> notInConfig,
        List<String> notInOpWhitelist,
        List<String> haveBlacklistedPerm,
        List<String> notAdminIp,
        List<String> failedPass,
        List<String> failedTime,
        List<String> failedRejoin
) {
}
