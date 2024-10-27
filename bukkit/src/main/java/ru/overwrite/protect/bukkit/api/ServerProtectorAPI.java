package ru.overwrite.protect.bukkit.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import org.jetbrains.annotations.NotNull;
import ru.overwrite.protect.bukkit.Logger;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.utils.Config;
import ru.overwrite.protect.bukkit.utils.Utils;

public class ServerProtectorAPI {

    private final Config pluginConfig;
    private final Logger logger;
    public final Set<String> login = new HashSet<>();
    public final Map<String, String> sessions = new HashMap<>();
    public final Set<String> saved = new HashSet<>();

    public ServerProtectorAPI(@NotNull ServerProtectorManager plugin) {
        this.pluginConfig = plugin.getPluginConfig();
        this.logger = plugin.getPluginLogger();
    }

    public boolean isCaptured(@NotNull Player p) {
        if (this.login.isEmpty()) {
            return false;
        }
        return this.login.contains(p.getName());
    }

    public void capturePlayer(@NotNull Player p) {
        if (isCaptured(p)) {
            logger.warn("Unable to capture " + p.getName() + " Reason: Already captured");
            return;
        }
        this.login.add(p.getName());
    }

    public void uncapturePlayer(@NotNull Player p) {
        if (!isCaptured(p)) {
            logger.warn("Unable to uncapture " + p.getName() + " Reason: Not captured");
            return;
        }
        this.login.remove(p.getName());
    }

    public boolean isAuthorised(@NotNull Player p) {
        return pluginConfig.session_settings_session ? hasSession(p)
                : saved.contains(p.getName());
    }

    public boolean hasSession(@NotNull Player p) {
        return !sessions.isEmpty() && sessions.containsKey(p.getName()) && sessions.get(p.getName()).equals(Utils.getIp(p));
    }

    public boolean hasSession(@NotNull Player p, @NotNull String ip) {
        return !sessions.isEmpty() && sessions.containsKey(p.getName()) && sessions.get(p.getName()).equals(ip);
    }

    public void authorisePlayer(@NotNull Player p) {
        if (isAuthorised(p)) {
            logger.warn("Unable to authorise " + p.getName() + " Reason: Already authorised");
            return;
        }
        if (pluginConfig.session_settings_session) {
            sessions.put(p.getName(), Utils.getIp(p));
            return;
        }
        saved.add(p.getName());
    }

    public void deauthorisePlayer(@NotNull Player p) {
        if (!isAuthorised(p)) {
            logger.warn("Unable to deauthorise " + p.getName() + " Reason: Is not authorised");
            return;
        }
        if (pluginConfig.session_settings_session) {
            sessions.remove(p.getName(), Utils.getIp(p));
            return;
        }
        saved.remove(p.getName());
    }

    public void handleInteraction(@NotNull Player p, Cancellable e) {
        if (isCaptured(p)) {
            e.setCancelled(true);
        }
    }
}