package ru.overwrite.protect.bukkit.api;

import org.jetbrains.annotations.NotNull;

public record CaptureReason(String permission) {

    @NotNull
    public Reason getReason() {
        return permission != null ? Reason.PERMISSION : Reason.OPERATOR;
    }

    public enum Reason {
        OPERATOR,
        PERMISSION
    }
}
