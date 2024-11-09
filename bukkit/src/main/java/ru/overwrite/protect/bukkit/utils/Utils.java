package ru.overwrite.protect.bukkit.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ru.overwrite.protect.bukkit.ServerProtectorManager;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Utils {

    public static final int SUB_VERSION = Integer.parseInt(Bukkit.getBukkitVersion().split("-")[0].split("\\.")[1]);
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([a-fA-F\\d]{6})");

    public static final boolean FOLIA;

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

    public static String getIp(Player player) {
        return player.getAddress().getAddress().getHostAddress();
    }

    public static void sendTitleMessage(String[] titleMessages, Player p) {
        if (titleMessages.length == 0) {
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
        p.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    public static void sendSound(String[] soundArgs, Player p) {
        if (soundArgs.length == 0) {
            return;
        }
        if (soundArgs.length > 3) {
            Bukkit.getConsoleSender().sendMessage("Unable to send sound. " + Arrays.toString(soundArgs));
            return;
        }
        Sound sound = Sound.valueOf(soundArgs[0]);
        float volume = soundArgs.length >= 2 ? Float.parseFloat(soundArgs[1]) : 1.0f;
        float pitch = soundArgs.length == 3 ? Float.parseFloat(soundArgs[2]) : 1.0f;
        p.playSound(p.getLocation(), sound, volume, pitch);
    }

    private static final char COLOR_CHAR = '§';

    public static String colorize(String message, String serializer) {
        return switch (serializer) {
            case "LEGACY" -> {
                if (SUB_VERSION >= 16) {
                    Matcher matcher = HEX_PATTERN.matcher(message);
                    StringBuilder builder = new StringBuilder(message.length() + 32);
                    while (matcher.find()) {
                        String group = matcher.group(1);
                        matcher.appendReplacement(builder,
                                COLOR_CHAR + "x" +
                                        COLOR_CHAR + group.charAt(0) +
                                        COLOR_CHAR + group.charAt(1) +
                                        COLOR_CHAR + group.charAt(2) +
                                        COLOR_CHAR + group.charAt(3) +
                                        COLOR_CHAR + group.charAt(4) +
                                        COLOR_CHAR + group.charAt(5));
                    }
                    message = matcher.appendTail(builder).toString();
                }
                yield message.replace('&', COLOR_CHAR); // The fastest approach, even if it looks like crap
            }
            case "MINIMESSAGE" -> {
                Component component = MiniMessage.miniMessage().deserialize(message);
                yield LegacyComponentSerializer.legacySection().serialize(component);
            }
            default -> message;
        };
    }

    public static void checkUpdates(ServerProtectorManager plugin, Consumer<String> consumer) {
        plugin.getRunner().runAsync(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new URL("https://raw.githubusercontent.com/Overwrite987/UltimateServerProtector/master/VERSION")
                            .openStream()))) {
                consumer.accept(reader.readLine().trim());
            } catch (IOException ex) {
                plugin.getLogger().warning("Unable to check for updates: " + ex.getMessage());
            }
        });
    }

    private static final Set<String> SUPPORTED_HASH_TYPES =
            Set.of("SHA224", "SHA256", "SHA384", "SHA512", "SHA-224", "SHA-256", "SHA-384", "SHA-512", "SHA3-224", "SHA3-256", "SHA3-384", "SHA3-512");

    public static String encryptPassword(String password, String salt, List<String> hashTypes) {
        if (hashTypes.isEmpty()) {
            return password;
        }
        String encryptedPassword = password;
        boolean salted = false;
        for (String hashType : hashTypes) {
            switch (hashType.toUpperCase()) {
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
                    if (SUPPORTED_HASH_TYPES.contains(hashType.toUpperCase())) {
                        encryptedPassword = encryptToHash(encryptedPassword, hashType);
                    } else {
                        throw new IllegalArgumentException("Unsupported hash type: " + hashType);
                    }
                }
            }
        }
        return salted ? salt + ":" + encryptedPassword : encryptedPassword;
    }

    private static final SecureRandom random = new SecureRandom();

    public static String generateSalt(int length) {
        byte[] saltBytes = new byte[(int) Math.ceil((double) length * 3 / 4)];
        random.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }

    private static String encodeToBase64(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }

    private static String encryptToHash(String str, String algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hash = digest.digest(str.getBytes(StandardCharsets.UTF_8));
            return bytesToHexString(hash);
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}