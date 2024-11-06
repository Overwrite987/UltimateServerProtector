package ru.overwrite.protect.bukkit.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.api.CaptureReason;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.api.events.ServerProtectorCaptureEvent;
import ru.overwrite.protect.bukkit.task.Runner;
import ru.overwrite.protect.bukkit.configuration.Config;

import java.util.Date;
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
    public void onLogin(PlayerLoginEvent e) {
        Player p = e.getPlayer();
        runner.runAsync(() -> {
            CaptureReason captureReason = plugin.checkPermissions(p);
            if (api.isCaptured(p) && captureReason == null) {
                api.uncapturePlayer(p);
                return;
            }
            if (captureReason != null) {
                String playerName = p.getName();
                String ip = e.getAddress().getHostAddress();
                if (pluginConfig.getSecureSettings().enableIpWhitelist()) {
                    if (!isIPAllowed(playerName, ip)) {
                        if (pluginConfig.getExcludedPlayers() == null || !plugin.isExcluded(p, pluginConfig.getExcludedPlayers().ipWhitelist())) {
                            plugin.checkFail(playerName, pluginConfig.getCommands().notAdminIp());
                        }
                    }
                }
                if (pluginConfig.getSessionSettings().session() && !api.hasSession(p, ip)) {
                    if (pluginConfig.getExcludedPlayers() == null || !plugin.isExcluded(p, pluginConfig.getExcludedPlayers().adminPass())) {
                        ServerProtectorCaptureEvent captureEvent = new ServerProtectorCaptureEvent(p, ip, captureReason);
                        if (pluginConfig.getApiSettings().callEventOnCapture()) {
                            captureEvent.callEvent();
                        }
                        if (captureEvent.isCancelled()) {
                            return;
                        }
                        api.capturePlayer(p);
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
                    plugin.logAction("log-format.joined", p, new Date());
                }
                plugin.sendAlert(p, pluginConfig.getBroadcasts().joined());
            }
        });
    }

    private boolean isIPAllowed(String p, String ip) {
        final List<String> ips = pluginConfig.getAccessData().ipWhitelist().get(p);
        if (ips == null || ips.isEmpty()) {
            return false;
        }
        final String[] ipParts = ip.split("\\.");

        for (String allowedIP : ips) {
            final String[] allowedParts = allowedIP.split("\\.");
            if (ipParts.length != allowedParts.length) {
                continue;
            }

            boolean matches = true;
            for (int i = 0; i < ipParts.length; i++) {
                if (!allowedParts[i].equals("*") && !allowedParts[i].equals(ipParts[i])) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
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
        plugin.getPerPlayerTime().remove(playerName);
        api.unsavePlayer(playerName);
    }

    private boolean isMaxRejoins(String playerName) {
        if (!rejoins.containsKey(playerName))
            return false;
        return rejoins.get(playerName) > pluginConfig.getPunishSettings().maxRejoins();
    }
}