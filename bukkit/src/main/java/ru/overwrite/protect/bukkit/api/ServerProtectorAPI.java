package ru.overwrite.protect.bukkit.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.configuration.Config;
import ru.overwrite.protect.bukkit.utils.Utils;
import ru.overwrite.protect.bukkit.utils.logging.Logger;

import java.util.*;

public final class ServerProtectorAPI {

    private final ServerProtectorManager plugin;

    private final Config pluginConfig;
    private final Logger pluginLogger;

    private final Set<String> captured = new HashSet<>();
    private final Map<String, String> sessions = new HashMap<>();
    private final Set<String> saved = new HashSet<>();

    public ServerProtectorAPI(@NotNull ServerProtectorManager plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
        this.pluginLogger = plugin.getPluginLogger();
    }

    public boolean isAnybodyCaptured() {
        return !this.captured.isEmpty();
    }

    public boolean isCaptured(@NotNull Player player) {
        if (!isAnybodyCaptured()) {
            return false;
        }
        return this.captured.contains(player.getName());
    }

    public boolean isCaptured(@NotNull String playerName) {
        if (!isAnybodyCaptured()) {
            return false;
        }
        return this.captured.contains(playerName);
    }

    public void capturePlayer(@NotNull Player player) {
        if (isCaptured(player)) {
            pluginLogger.warn("Unable to capture " + player.getName() + " Reason: Already captured");
            return;
        }
        this.captured.add(player.getName());
    }

    public void capturePlayer(@NotNull String playerName) {
        if (isCaptured(playerName)) {
            pluginLogger.warn("Unable to capture " + playerName + " Reason: Already captured");
            return;
        }
        this.captured.add(playerName);
    }

    public void uncapturePlayer(@NotNull Player player) {
        if (!plugin.isCalledFromAllowedApplication()) {
            pluginLogger.warn("Unable to uncapture " + player.getName() + " Reason: Action not allowed");
            return;
        }
        if (!this.captured.remove(player.getName())) {
            pluginLogger.warn("Unable to uncapture " + player.getName() + " Reason: Not captured");
        }
    }

    public void uncapturePlayer(@NotNull String playerName) {
        if (!plugin.isCalledFromAllowedApplication()) {
            pluginLogger.warn("Unable to uncapture " + playerName + " Reason: Action not allowed");
            return;
        }
        if (!this.captured.remove(playerName)) {
            pluginLogger.warn("Unable to uncapture " + playerName + " Reason: Not captured");
        }
    }

    public boolean isAuthorised(@NotNull Player player) {
        return pluginConfig.getSessionSettings().session() ?
                hasSession(player) :
                saved.contains(player.getName());
    }

    public boolean isAuthorised(@NotNull Player player, @NotNull String ip) {
        return pluginConfig.getSessionSettings().session() ?
                hasSession(player, ip) :
                saved.contains(player.getName());
    }

    public boolean isAuthorised(@NotNull String playerName, @NotNull String ip) {
        return pluginConfig.getSessionSettings().session() ?
                hasSession(playerName, ip) :
                saved.contains(playerName);
    }

    public boolean hasSession(@NotNull Player player) {
        if (sessions.isEmpty()) {
            return false;
        }
        String sessionIp = sessions.get(player.getName());
        return sessionIp != null && sessionIp.equals(Utils.getIp(player));
    }

    public boolean hasSession(@NotNull Player player, @NotNull String ip) {
        if (sessions.isEmpty()) {
            return false;
        }
        String sessionIp = sessions.get(player.getName());
        return sessionIp != null && sessionIp.equals(ip);
    }

    public boolean hasSession(@NotNull String playerName, @NotNull String ip) {
        if (sessions.isEmpty()) {
            return false;
        }
        String sessionIp = sessions.get(playerName);
        return sessionIp != null && sessionIp.equals(ip);
    }

    public void authorisePlayer(@NotNull Player player) {
        if (!plugin.isCalledFromAllowedApplication()) {
            pluginLogger.warn("Unable to authorise " + player.getName() + " Reason: Action not allowed");
            return;
        }
        if (isAuthorised(player)) {
            pluginLogger.warn("Unable to authorise " + player.getName() + " Reason: Already authorised");
            return;
        }
        if (pluginConfig.getSessionSettings().session()) {
            sessions.put(player.getName(), Utils.getIp(player));
            return;
        }
        saved.add(player.getName());
    }

    public void authorisePlayer(@NotNull Player player, @NotNull String ip) {
        if (!plugin.isCalledFromAllowedApplication()) {
            pluginLogger.warn("Unable to authorise " + player.getName() + " Reason: Action not allowed");
            return;
        }
        if (isAuthorised(player)) {
            pluginLogger.warn("Unable to authorise " + player.getName() + " Reason: Already authorised");
            return;
        }
        if (pluginConfig.getSessionSettings().session()) {
            sessions.put(player.getName(), ip);
            return;
        }
        saved.add(player.getName());
    }

    public void authorisePlayer(@NotNull String playerName, @NotNull String ip) {
        if (!plugin.isCalledFromAllowedApplication()) {
            pluginLogger.warn("Unable to authorise " + playerName + " Reason: Action not allowed");
            return;
        }
        if (isAuthorised(playerName, ip)) {
            pluginLogger.warn("Unable to authorise " + playerName + " Reason: Already authorised");
            return;
        }
        if (pluginConfig.getSessionSettings().session()) {
            sessions.put(playerName, ip);
            return;
        }
        saved.add(playerName);
    }

    public void deauthorisePlayer(@NotNull Player player) {
        if (!plugin.isCalledFromAllowedApplication()) {
            pluginLogger.warn("Unable to deauthorise " + player.getName() + " Reason: Action not allowed");
            return;
        }
        if (!isAuthorised(player)) {
            pluginLogger.warn("Unable to deauthorise " + player.getName() + " Reason: Is not authorised");
            return;
        }
        if (pluginConfig.getSessionSettings().session()) {
            sessions.remove(player.getName());
            return;
        }
        saved.remove(player.getName());
    }

    public void unsavePlayer(@NotNull Player player) {
        saved.remove(player.getName());
    }

    public void unsavePlayer(@NotNull String playerName) {
        saved.remove(playerName);
    }

    public void handleInteraction(@NotNull Player player, Cancellable e) {
        if (isCaptured(player)) {
            e.setCancelled(true);
        }
    }

    public void clearCaptured() {
        this.captured.clear();
    }

    public void clearSessions() {
        this.sessions.clear();
    }

    public void clearSaved() {
        this.saved.clear();
    }
}
