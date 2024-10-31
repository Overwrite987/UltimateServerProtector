package ru.overwrite.protect.bukkit.utils.configuration.data;

public record BossbarSettings(
        boolean enableBossbar,
        String barColor,
        String barStyle,
        String bossbarMessage
) {
}
