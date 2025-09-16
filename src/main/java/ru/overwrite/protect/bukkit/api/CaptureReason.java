package ru.overwrite.protect.bukkit.api;

import org.jetbrains.annotations.NotNull;

public record CaptureReason(String permission) {

    public static final CaptureReason OPERATOR_REASON = new CaptureReason(null);
    public static final CaptureReason DEFAULT_PERMISSION_REASON = new CaptureReason("serverprotector.protect");

    @NotNull
    public Reason getReason() {
        return permission != null ? Reason.PERMISSION : Reason.OPERATOR;
    }

    public enum Reason {
        OPERATOR, PERMISSION
    }
}
