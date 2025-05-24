package ru.overwrite.protect.bukkit;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.potion.PotionEffect;
import ru.overwrite.protect.bukkit.api.CaptureReason;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.commands.PasCommand;
import ru.overwrite.protect.bukkit.commands.UspCommand;
import ru.overwrite.protect.bukkit.configuration.Config;
import ru.overwrite.protect.bukkit.configuration.data.BlockingSettings;
import ru.overwrite.protect.bukkit.configuration.data.SecureSettings;
import ru.overwrite.protect.bukkit.configuration.data.SystemMessages;
import ru.overwrite.protect.bukkit.listeners.ChatListener;
import ru.overwrite.protect.bukkit.listeners.ConnectionListener;
import ru.overwrite.protect.bukkit.listeners.MainListener;
import ru.overwrite.protect.bukkit.listeners.TabCompleteListener;
import ru.overwrite.protect.bukkit.task.BukkitRunner;
import ru.overwrite.protect.bukkit.task.PaperRunner;
import ru.overwrite.protect.bukkit.task.Runner;
import ru.overwrite.protect.bukkit.task.TaskManager;
import ru.overwrite.protect.bukkit.utils.PAPIUtils;
import ru.overwrite.protect.bukkit.utils.PluginMessage;
import ru.overwrite.protect.bukkit.utils.Utils;
import ru.overwrite.protect.bukkit.utils.logging.BukkitLogger;
import ru.overwrite.protect.bukkit.utils.logging.Logger;
import ru.overwrite.protect.bukkit.utils.logging.PaperLogger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class ServerProtectorManager extends JavaPlugin {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("'['dd-MM-yyyy']' HH:mm:ss -");

    private final Logger pluginLogger = Utils.FOLIA ?
            new PaperLogger(this) :
            new BukkitLogger(this);

    private boolean paper;

    private FileConfiguration messageFile;

    @Setter
    private FileConfiguration dataFile;
    private String dataFileName;
    private String dataFilePath;
    private final Config pluginConfig = new Config(this);
    private final ServerProtectorAPI api = new ServerProtectorAPI(this);
    private final PasswordHandler passwordHandler = new PasswordHandler(this);
    private final Runner runner = Utils.FOLIA ? new PaperRunner(this) : new BukkitRunner(this);

    private PluginMessage pluginMessage;

    private final Map<String, Integer> perPlayerTime = new HashMap<>();

    @Getter(AccessLevel.NONE)
    private File logFile;

    @Getter(AccessLevel.NONE)
    public final Server server = getServer();

    public void checkPaper() {
        if (server.getName().equals("CraftBukkit")) {
            SystemMessages systemMessages = pluginConfig.getSystemMessages();
            runner.runPeriodical(() -> {
                pluginLogger.info(systemMessages.baselineWarn());
                pluginLogger.info(systemMessages.paper1());
                pluginLogger.info(systemMessages.paper2());
                pluginLogger.info(systemMessages.baselineWarn());
            }, 0L, 20L * 1800);
            return;
        }
        this.paper = true;
    }

    private boolean safe;

    public void checkSafe(PluginManager pluginManager) {
        if (server.spigot().getConfig().getBoolean("settings.bungeecord")) {
            if (pluginManager.isPluginEnabled("BungeeGuard") || pluginManager.isPluginEnabled("SafeNET")) {
                this.safe = true;
                return;
            }
            logUnsafe();
            return;
        }
        this.safe = true;
    }

    public void logUnsafe() {
        SystemMessages systemMessages = pluginConfig.getSystemMessages();
        pluginLogger.info(systemMessages.baselineWarn());
        pluginLogger.info(systemMessages.bungeecord1());
        pluginLogger.info(systemMessages.bungeecord2());
        pluginLogger.info(systemMessages.bungeecord3());
        pluginLogger.info(systemMessages.baselineWarn());
    }

    public void setupProxy(FileConfiguration config) {
        if (config.getBoolean("main-settings.proxy", false)) {
            Messenger messenger = server.getMessenger();
            messenger.registerOutgoingPluginChannel(this, "BungeeCord");
            pluginMessage = new PluginMessage(this);
            messenger.registerIncomingPluginChannel(this, "BungeeCord", pluginMessage);
        }
    }

    public void loadConfigs(FileConfiguration config) {
        Utils.setupColorizer(config.getConfigurationSection("main-settings"));
        final ConfigurationSection fileSettings = config.getConfigurationSection("file-settings");
        boolean fullPath = fileSettings.getBoolean("use-full-path", false);
        dataFilePath = fullPath ? fileSettings.getString("data-file-path") : getDataFolder().getAbsolutePath();
        dataFileName = fileSettings.getString("data-file");
        dataFile = pluginConfig.getFile(dataFilePath, dataFileName);
        pluginConfig.save(dataFilePath, dataFile, dataFileName);
        messageFile = pluginConfig.getFile(getDataFolder().getAbsolutePath(), "message.yml");
        pluginConfig.save(getDataFolder().getAbsolutePath(), messageFile, "message.yml");
        setupPluginConfig(config);
        pluginConfig.setupPasswords(dataFile);
    }

    public void reloadConfigs() {
        runner.runAsync(() -> {
            reloadConfig();
            final FileConfiguration config = getConfig();
            Utils.setupColorizer(config.getConfigurationSection("main-settings"));
            messageFile = pluginConfig.getFile(getDataFolder().getAbsolutePath(), "message.yml");
            final ConfigurationSection fileSettings = config.getConfigurationSection("file-settings");
            boolean fullPath = fileSettings.getBoolean("use-full-path", false);
            dataFilePath = fullPath ? fileSettings.getString("data-file-path") : getDataFolder().getAbsolutePath();
            dataFileName = fileSettings.getString("data-file");
            dataFile = pluginConfig.getFile(dataFilePath, dataFileName);
            setupPluginConfig(config);
            pluginConfig.setupPasswords(dataFile);
        });
    }

    private void setupPluginConfig(FileConfiguration config) {
        pluginConfig.loadAccessData(config);
        pluginConfig.setupExcluded(config);
        final FileConfiguration configFile = pluginConfig.getFile(dataFilePath, "config.yml");
        pluginConfig.loadMainSettings(config, configFile);
        pluginConfig.loadEncryptionSettings(config, configFile);
        pluginConfig.loadSecureSettings(config, configFile);
        pluginConfig.loadApiSettings(config, configFile);
        pluginConfig.loadGeyserSettings(config, configFile);
        pluginConfig.loadAdditionalChecks(config, configFile);
        pluginConfig.loadPunishSettings(config, configFile);
        pluginConfig.loadSessionSettings(config, configFile);
        pluginConfig.loadMessageSettings(config, configFile);
        pluginConfig.loadBossbarSettings(config, configFile);
        pluginConfig.loadSoundSettings(config, configFile);
        pluginConfig.loadEffects(config, configFile);
        pluginConfig.loadLoggingSettings(config, configFile);
        pluginConfig.loadFailCommands(config, configFile);
        pluginConfig.loadMsgMessages(messageFile);
        pluginConfig.loadUspMessages(messageFile);
        pluginConfig.loadLogFormats(messageFile);
        pluginConfig.loadSystemMessages(messageFile);
        final ConfigurationSection messageSettings = config.getConfigurationSection("message-settings");
        if (messageSettings.getBoolean("send-titles")) {
            pluginConfig.loadTitleMessages(messageFile);
        }
        if (messageSettings.getBoolean("enable-broadcasts")
                || messageSettings.getBoolean("enable-console-broadcasts")) {
            pluginConfig.loadBroadcastMessages(messageFile);
        }
    }

    public void registerListeners(PluginManager pluginManager) {
        pluginManager.registerEvents(new ChatListener(this), this);
        pluginManager.registerEvents(new ConnectionListener(this), this);
        pluginManager.registerEvents(new MainListener(this), this);
        if (paper && pluginConfig.getBlockingSettings().blockTabComplete()) {
            pluginManager.registerEvents(new TabCompleteListener(this), this);
        }
    }

    public void registerCommands(PluginManager pluginManager, ConfigurationSection mainSettings) {
        if (paper && mainSettings.getBoolean("use-command", true)) {
            try {
                CommandMap commandMap = server.getCommandMap();
                Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
                constructor.setAccessible(true);
                PluginCommand command = constructor.newInstance(mainSettings.getString("pas-command", "pas"), this);
                command.setExecutor(new PasCommand(this));
                commandMap.register(getDescription().getName(), command);
            } catch (Exception ex) {
                pluginLogger.info("Unable to register password command! " + ex.getMessage());
                pluginManager.disablePlugin(this);
            }
        } else {
            pluginLogger.info("Command for password entering will not be registered.");
        }
        PluginCommand uspCommand = getCommand("ultimateserverprotector");
        UspCommand uspCommandClass = new UspCommand(this);
        uspCommand.setExecutor(uspCommandClass);
    }

    public void startTasks(FileConfiguration config) {
        TaskManager taskManager = new TaskManager(this);
        taskManager.startMainCheck(pluginConfig.getMainSettings().checkInterval());
        taskManager.startCapturesMessages(config);
        if (pluginConfig.getPunishSettings().enableTime()) {
            taskManager.startCapturesTimer();
        }
        SecureSettings secureSettings = pluginConfig.getSecureSettings();
        if (secureSettings.enableNotAdminPunish()) {
            taskManager.startAdminCheck();
        }
        if (secureSettings.enableOpWhitelist()) {
            taskManager.startOpCheck();
        }
        if (secureSettings.enablePermissionBlacklist()) {
            taskManager.startPermsCheck();
        }
    }

    public void setupLogger(FileConfiguration config) {
        File dataFolder = getDataFolder();
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new RuntimeException("Unable to create data folder");
        }
        ConfigurationSection fileSettings = config.getConfigurationSection("file-settings");
        boolean fullPath = fileSettings.getBoolean("use-full-path");
        String logFilePath = fullPath ? fileSettings.getString("log-file-path") : dataFolder.getPath();
        logFile = new File(logFilePath, fileSettings.getString("log-file"));
    }

    public void checkForUpdates(ConfigurationSection mainSettings) {
        if (!mainSettings.getBoolean("update-checker", true)) {
            return;
        }
        Utils.checkUpdates(this, version -> {
            SystemMessages systemMessages = pluginConfig.getSystemMessages();
            pluginLogger.info(systemMessages.baselineDefault());
            if (getDescription().getVersion().equals(version)) {
                pluginLogger.info(systemMessages.updateLatest());
            } else {
                pluginLogger.info(systemMessages.updateOutdated1());
                pluginLogger.info(systemMessages.updateOutdated2());
                pluginLogger.info(systemMessages.updateOutdated3());
            }
            pluginLogger.info(systemMessages.baselineDefault());
        });
    }

    public void checkFail(String playerName, List<String> commands) {
        if (commands.isEmpty()) {
            return;
        }
        runner.run(() -> {
            for (String command : commands) {
                server.dispatchCommand(server.getConsoleSender(), command.replace("%player%", playerName));
                if (pluginConfig.getLoggingSettings().loggingCommandExecution()) {
                    LocalDateTime date = LocalDateTime.now();
                    logToFile(pluginConfig.getLogMessages().command()
                            .replace("%player%", playerName)
                            .replace("%cmd%", command)
                            .replace("%date%", date.format(TIME_FORMATTER)));
                }
            }
        });
    }

    @Getter(AccessLevel.NONE)
    private final Map<String, Collection<PotionEffect>> oldEffects = new HashMap<>();

    public void giveEffects(Player player) {
        runner.runPlayer(() -> {
            if (!player.getActivePotionEffects().isEmpty()) {
                oldEffects.put(player.getName(), player.getActivePotionEffects());
            }
            player.addPotionEffects(pluginConfig.getEffectSettings().effects());
        }, player);
    }

    public void removeEffects(Player player) {
        runner.runPlayer(() -> {
            for (PotionEffect effect : player.getActivePotionEffects()) { // Old versions compatibility
                player.removePotionEffect(effect.getType());
            }
            if (oldEffects.isEmpty()) {
                return;
            }
            Collection<PotionEffect> effects = oldEffects.get(player.getName());
            if (effects != null) {
                player.addPotionEffects(effects);
            }
        }, player);
    }

    public void applyHide(Player player) {
        runner.runPlayer(() -> {
            BlockingSettings blockingSettings = pluginConfig.getBlockingSettings();
            if (!blockingSettings.hideOnEntering() && !blockingSettings.hideOtherOnEntering()) {
                return;
            }
            for (Player onlinePlayer : server.getOnlinePlayers()) {
                if (blockingSettings.hideOnEntering()) {
                    onlinePlayer.hidePlayer(this, player);
                }
                if (blockingSettings.hideOtherOnEntering()) {
                    player.hidePlayer(this, onlinePlayer);
                }
            }
        }, player);
    }

    public void logEnableDisable(String msg, LocalDateTime date) {
        if (getConfig().getBoolean("logging-settings.logging-enable-disable", true)) {
            logToFile(msg.replace("%date%", date.format(TIME_FORMATTER)));
        }
    }

    public CaptureReason checkPermissions(Player player) {
        if (player.isOp()) {
            return new CaptureReason(null);
        }
        if (player.hasPermission("serverprotector.protect")) {
            return new CaptureReason("serverprotector.protect");
        }
        for (String perm : pluginConfig.getAccessData().perms()) {
            if (player.hasPermission(perm)) {
                return new CaptureReason(perm);
            }
        }
        return null;
    }

    public boolean isExcluded(Player player, List<String> list) {
        return pluginConfig.getSecureSettings().enableExcludedPlayers() && !list.isEmpty() && list.contains(player.getName());
    }

    public boolean isAdmin(String nick) {
        return pluginConfig.getPerPlayerPasswords().containsKey(nick);
    }

    public void sendAlert(Player player, String msg) {
        msg = msg.replace("%player%", player.getName()).replace("%ip%", Utils.getIp(player));
        if (pluginConfig.getMainSettings().papiSupport()) {
            msg = PAPIUtils.parsePlaceholders(player, msg);
        }
        if (pluginConfig.getMessageSettings().enableBroadcasts()) {
            for (Player onlinePlayer : server.getOnlinePlayers()) {
                if (onlinePlayer.hasPermission("serverprotector.admin") && player != onlinePlayer) {
                    onlinePlayer.sendMessage(msg);
                }
            }
            if (this.pluginMessage != null) {
                pluginMessage.sendCrossProxy(player, msg);
            }
        }
        if (pluginConfig.getMessageSettings().enableConsoleBroadcasts()) {
            server.getConsoleSender().sendMessage(msg);
        }
    }

    public void logAction(String message, Player player, LocalDateTime date) {
        runner.runAsync(() ->
                logToFile(message
                        .replace("%player%", player.getName())
                        .replace("%ip%", Utils.getIp(player))
                        .replace("%date%", date.format(TIME_FORMATTER)))
        );
    }

    public void logToFile(String message) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(logFile, true))) {
            bufferedWriter.write(message);
            bufferedWriter.newLine();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
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
        List<String> allowedAuthApiCallsPackages = pluginConfig.getApiSettings().allowedAuthApiCallsPackages();
        if (allowedAuthApiCallsPackages.isEmpty()) {
            pluginLogger.warn("Found illegal method call from " + className);
            return false;
        }
        for (int i = 0; i < allowedAuthApiCallsPackages.size(); i++) {
            String allowed = allowedAuthApiCallsPackages.get(i);
            if (className.startsWith(allowed)) {
                return true;
            }
        }
        pluginLogger.warn("Found illegal method call from " + className);
        return false;
    }
}
