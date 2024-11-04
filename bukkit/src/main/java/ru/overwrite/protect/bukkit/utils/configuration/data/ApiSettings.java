package ru.overwrite.protect.bukkit.utils.configuration.data;

import java.util.List;

public record ApiSettings(
        boolean callEventOnCapture,
        boolean callEventOnPasswordEnter,
        List<String> allowedAuthApiCallsPackages
) {
}
