package ru.overwrite.protect.bukkit.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.utils.color.Colorizer;
import ru.overwrite.protect.bukkit.utils.color.impl.LegacyAdvancedColorizer;
import ru.overwrite.protect.bukkit.utils.color.impl.LegacyColorizer;
import ru.overwrite.protect.bukkit.utils.color.impl.MiniMessageColorizer;
import ru.overwrite.protect.bukkit.utils.color.impl.VanillaColorizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@UtilityClass
public class Utils {

    public final int SUB_VERSION = Integer.parseInt(Bukkit.getBukkitVersion().split("-")[0].split("\\.")[1]);

    public final boolean FOLIA;

    static {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");
            folia = true;
        } catch (ClassNotFoundException e) {
            folia = false;
        }
        FOLIA = folia;
    }

    public Colorizer COLORIZER;

    public void setupColorizer(ConfigurationSection mainSettings) {
        COLORIZER = switch (mainSettings.getString("serializer", "LEGACY").toUpperCase()) {
            case "MINIMESSAGE" -> new MiniMessageColorizer();
            case "LEGACY" -> SUB_VERSION >= 16 ? new LegacyColorizer() : new VanillaColorizer();
            case "LEGACY_ADVANCED" -> new LegacyAdvancedColorizer();
            default -> new VanillaColorizer();
        };
    }

    public String getIp(Player player) {
        return player.getAddress().getAddress().getHostAddress();
    }

    public void sendTitleMessage(String[] titleMessages, Player player) {
        if (titleMessages[0].isEmpty()) {
            return;
        }
        if (titleMessages.length > 5) {
            Bukkit.getConsoleSender().sendMessage("Unable to send title. " + Arrays.toString(titleMessages));
            return;
        }
        String title = titleMessages[0];
        String subtitle = titleMessages.length >= 2 ? titleMessages[1] : "";
        int fadeIn = titleMessages.length >= 3 ? Integer.parseInt(titleMessages[2]) : 10;
        int stay = titleMessages.length >= 4 ? Integer.parseInt(titleMessages[3]) : 70;
        int fadeOut = titleMessages.length == 5 ? Integer.parseInt(titleMessages[4]) : 20;
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    public void sendSound(String[] soundArgs, Player player) {
        if (soundArgs[0].isEmpty()) {
            return;
        }
        if (soundArgs.length > 3) {
            Bukkit.getConsoleSender().sendMessage("Unable to send sound. " + Arrays.toString(soundArgs));
            return;
        }
        Sound sound = Sound.valueOf(soundArgs[0]);
        float volume = soundArgs.length >= 2 ? Float.parseFloat(soundArgs[1]) : 1.0F;
        float pitch = soundArgs.length == 3 ? Float.parseFloat(soundArgs[2]) : 1.0F;
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    public final char COLOR_CHAR = '§';

    public String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
        final char[] b = textToTranslate.toCharArray();

        for (int i = 0, length = b.length - 1; i < length; i++) {
            if (b[i] == altColorChar && isValidColorCharacter(b[i + 1])) {
                b[i++] = COLOR_CHAR;
                b[i] |= 0x20;
            }
        }

        return new String(b);
    }

    private boolean isValidColorCharacter(char c) {
        return switch (c) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'A', 'B', 'C', 'D',
                 'E', 'F', 'r', 'R', 'k', 'K', 'l', 'L', 'm', 'M', 'n', 'N', 'o', 'O', 'x', 'X' -> true;
            default -> false;
        };
    }

    public void checkUpdates(ServerProtectorManager plugin, Consumer<String> consumer) {
        plugin.getRunner().runDelayedAsync(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new URL("https://raw.githubusercontent.com/Overwrite987/UltimateServerProtector/master/VERSION")
                            .openStream()))) {
                consumer.accept(reader.readLine().trim());
            } catch (IOException ex) {
                plugin.getPluginLogger().warn("Unable to check for updates: " + ex.getMessage());
            }
        }, 10);
    }

    private final Set<String> SUPPORTED_HASH_TYPES =
            Set.of("SHA224", "SHA256", "SHA384", "SHA512", "SHA-224", "SHA-256", "SHA-384", "SHA-512", "SHA3-224", "SHA3-256", "SHA3-384", "SHA3-512");

    public String encryptPassword(String password, String salt, List<String> hashTypes) {
        if (hashTypes.isEmpty()) {
            return password;
        }
        String encryptedPassword = password;
        boolean salted = false;
        for (String hashType : hashTypes) {
            switch (hashType) {
                case "BASE64": {
                    encryptedPassword = encodeToBase64(encryptedPassword);
                    break;
                }
                case "SALT": {
                    if (salted) {
                        break;
                    }
                    encryptedPassword = salt + encryptedPassword;
                    salted = true;
                    break;
                }
                default: {
                    if (SUPPORTED_HASH_TYPES.contains(hashType)) {
                        encryptedPassword = encryptToHash(encryptedPassword, hashType);
                    } else {
                        throw new IllegalArgumentException("Unsupported hash type: " + hashType);
                    }
                }
            }
        }
        return salted ? salt + ":" + encryptedPassword : encryptedPassword;
    }

    private final SecureRandom random = new SecureRandom();

    public String generateSalt(int length) {
        byte[] saltBytes = new byte[(int) Math.ceil((double) length * 3 / 4)];
        random.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }

    private final char[] CHARS = {
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            '1', '2', '3', '4', '5', '6', '7', '8', '9', '0'
    };

    public String generatePassword(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS[random.nextInt(CHARS.length)]);
        }
        return sb.toString();
    }

    private String encodeToBase64(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }

    private String encryptToHash(String str, String algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hash = digest.digest(str.getBytes(StandardCharsets.UTF_8));
            return bytesToHexString(hash);
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
            return str;
        }
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
