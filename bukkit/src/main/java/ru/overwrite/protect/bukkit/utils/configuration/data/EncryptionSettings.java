package ru.overwrite.protect.bukkit.utils.configuration.data;

import java.util.List;

public record EncryptionSettings(
        boolean enableEncryption,
        String encryptMethod,
        List<String> encryptMethods,
        int saltLength,
        boolean autoEncryptPasswords,
        List<List<String>> oldEncryptMethods
) {
}