package ru.overwrite.protect.bukkit.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.configuration.Config;
import ru.overwrite.protect.bukkit.utils.Utils;
import ru.overwrite.protect.bukkit.utils.logging.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
        return this.isCaptured(player.getName());
    }

    public boolean isCaptured(@NotNull String playerName) {
        if (!this.isAnybodyCaptured()) {
            return false;
        }
        return this.captured.contains(playerName);
    }

    public void capturePlayer(@NotNull Player player) {
        this.capturePlayer(player.getName());
    }

    public void capturePlayer(@NotNull String playerName) {
        if (this.isCaptured(playerName)) {
            this.warning("Unable to capture " + playerName + " Reason: Already captured");
            return;
        }
        this.captured.add(playerName);
    }

    public void uncapturePlayer(@NotNull Player player) {
        this.uncapturePlayer(player.getName());
    }

    public void uncapturePlayer(@NotNull String playerName) {
        if (!plugin.isCalledFromAllowedApplication()) {
            this.warning("Unable to uncapture " + playerName + " Reason: Action not allowed");
            return;
        }
        if (!this.captured.remove(playerName)) {
            this.warning("Unable to uncapture " + playerName + " Reason: Not captured");
        }
    }

    public boolean isAuthorised(@NotNull Player player) {
        return this.isAuthorised(player.getName(), Utils.getIp(player));
    }

    public boolean isAuthorised(@NotNull Player player, @NotNull String ip) {
        return this.isAuthorised(player.getName(), ip);
    }

    public boolean isAuthorised(@NotNull String playerName, @NotNull String ip) {
        return pluginConfig.getSessionSettings().session() ?
                this.hasSession(playerName, ip) :
                this.saved.contains(playerName);
    }

    public boolean hasSession(@NotNull Player player) {
        return this.hasSession(player.getName(), Utils.getIp(player));
    }

    public boolean hasSession(@NotNull Player player, @NotNull String ip) {
        return this.hasSession(player.getName(), ip);
    }

    public boolean hasSession(@NotNull String playerName, @NotNull String ip) {
        if (this.sessions.isEmpty()) {
            return false;
        }
        String sessionIp = this.sessions.get(playerName);
        return sessionIp != null && sessionIp.equals(ip);
    }

    public void authorisePlayer(@NotNull Player player) {
        this.authorisePlayer(player.getName(), Utils.getIp(player));
    }

    public void authorisePlayer(@NotNull Player player, @NotNull String ip) {
        this.authorisePlayer(player.getName(), ip);
    }

    public void authorisePlayer(@NotNull String playerName, @NotNull String ip) {
        if (!plugin.isCalledFromAllowedApplication()) {
            this.warning("Unable to authorise " + playerName + " Reason: Action not allowed");
            return;
        }
        if (isAuthorised(playerName, ip)) {
            this.warning("Unable to authorise " + playerName + " Reason: Already authorised");
            return;
        }
        if (pluginConfig.getSessionSettings().session()) {
            this.sessions.put(playerName, ip);
            return;
        }
        this.saved.add(playerName);
    }

    public void deauthorisePlayer(@NotNull Player player) {
        this.deauthorisePlayer(player.getName(), Utils.getIp(player));
    }

    public void deauthorisePlayer(@NotNull Player player, @NotNull String ip) {
        this.deauthorisePlayer(player.getName(), ip);
    }

    public void deauthorisePlayer(@NotNull String playerName, @NotNull String ip) {
        if (!plugin.isCalledFromAllowedApplication()) {
            this.warning("Unable to deauthorise " + playerName + " Reason: Action not allowed");
            return;
        }
        if (!this.isAuthorised(playerName, ip)) {
            this.warning("Unable to deauthorise " + playerName + " Reason: Is not authorised");
            return;
        }
        if (pluginConfig.getSessionSettings().session()) {
            this.sessions.remove(playerName);
            return;
        }
        this.saved.remove(playerName);
    }

    public void unsavePlayer(@NotNull Player player) {
        this.unsavePlayer(player.getName());
    }

    public void unsavePlayer(@NotNull String playerName) {
        this.saved.remove(playerName);
    }

    public void handleInteraction(@NotNull Player player, Cancellable e) {
        if (this.isCaptured(player)) {
            e.setCancelled(true);
        }
    }

    public void clearCaptured() {
        if (!plugin.isCalledFromAllowedApplication()) {
            this.warning("Unable to clear captured players. Reason: Action not allowed");
            return;
        }
        this.captured.clear();
    }

    public void clearSessions() {
        if (!plugin.isCalledFromAllowedApplication()) {
            this.warning("Unable to clear active sessions. Reason: Action not allowed");
            return;
        }
        this.sessions.clear();
    }

    public void clearSaved() {
        if (!plugin.isCalledFromAllowedApplication()) {
            this.warning("Unable to clear saved players. Reason: Action not allowed");
            return;
        }
        this.saved.clear();
    }

    private void warning(String message) {
        if (!pluginConfig.getMainSettings().suppressApiWarnings()) {
            pluginLogger.warn(message);
        }
    }
}
