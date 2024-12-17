package ru.overwrite.protect.bukkit.configuration.data;

import java.util.List;

public record ApiSettings(
        boolean allowCancelCaptureEvent,
        boolean callEventOnPasswordEnter,
        List<String> allowedAuthApiCallsPackages
) {
}
