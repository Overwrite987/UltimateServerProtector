package ru.overwrite.protect.bukkit.utils.configuration.data;

import java.util.Set;

public record GeyserSettings(
        String prefix,
        Set<String> nicknames
) {
}
