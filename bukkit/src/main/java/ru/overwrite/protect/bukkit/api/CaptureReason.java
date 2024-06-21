package ru.overwrite.protect.bukkit.api;

public class CaptureReason {

    private final String permission;

    public CaptureReason(String permission) {
        this.permission = permission;
    }

    public Reason getReason() {
        return permission != null ? Reason.PERMISSION : Reason.OPERATOR;
    }

    public String getPermission() {
        return permission;
    }

    public enum Reason {
        OPERATOR,
        PERMISSION
    }
}
