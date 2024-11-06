package ru.overwrite.protect.bukkit.configuration.data;

public record BossbarSettings(
        boolean enableBossbar,
        String barColor,
        String barStyle,
        String bossbarMessage
) {
}
