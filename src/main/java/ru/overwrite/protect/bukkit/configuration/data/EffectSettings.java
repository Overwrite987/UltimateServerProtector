package ru.overwrite.protect.bukkit.configuration.data;

import org.bukkit.potion.PotionEffect;

import java.util.List;

public record EffectSettings(
        boolean enableEffects,
        List<PotionEffect> effects
) {
}
