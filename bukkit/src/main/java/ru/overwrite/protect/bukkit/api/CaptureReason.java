package ru.overwrite.protect.bukkit.api;

public record CaptureReason(String permission) {

    public Reason getReason() {
        return permission != null ? Reason.PERMISSION : Reason.OPERATOR;
    }

    public enum Reason {
        OPERATOR,
        PERMISSION
    }
}