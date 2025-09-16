package ru.overwrite.protect.bukkit.configuration.data;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

public record BossbarSettings(
        boolean enableBossbar,
        BarColor barColor,
        BarStyle barStyle,
        String bossbarMessage
) {
}
