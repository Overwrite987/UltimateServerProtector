package ru.overwrite.protect.bukkit.api;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
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
    private final Object2IntOpenHashMap<String> rejoins = new Object2IntOpenHashMap<>();

    public ServerProtectorAPI(@NotNull ServerProtectorManager plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
        this.pluginLogger = plugin.getPluginLogger();
    }

    /**
     * Checks if there are any currently captured players.
     *
     * @return true if at least one player is captured, false otherwise
     */
    public boolean isAnybodyCaptured() {
        return !this.captured.isEmpty();
    }

    /**
     * Checks if the specified player is currently captured.
     *
     * @param player the player to check
     * @return true if the player is captured, false otherwise
     */
    public boolean isCaptured(@NotNull Player player) {
        return this.isCaptured(player.getName());
    }

    /**
     * Checks if the specified player (by name) is currently captured.
     *
     * @param playerName the name of the player to check
     * @return true if the player is captured, false otherwise
     */
    public boolean isCaptured(@NotNull String playerName) {
        if (!this.isAnybodyCaptured()) {
            return false;
        }
        return this.captured.contains(playerName);
    }

    /**
     * Captures the specified player, preventing certain interactions.
     *
     * @param player the player to capture
     */
    public void capturePlayer(@NotNull Player player) {
        this.capturePlayer(player.getName());
    }

    /**
     * Captures the specified player (by name), preventing certain interactions.
     *
     * @param playerName the name of the player to capture
     */
    public void capturePlayer(@NotNull String playerName) {
        if (this.isCaptured(playerName)) {
            this.warning("Unable to capture " + playerName + " Reason: Already captured");
            return;
        }
        this.captured.add(playerName);
    }

    /**
     * Releases the specified player from capture state.
     *
     * @param player the player to uncapture
     */
    public void uncapturePlayer(@NotNull Player player) {
        this.uncapturePlayer(player.getName());
    }

    /**
     * Releases the specified player (by name) from capture state.
     *
     * @param playerName the name of the player to uncapture
     */
    public void uncapturePlayer(@NotNull String playerName) {
        if (!plugin.isCalledFromAllowedApplication()) {
            this.warning("Unable to uncapture " + playerName + " Reason: Action not allowed");
            return;
        }
        if (!this.captured.remove(playerName)) {
            this.warning("Unable to uncapture " + playerName + " Reason: Not captured");
        }
    }

    /**
     * Checks if the player is excluded based on the provided exclusion list.
     *
     * @param player the player to check
     * @param list the exclusion list to check against
     * @return true if player is excluded, false otherwise
     */
    public boolean isExcluded(@NotNull Player player, @NotNull List<String> list) {
        return pluginConfig.getSecureSettings().enableExcludedPlayers() && !list.isEmpty() && list.contains(player.getName());
    }

    /**
     * Checks if the player (by name) is excluded based on the provided exclusion list.
     *
     * @param playerName the name of the player to check
     * @param list the exclusion list to check against
     * @return true if player is excluded, false otherwise
     */
    public boolean isExcluded(@NotNull String playerName, @NotNull List<String> list) {
        return pluginConfig.getSecureSettings().enableExcludedPlayers() && !list.isEmpty() && list.contains(playerName);
    }

    /**
     * Checks if the player is currently authorized (either via session or saved status).
     *
     * @param player the player to check
     * @return true if authorized, false otherwise
     */
    public boolean isAuthorised(@NotNull Player player) {
        return this.isAuthorised(player.getName(), Utils.getIp(player));
    }

    /**
     * Checks if the player is currently authorized with the specified IP.
     *
     * @param player the player to check
     * @param ip the IP address to verify
     * @return true if authorized, false otherwise
     */
    public boolean isAuthorised(@NotNull Player player, @NotNull String ip) {
        return this.isAuthorised(player.getName(), ip);
    }

    /**
     * Checks if the player (by name) is currently authorized with the specified IP.
     *
     * @param playerName the name of the player to check
     * @param ip the IP address to verify
     * @return true if authorized, false otherwise
     */
    public boolean isAuthorised(@NotNull String playerName, @NotNull String ip) {
        return pluginConfig.getSessionSettings().session() ?
                this.hasSession(playerName, ip) :
                this.saved.contains(playerName);
    }

    /**
     * Checks if the player has an active session.
     *
     * @param player the player to check
     * @return true if has active session, false otherwise
     */
    public boolean hasSession(@NotNull Player player) {
        return this.hasSession(player.getName(), Utils.getIp(player));
    }

    /**
     * Checks if the player has an active session with the specified IP.
     *
     * @param player the player to check
     * @param ip the IP address to verify
     * @return true if has active session, false otherwise
     */
    public boolean hasSession(@NotNull Player player, @NotNull String ip) {
        return this.hasSession(player.getName(), ip);
    }

    /**
     * Checks if the player (by name) has an active session with the specified IP.
     *
     * @param playerName the name of the player to check
     * @param ip the IP address to verify
     * @return true if has active session, false otherwise
     */
    public boolean hasSession(@NotNull String playerName, @NotNull String ip) {
        if (this.sessions.isEmpty()) {
            return false;
        }
        String sessionIp = this.sessions.get(playerName);
        return sessionIp != null && sessionIp.equals(ip);
    }

    /**
     * Authorizes the player, either creating a session or marking as saved.
     *
     * @param player the player to authorize
     */
    public void authorisePlayer(@NotNull Player player) {
        this.authorisePlayer(player.getName(), Utils.getIp(player));
    }

    /**
     * Authorizes the player with the specified IP.
     *
     * @param player the player to authorize
     * @param ip the IP address to associate
     */
    public void authorisePlayer(@NotNull Player player, @NotNull String ip) {
        this.authorisePlayer(player.getName(), ip);
    }

    /**
     * Authorizes the player (by name) with the specified IP.
     *
     * @param playerName the name of the player to authorize
     * @param ip the IP address to associate
     */
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

    /**
     * Deauthorizes the player, removing session or saved status.
     *
     * @param player the player to deauthorize
     */
    public void deauthorisePlayer(@NotNull Player player) {
        this.deauthorisePlayer(player.getName(), Utils.getIp(player));
    }

    /**
     * Deauthorizes the player with the specified IP.
     *
     * @param player the player to deauthorize
     * @param ip the IP address to verify
     */
    public void deauthorisePlayer(@NotNull Player player, @NotNull String ip) {
        this.deauthorisePlayer(player.getName(), ip);
    }

    /**
     * Deauthorizes the player (by name) with the specified IP.
     *
     * @param playerName the name of the player to deauthorize
     * @param ip the IP address to verify
     */
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

    /**
     * Removes the player's saved status (non-session authorization).
     *
     * @param player the player to unsave
     */
    public void unsavePlayer(@NotNull Player player) {
        this.unsavePlayer(player.getName());
    }

    /**
     * Removes the player's (by name) saved status (non-session authorization).
     *
     * @param playerName the name of the player to unsave
     */
    public void unsavePlayer(@NotNull String playerName) {
        this.saved.remove(playerName);
    }

    /**
     * Handles player interaction by cancelling it if the player is captured.
     *
     * @param player the player interacting
     * @param e the cancellable event
     */
    public void handleInteraction(@NotNull Player player, @NotNull Cancellable e) {
        if (this.isCaptured(player)) {
            e.setCancelled(true);
        }
    }

    /**
     * Gets the number of rejoins for the specified player.
     *
     * @param player the player to check
     * @return number of rejoins
     */
    public int getPlayerRejoins(@NotNull Player player) {
        return getPlayerRejoins(player.getName());
    }

    /**
     * Gets the number of rejoins for the specified player (by name).
     *
     * @param playerName the name of the player to check
     * @return number of rejoins
     */
    public int getPlayerRejoins(@NotNull String playerName) {
        return rejoins.getInt(playerName);
    }

    /**
     * Adds to the player's rejoin count.
     *
     * @param player the player to modify
     * @param amount the number of rejoins to add
     * @return the new total number of rejoins
     */
    public int addRejoin(@NotNull Player player, int amount) {
        return addRejoin(player.getName(), amount);
    }

    /**
     * Adds to the player's (by name) rejoin count.
     *
     * @param playerName the name of the player to modify
     * @param amount the number of rejoins to add
     * @return the new total number of rejoins
     */
    public int addRejoin(@NotNull String playerName, int amount) {
        return rejoins.addTo(playerName, amount);
    }

    /**
     * Clears the rejoin count for the specified player.
     *
     * @param player the player to clear
     */
    public void clearRejoins(@NotNull Player player) {
        clearRejoins(player.getName());
    }

    /**
     * Clears the rejoin count for the specified player (by name).
     *
     * @param playerName the name of the player to clear
     */
    public void clearRejoins(@NotNull String playerName) {
        rejoins.removeInt(playerName);
    }

    /**
     * Clears all captured players (requires proper authorization).
     */
    public void clearCaptured() {
        if (!plugin.isCalledFromAllowedApplication()) {
            this.warning("Unable to clear captured players. Reason: Action not allowed");
            return;
        }
        this.captured.clear();
    }

    /**
     * Clears all active sessions (requires proper authorization).
     */
    public void clearSessions() {
        if (!plugin.isCalledFromAllowedApplication()) {
            this.warning("Unable to clear active sessions. Reason: Action not allowed");
            return;
        }
        this.sessions.clear();
    }

    /**
     * Clears all saved players (requires proper authorization).
     */
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
