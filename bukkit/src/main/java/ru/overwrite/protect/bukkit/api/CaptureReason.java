package ru.overwrite.protect.bukkit.api;

public class CaptureReason {

    private final Reason reason;
    private final String permission;

    public CaptureReason(Reason reason, String permission) {
        this.reason = reason;
        this.permission = permission;
    }

    public Reason getReason() {
        return reason;
    }

    public String getPermission() {
        return permission;
    }

    public enum Reason {
        OPERATOR,
        PERMISSION
    }
}
