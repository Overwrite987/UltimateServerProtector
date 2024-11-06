package ru.overwrite.protect.bukkit.configuration.data;

import java.util.List;

public record EffectSettings(
        boolean enableEffects,
        List<String> effects
) {
}
