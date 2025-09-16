package ru.overwrite.protect.bukkit.configuration.data;

public record BlockingSettings(
        boolean blockItemDrop,
        boolean blockItemPickup,
        boolean blockTabComplete,
        boolean blockDamage,
        boolean blockDamagingEntity,
        boolean blockInventoryOpen,
        boolean hideOnEntering,
        boolean hideOtherOnEntering,
        boolean allowOrientationChange
) {
}
