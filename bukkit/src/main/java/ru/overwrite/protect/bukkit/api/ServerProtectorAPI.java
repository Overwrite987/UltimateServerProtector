package ru.overwrite.protect.bukkit.api;

import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import org.jetbrains.annotations.NotNull;
import ru.overwrite.protect.bukkit.utils.logging.Logger;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.utils.configuration.Config;
import ru.overwrite.protect.bukkit.utils.Utils;

public class ServerProtectorAPI {

    private final Config pluginConfig;
    private final Logger logger;

    private final Set<String> captured = new HashSet<>();
    private final Map<String, String> sessions = new HashMap<>();
    private final Set<String> saved = new HashSet<>();

    public ServerProtectorAPI(@NotNull ServerProtectorManager plugin) {
        this.pluginConfig = plugin.getPluginConfig();
        this.logger = plugin.getPluginLogger();
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
            logger.warn("Unable to capture " + player.getName() + " Reason: Already captured");
            return;
        }
        this.captured.add(player.getName());
    }

    public void capturePlayer(@NotNull String playerName) {
        if (isCaptured(playerName)) {
            logger.warn("Unable to capture " + playerName + " Reason: Already captured");
            return;
        }
        this.captured.add(playerName);
    }

    public void uncapturePlayer(@NotNull Player player) {
        if (!isCalledFromAllowedApplication()) {
            logger.warn("Unable to uncapture " + player.getName() + " Reason: Action not allowed");
            return;
        }
        if (!isCaptured(player)) {
            logger.warn("Unable to uncapture " + player.getName() + " Reason: Not captured");
            return;
        }
        this.captured.remove(player.getName());
    }

    public void uncapturePlayer(@NotNull String playerName) {
        if (!isCalledFromAllowedApplication()) {
            logger.warn("Unable to uncapture " + playerName + " Reason: Action not allowed");
            return;
        }
        if (!isCaptured(playerName)) {
            logger.warn("Unable to uncapture " + playerName + " Reason: Not captured");
            return;
        }
        this.captured.remove(playerName);
    }

    public boolean isAuthorised(@NotNull Player player) {
        return pluginConfig.getSessionSettings().session() ?
                hasSession(player) :
                saved.contains(player.getName());
    }

    public boolean hasSession(@NotNull Player player) {
        return !sessions.isEmpty() && sessions.containsKey(player.getName()) && sessions.get(player.getName()).equals(Utils.getIp(player));
    }

    public boolean hasSession(@NotNull Player player, @NotNull String ip) {
        return !sessions.isEmpty() && sessions.containsKey(player.getName()) && sessions.get(player.getName()).equals(ip);
    }

    public void authorisePlayer(@NotNull Player player) {
        if (!isCalledFromAllowedApplication()) {
            logger.warn("Unable to authorise " + player.getName() + " Reason: Action not allowed");
            return;
        }
        if (isAuthorised(player)) {
            logger.warn("Unable to authorise " + player.getName() + " Reason: Already authorised");
            return;
        }
        if (pluginConfig.getSessionSettings().session()) {
            sessions.put(player.getName(), Utils.getIp(player));
            return;
        }
        saved.add(player.getName());
    }

    public void deauthorisePlayer(@NotNull Player player) {
        if (!isCalledFromAllowedApplication()) {
            logger.warn("Unable to deauthorise " + player.getName() + " Reason: Action not allowed");
            return;
        }
        if (!isAuthorised(player)) {
            logger.warn("Unable to deauthorise " + player.getName() + " Reason: Is not authorised");
            return;
        }
        if (pluginConfig.getSessionSettings().session()) {
            sessions.remove(player.getName(), Utils.getIp(player));
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

    public boolean isCalledFromAllowedApplication() {
        StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

        List<Class<?>> callStack = walker.walk(frames ->
                frames.map(StackWalker.StackFrame::getDeclaringClass)
                        .collect(Collectors.toList())
        );
        String className = callStack.get(2).getName();

        if (className.startsWith("ru.overwrite.protect.bukkit")) {
            return true;
        }
        if (pluginConfig.getApiSettings().allowedAuthApiCallsPackages().isEmpty()) {
            logger.warn("Found illegal method call from " + className);
            return false;
        }
        for (String allowed : pluginConfig.getApiSettings().allowedAuthApiCallsPackages()) {
            if (className.startsWith(allowed)) {
                return true;
            }
        }
        logger.warn("Found illegal method call from " + className);
        return false;
    }
}