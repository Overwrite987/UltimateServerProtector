package ru.overwrite.protect.bukkit.configuration.data;

import java.util.Set;

public record GeyserSettings(
        String prefix,
        Set<String> nicknames
) {
}
