package ru.overwrite.protect.bukkit.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.api.CaptureReason;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.api.events.ServerProtectorCaptureEvent;
import ru.overwrite.protect.bukkit.task.Runner;
import ru.overwrite.protect.bukkit.configuration.Config;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionListener implements Listener {

    private final ServerProtectorManager plugin;
    private final ServerProtectorAPI api;
    private final Config pluginConfig;

    private final Runner runner;

    public ConnectionListener(ServerProtectorManager plugin) {
        this.plugin = plugin;
        this.api = plugin.getPluginAPI();
        this.pluginConfig = plugin.getPluginConfig();
        this.runner = plugin.getRunner();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent e) {
        if (!plugin.isSafe()) {
            plugin.logUnsafe();
            e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(PlayerLoginEvent e) {
        Player p = e.getPlayer();
        runner.runAsync(() -> {
            final String playerName = p.getName();
            CaptureReason captureReason = plugin.checkPermissions(p);
            if (api.isCaptured(playerName) && captureReason == null) {
                api.uncapturePlayer(playerName);
                return;
            }
            if (captureReason != null) {
                final String ip = e.getAddress().getHostAddress();
                if (pluginConfig.getSecureSettings().enableIpWhitelist()) {
                    if (!isIPAllowed(ip, pluginConfig.getAccessData().ipWhitelist().get(playerName))) {
                        if (pluginConfig.getExcludedPlayers() == null || !plugin.isExcluded(p, pluginConfig.getExcludedPlayers().ipWhitelist())) {
                            plugin.checkFail(playerName, pluginConfig.getCommands().notAdminIp());
                        }
                    }
                }
                if (pluginConfig.getSessionSettings().session() && !api.hasSession(playerName, ip)) {
                    if (pluginConfig.getExcludedPlayers() == null || !plugin.isExcluded(p, pluginConfig.getExcludedPlayers().adminPass())) {
                        ServerProtectorCaptureEvent captureEvent = new ServerProtectorCaptureEvent(p, ip, captureReason);
                        if (pluginConfig.getApiSettings().callEventOnCapture()) {
                            captureEvent.callEvent();
                        }
                        if (captureEvent.isCancelled()) {
                            return;
                        }
                        api.capturePlayer(playerName);
                    }
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        runner.runAsync(() -> {
            CaptureReason captureReason = plugin.checkPermissions(p);
            if (captureReason != null) {
                if (api.isCaptured(p)) {
                    if (pluginConfig.getEffectSettings().enableEffects()) {
                        plugin.giveEffect(p);
                    }
                    plugin.applyHide(p);
                }
                if (pluginConfig.getLoggingSettings().loggingJoin()) {
                    plugin.logAction(pluginConfig.getLogFormats().joined(), p, LocalDateTime.now());
                }
                plugin.sendAlert(p, pluginConfig.getBroadcasts().joined());
            }
        });
    }

    private boolean isIPAllowed(String playerIp, List<String> allowedIps) {
        if (allowedIps == null || allowedIps.isEmpty()) {
            return false;
        }

        outer:
        for (int i = 0; i < allowedIps.size(); i++) {
            final String allowedIp = allowedIps.get(i);
            int playerIpLength = playerIp.length();
            int allowedIpLength = allowedIp.length();

            if (playerIpLength != allowedIpLength && !allowedIp.contains("*")) {
                continue;
            }

            for (int n = 0; n < allowedIpLength; n++) {
                char currentChar = allowedIp.charAt(n);
                if (currentChar == '*') {
                    return true;
                }

                if (n >= playerIpLength || currentChar != playerIp.charAt(n)) {
                    continue outer;
                }
            }

            if (playerIpLength == allowedIpLength) {
                return true;
            }
        }

        return false;
    }

    private final Map<String, Integer> rejoins = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeave(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        handlePlayerLeave(p);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onKick(PlayerKickEvent event) {
        Player p = event.getPlayer();
        handlePlayerLeave(p);
    }

    private void handlePlayerLeave(Player p) {
        String playerName = p.getName();
        if (api.isCaptured(p)) {
            for (PotionEffect effect : p.getActivePotionEffects()) {
                p.removePotionEffect(effect.getType());
            }
            if (pluginConfig.getPunishSettings().enableRejoin()) {
                rejoins.put(playerName, rejoins.getOrDefault(playerName, 0) + 1);
                if (isMaxRejoins(playerName)) {
                    rejoins.remove(playerName);
                    plugin.checkFail(playerName, pluginConfig.getCommands().failedRejoin());
                }
            }
        }
        if (plugin.getPerPlayerTime() != null) {
            plugin.getPerPlayerTime().remove(playerName);
        }
        api.unsavePlayer(playerName);
    }

    private boolean isMaxRejoins(String playerName) {
        if (!rejoins.containsKey(playerName))
            return false;
        return rejoins.get(playerName) > pluginConfig.getPunishSettings().maxRejoins();
    }
}
