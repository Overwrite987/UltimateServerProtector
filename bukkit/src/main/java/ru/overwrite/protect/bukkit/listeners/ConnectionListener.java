package ru.overwrite.protect.bukkit.listeners;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
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
import ru.overwrite.protect.bukkit.configuration.Config;
import ru.overwrite.protect.bukkit.task.Runner;

import java.time.LocalDateTime;
import java.util.List;

public class ConnectionListener implements Listener {

    private final ServerProtectorManager plugin;
    private final ServerProtectorAPI api;
    private final Config pluginConfig;

    private final Runner runner;

    public ConnectionListener(ServerProtectorManager plugin) {
        this.plugin = plugin;
        this.api = plugin.getApi();
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
        final Player player = e.getPlayer();
        player.loadData();
        runner.runAsync(() -> {
            final String playerName = player.getName();
            CaptureReason captureReason = plugin.checkPermissions(player);
            if (api.isCaptured(playerName) && captureReason == null) {
                api.uncapturePlayer(playerName);
                return;
            }
            if (captureReason != null) {
                final String ip = e.getAddress().getHostAddress();
                if (pluginConfig.getSecureSettings().enableIpWhitelist()) {
                    if (!isIPAllowed(ip, pluginConfig.getAccessData().ipWhitelist().get(playerName))) {
                        if (!plugin.isExcluded(player, pluginConfig.getExcludedPlayers().ipWhitelist())) {
                            plugin.checkFail(playerName, pluginConfig.getCommands().notAdminIp());
                        }
                    }
                }
                if (pluginConfig.getSessionSettings().session() && !api.hasSession(playerName, ip)) {
                    if (!plugin.isExcluded(player, pluginConfig.getExcludedPlayers().adminPass())) {
                        ServerProtectorCaptureEvent captureEvent = new ServerProtectorCaptureEvent(player, ip, captureReason);
                        captureEvent.callEvent();
                        if (pluginConfig.getApiSettings().allowCancelCaptureEvent() && captureEvent.isCancelled()) {
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
        runner.runAsync(() -> {
            Player player = e.getPlayer();
            CaptureReason captureReason = plugin.checkPermissions(player);
            if (captureReason != null) {
                if (api.isCaptured(player)) {
                    if (pluginConfig.getEffectSettings().enableEffects()) {
                        plugin.giveEffects(player);
                    }
                    plugin.applyHide(player);
                }
                if (pluginConfig.getLoggingSettings().loggingJoin()) {
                    plugin.logAction(pluginConfig.getLogMessages().joined(), player, LocalDateTime.now());
                }
                if (pluginConfig.getBroadcasts() != null) {
                    plugin.sendAlert(player, pluginConfig.getBroadcasts().joined());
                }
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

    private final Object2IntOpenHashMap<String> rejoins = new Object2IntOpenHashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        handlePlayerLeave(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        handlePlayerLeave(player);
    }

    private void handlePlayerLeave(Player player) {
        String playerName = player.getName();
        if (api.isCaptured(player)) {
            for (PotionEffect effect : player.getActivePotionEffects()) { // Old versions compatibility
                player.removePotionEffect(effect.getType());
            }
            if (pluginConfig.getPunishSettings().enableRejoin()) {
                handleRejoin(playerName);
            }
        }
        plugin.getPerPlayerTime().removeInt(playerName);
        api.unsavePlayer(playerName);
    }

    private void handleRejoin(String playerName) {
        int attempts = rejoins.addTo(playerName, 1);
        if (attempts > pluginConfig.getPunishSettings().maxRejoins()) {
            plugin.checkFail(playerName, pluginConfig.getCommands().failedRejoin());
            rejoins.removeInt(playerName);
        }
    }
}
