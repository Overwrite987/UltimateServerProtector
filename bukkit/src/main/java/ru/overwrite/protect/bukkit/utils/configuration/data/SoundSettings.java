package ru.overwrite.protect.bukkit.utils.configuration.data;

public record SoundSettings(
        boolean enableSounds,
        String[] onCapture,
        String[] onPasFail,
        String[] onPasCorrect
) {
}
