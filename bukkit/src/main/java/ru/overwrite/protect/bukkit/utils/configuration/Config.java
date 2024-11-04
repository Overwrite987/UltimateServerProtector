package ru.overwrite.protect.bukkit.utils.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.overwrite.protect.bukkit.utils.Utils;
import ru.overwrite.protect.bukkit.utils.configuration.data.*;
import ru.overwrite.protect.bukkit.utils.logging.Logger;
import ru.overwrite.protect.bukkit.ServerProtectorManager;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class Config {

    private final ServerProtectorManager plugin;
    private final Logger pluginLogger;

    public Config(ServerProtectorManager plugin) {
        this.plugin = plugin;
        this.pluginLogger = plugin.getPluginLogger();
    }

    private String serializer;

    public String getSerializer() {
        return this.serializer;
    }

    private Map<String, String> perPlayerPasswords;

    public Map<String, String> getPerPlayerPasswords() {
        return this.perPlayerPasswords;
    }

    private MainSettings mainSettings;

    public MainSettings getMainSettings() {
        return this.mainSettings;
    }

    private EncryptionSettings encryptionSettings;

    public EncryptionSettings getEncryptionSettings() {
        return this.encryptionSettings;
    }

    private GeyserSettings geyserSettings;

    public GeyserSettings getGeyserSettings() {
        return this.geyserSettings;
    }

    private BlockingSettings blockingSettings;

    public BlockingSettings getBlockingSettings() {
        return this.blockingSettings;
    }

    private SessionSettings sessionSettings;

    public SessionSettings getSessionSettings() {
        return this.sessionSettings;
    }

    private PunishSettings punishSettings;

    public PunishSettings getPunishSettings() {
        return this.punishSettings;
    }

    private SecureSettings secureSettings;

    public SecureSettings getSecureSettings() {
        return this.secureSettings;
    }

    private ApiSettings apiSettings;

    public ApiSettings getApiSettings() {
        return this.apiSettings;
    }

    private MessageSettings messageSettings;

    public MessageSettings getMessageSettings() {
        return this.messageSettings;
    }

    private BossbarSettings bossbarSettings;

    public BossbarSettings getBossbarSettings() {
        return this.bossbarSettings;
    }

    private SoundSettings soundSettings;

    public SoundSettings getSoundSettings() {
        return this.soundSettings;
    }

    private EffectSettings effectSettings;

    public EffectSettings getEffectSettings() {
        return this.effectSettings;
    }

    private LoggingSettings loggingSettings;

    public LoggingSettings getLoggingSettings() {
        return this.loggingSettings;
    }

    private Commands commands;

    public Commands getCommands() {
        return this.commands;
    }

    private AccessData accessData;

    public AccessData getAccessData() {
        return this.accessData;
    }

    private ExcludedPlayers excludedPlayers;

    public ExcludedPlayers getExcludedPlayers() {
        return this.excludedPlayers;
    }

    private UspMessages uspMessages;

    public UspMessages getUspMessages() {
        return this.uspMessages;
    }

    private Messages messages;

    public Messages getMessages() {
        return this.messages;
    }

    private Titles titles;

    public Titles getTitles() {
        return this.titles;
    }

    private Broadcasts broadcasts;

    public Broadcasts getBroadcasts() {
        return this.broadcasts;
    }

    public void setupPasswords(FileConfiguration dataFile) {
        perPlayerPasswords = new ConcurrentHashMap<>();
        ConfigurationSection data = dataFile.getConfigurationSection("data");
        boolean shouldSave = false;
        for (String nick : data.getKeys(false)) {
            String playerNick = nick;
            if (this.geyserSettings.prefix() != null && !this.geyserSettings.prefix().isBlank() && this.geyserSettings.nicknames().contains(nick)) {
                playerNick = this.geyserSettings.prefix() + nick;
            }
            if (!this.encryptionSettings.enableEncryption()) {
                perPlayerPasswords.put(playerNick, data.getString(nick + ".pass"));
            } else {
                if (this.encryptionSettings.autoEncryptPasswords()) {
                    if (data.getString(nick + ".pass") != null) {
                        String encryptedPas = Utils.encryptPassword(data.getString(nick + ".pass"), Utils.generateSalt(this.encryptionSettings.saltLength()), this.encryptionSettings.encryptMethods());
                        dataFile.set("data." + nick + ".encrypted-pass", encryptedPas);
                        dataFile.set("data." + nick + ".pass", null);
                        shouldSave = true;
                        plugin.setDataFile(dataFile);
                    }
                }
                perPlayerPasswords.put(playerNick, data.getString(nick + ".encrypted-pass"));
            }
        }
        if (shouldSave) {
            save(plugin.getDataFilePath(), dataFile, plugin.getDataFileName());
        }
    }

    public void loadMainSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection mainSettings = config.getConfigurationSection("main-settings");
        if (!configFile.contains("main-settings")) {
            pluginLogger.warn("Configuration section main-settings not found!");
            configFile.createSection("main-settings");
            configFile.set("main-settings.serializer", "LEGACY");
            configFile.set("main-settings.prefix", "[UltimateServerProtector]");
            configFile.set("main-settings.pas-command", "pas");
            configFile.set("main-settings.use-command", true);
            configFile.set("main-settings.enable-admin-commands", false);
            configFile.set("main-settings.check-interval", 40);
            configFile.set("main-settings.papi-support", false);
            save(plugin.getDataFilePath(), configFile, "config.yml");
            pluginLogger.info("Created section main-settings");
            mainSettings = configFile.getConfigurationSection("main-settings");
        }
        serializer = mainSettings.getString("serializer", "LEGACY").toUpperCase();
        this.mainSettings = new MainSettings(
                mainSettings.getString("prefix", "[UltimateServerProtector]"),
                mainSettings.getString("pas-command", "pas"),
                mainSettings.getBoolean("use-command", true),
                mainSettings.getBoolean("enable-admin-commands", false),
                mainSettings.getLong("check-interval", 40),
                mainSettings.getBoolean("papi-support", false)
        );
    }

    public void loadEncryptionSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection encryptionSettings = config.getConfigurationSection("encryption-settings");
        if (!configFile.contains("encryption-settings")) {
            pluginLogger.warn("Configuration section encryption-settings not found!");
            configFile.createSection("encryption-settings");
            configFile.set("encryption-settings.encrypt-method", "");
            configFile.set("encryption-settings.salt-length", 24);
            configFile.set("encryption-settings.auto-encrypt-passwords", true);
            configFile.set("encryption-settings.old-encrypt-methods", Collections.emptyList());
            configFile.set("encryption-settings.encrypt-method", "");
            save(plugin.getDataFilePath(), configFile, "config.yml");
            pluginLogger.info("Created section encryption-settings");
            encryptionSettings = configFile.getConfigurationSection("encryption-settings");
        }
        this.encryptionSettings = new EncryptionSettings(
                encryptionSettings.getBoolean("enable-encryption", false),
                encryptionSettings.getString("encrypt-method", "").trim(),
                getEncryptionMethods(encryptionSettings),
                encryptionSettings.getInt("salt-length", 24),
                encryptionSettings.getBoolean("auto-encrypt-passwords", true),
                getOldEncryptionMethods(encryptionSettings)
        );
    }

    private List<String> getEncryptionMethods(ConfigurationSection section) {
        String encryptionMethod = section.getString("encrypt-method", "").trim();
        return encryptionMethod.isEmpty() ? List.of() : List.of(encryptionMethod.split(";"));
    }

    private List<List<String>> getOldEncryptionMethods(ConfigurationSection section) {
        List<List<String>> oldMethods = new ArrayList<>();
        List<String> oldEncryptionMethods = section.getStringList("old-encrypt-methods");

        for (String oldEncryptionMethod : oldEncryptionMethods) {
            if (oldEncryptionMethod.contains(";")) {
                oldMethods.add(List.of(oldEncryptionMethod.trim().split(";")));
            } else {
                oldMethods.add(List.of(oldEncryptionMethod.trim()));
            }
        }
        return oldMethods;
    }

    public void loadGeyserSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection geyserSettings = config.getConfigurationSection("geyser-settings");
        if (!configFile.contains("geyser-settings")) {
            pluginLogger.warn("Configuration section geyser-settings not found!");
            configFile.createSection("geyser-settings");
            configFile.set("geyser-settings.geyser-prefix", ".");
            configFile.set("geyser-settings.geyser-nicknames", List.of("test99999"));
            save(plugin.getDataFilePath(), configFile, "config.yml");
            pluginLogger.info("Created section geyser-settings");
            geyserSettings = configFile.getConfigurationSection("geyser-settings");
        }
        this.geyserSettings = new GeyserSettings(
                geyserSettings.getString("geyser-prefix", "."),
                new HashSet<>(geyserSettings.getStringList("geyser-nicknames"))
        );
    }

    public void loadAdditionalChecks(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection blockingSettings = config.getConfigurationSection("blocking-settings");
        if (!configFile.contains("blocking-settings")) {
            pluginLogger.warn("Configuration section blocking-settings not found!");
            configFile.createSection("secure-settings");
            configFile.set("blocking-settings.block-item-drop", true);
            configFile.set("blocking-settings.block-item-pickup", true);
            configFile.set("blocking-settings.block-tab-complete", true);
            configFile.set("blocking-settings.block-damage", true);
            configFile.set("blocking-settings.block-damaging-entity", true);
            configFile.set("blocking-settings.block-inventory-open", false);
            configFile.set("blocking-settings.hide-on-entering", true);
            configFile.set("blocking-settings.hide-other-on-entering", true);
            configFile.set("blocking-settings.allow-orientation-change", false);
            save(plugin.getDataFilePath(), configFile, "config.yml");
            pluginLogger.info("Created section blocking-settings");
            blockingSettings = configFile.getConfigurationSection("blocking-settings");
        }
        this.blockingSettings = new BlockingSettings(
                blockingSettings.getBoolean("block-item-drop", true),
                blockingSettings.getBoolean("block-item-pickup", true),
                blockingSettings.getBoolean("block-tab-complete", true),
                blockingSettings.getBoolean("block-damage", true),
                blockingSettings.getBoolean("block-damaging-entity", true),
                blockingSettings.getBoolean("block-inventory-open", false),
                blockingSettings.getBoolean("hide-on-entering", true),
                blockingSettings.getBoolean("hide-other-on-entering", true),
                blockingSettings.getBoolean("allow-orientation-change", false)
        );
    }

    public void loadSessionSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection sessionSettings = config.getConfigurationSection("session-settings");
        if (!configFile.contains("session-settings")) {
            pluginLogger.warn("Configuration section session-settings not found!");
            configFile.createSection("session-settings");
            configFile.set("session-settings.session", true);
            configFile.set("session-settings.session-time-enabled", true);
            configFile.set("session-settings.session-time", 21600);
            save(plugin.getDataFilePath(), configFile, "config.yml");
            pluginLogger.info("Created section session-settings");
            sessionSettings = configFile.getConfigurationSection("session-settings");
        }
        this.sessionSettings = new SessionSettings(
                sessionSettings.getBoolean("session", true),
                sessionSettings.getBoolean("session-time-enabled", true),
                sessionSettings.getInt("session-time", 21600)
        );
    }

    public void loadPunishSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection punishSettings = config.getConfigurationSection("punish-settings");
        if (!configFile.contains("punish-settings")) {
            pluginLogger.warn("Configuration section punish-settings not found!");
            configFile.createSection("punish-settings");
            configFile.set("punish-settings.enable-attempts", true);
            configFile.set("punish-settings.max-attempts", 3);
            configFile.set("punish-settings.enable-time", true);
            configFile.set("punish-settings.time", 60);
            configFile.set("punish-settings.enable-rejoin", true);
            configFile.set("punish-settings.max-rejoins", 3);
            save(plugin.getDataFilePath(), configFile, "config.yml");
            pluginLogger.info("Created section punish-settings");
            punishSettings = configFile.getConfigurationSection("punish-settings");
        }
        this.punishSettings = new PunishSettings(
                punishSettings.getBoolean("enable-attempts", true),
                punishSettings.getInt("max-attempts", 3),
                punishSettings.getBoolean("enable-time", true),
                punishSettings.getInt("time", 60),
                punishSettings.getBoolean("enable-rejoin", true),
                punishSettings.getInt("max-rejoins", 3)
        );
    }

    public void loadSecureSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection secureSettings = config.getConfigurationSection("secure-settings");
        if (!configFile.contains("secure-settings")) {
            pluginLogger.warn("Configuration section secure-settings not found!");
            configFile.createSection("secure-settings");
            configFile.set("secure-settings.enable-op-whitelist", false);
            configFile.set("secure-settings.enable-notadmin-punish", false);
            configFile.set("secure-settings.enable-permission-blacklist", false);
            configFile.set("secure-settings.enable-ip-whitelist", false);
            configFile.set("secure-settings.only-console-usp", false);
            configFile.set("secure-settings.enable-excluded-players", false);
            save(plugin.getDataFilePath(), configFile, "config.yml");
            pluginLogger.info("Created section secure-settings");
            secureSettings = configFile.getConfigurationSection("secure-settings");
        }
        this.secureSettings = new SecureSettings(
                secureSettings.getBoolean("enable-op-whitelist", false),
                secureSettings.getBoolean("enable-notadmin-punish", false),
                secureSettings.getBoolean("enable-permission-blacklist", false),
                secureSettings.getBoolean("enable-ip-whitelist", false),
                secureSettings.getBoolean("only-console-usp", false),
                secureSettings.getBoolean("enable-excluded-players", false)
        );
    }

    public void loadApiSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection apiSettings = config.getConfigurationSection("api-settings");
        if (!configFile.contains("api-settings")) {
            pluginLogger.warn("Configuration section api-settings not found!");
            configFile.createSection("api-settings");
            configFile.set("api-settings.call-event-on-capture", false);
            configFile.set("api-settings.call-event-on-password-enter", false);
            configFile.set("api-settings.allowed-auth-api-calls-packages", Collections.emptyList());
            save(plugin.getDataFilePath(), configFile, "config.yml");
            pluginLogger.info("Created section api-settings");
            apiSettings = configFile.getConfigurationSection("api-settings");
        }
        this.apiSettings = new ApiSettings(
                apiSettings.getBoolean("call-event-on-capture", false),
                apiSettings.getBoolean("call-event-on-password-enter", false),
                apiSettings.getStringList("allowed-auth-api-calls-packages")
        );
    }

    public void loadMessageSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection messageSettings = config.getConfigurationSection("message-settings");
        if (!configFile.contains("message-settings")) {
            pluginLogger.warn("Configuration section message-settings not found!");
            configFile.createSection("message-settings");
            configFile.set("message-settings.send-titles", true);
            configFile.set("message-settings.enable-broadcasts", true);
            configFile.set("message-settings.enable-console-broadcasts", true);
            save(plugin.getDataFilePath(), configFile, "config.yml");
            pluginLogger.info("Created section message-settings");
            messageSettings = configFile.getConfigurationSection("message-settings");
        }
        this.messageSettings = new MessageSettings(
                messageSettings.getBoolean("send-titles", true),
                messageSettings.getBoolean("enable-broadcasts", true),
                messageSettings.getBoolean("enable-console-broadcasts", true)
        );
    }

    public void loadBossbarSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection bossbarSettings = config.getConfigurationSection("bossbar-settings");
        if (!configFile.contains("bossbar-settings")) {
            pluginLogger.warn("Configuration section bossbar-settings not found!");
            configFile.createSection("bossbar-settings");
            configFile.set("bossbar-settings.enable-bossbar", false);
            configFile.set("bossbar-settings.bar-color", "RED");
            configFile.set("bossbar-settings.bar-style", "SEGMENTED_12");
            save(plugin.getDataFilePath(), configFile, "config.yml");
            pluginLogger.info("Created section bossbar-settings");
            bossbarSettings = configFile.getConfigurationSection("bossbar-settings");
        }
        ConfigurationSection bossbar = plugin.getMessageFile().getConfigurationSection("bossbar");
        String bossbarMessage = getMessage(bossbar, "message");

        this.bossbarSettings = new BossbarSettings(
                bossbarSettings.getBoolean("enable-bossbar", true),
                bossbarSettings.getString("bar-color", "RED"),
                bossbarSettings.getString("bar-style", "SEGMENTED_12"),
                bossbarMessage
        );
    }

    public void loadSoundSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection soundSettings = config.getConfigurationSection("sound-settings");
        if (!configFile.contains("sound-settings")) {
            pluginLogger.warn("Configuration section sound-settings not found!");
            configFile.createSection("sound-settings");
            configFile.set("sound-settings.enable-sounds", false);
            configFile.set("sound-settings.on-capture", "ENTITY_ITEM_BREAK;1.0;1.0");
            configFile.set("sound-settings.on-pas-fail", "ENTITY_VILLAGER_NO;1.0;1.0");
            configFile.set("sound-settings.on-pas-correct", "ENTITY_PLAYER_LEVELUP;1.0;1.0");
            save(plugin.getDataFilePath(), configFile, "config.yml");
            pluginLogger.info("Created section sound-settings");
            soundSettings = configFile.getConfigurationSection("sound-settings");
        }
        this.soundSettings = new SoundSettings(
                soundSettings.getBoolean("enable-sounds"),
                soundSettings.getString("on-capture", "ENTITY_ITEM_BREAK;1.0;1.0").split(";"),
                soundSettings.getString("on-pas-fail", "ENTITY_VILLAGER_NO;1.0;1.0").split(";"),
                soundSettings.getString("on-pas-correct", "ENTITY_PLAYER_LEVELUP;1.0;1.0").split(";")
        );
    }

    public void loadEffects(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection effectSettings = config.getConfigurationSection("effect-settings");
        if (!configFile.contains("effect-settings")) {
            pluginLogger.warn("Configuration section effect-settings not found!");
            configFile.createSection("effect-settings");
            configFile.set("effect-settings.enable-effects", true);
            configFile.set("effect-settings.effects", List.of("BLINDNESS;3"));
            save(plugin.getDataFilePath(), configFile, "config.yml");
            pluginLogger.info("Created section effect-settings");
            effectSettings = configFile.getConfigurationSection("effect-settings");
        }
        this.effectSettings = new EffectSettings(
                effectSettings.getBoolean("enable-effects", true),
                effectSettings.getStringList("effects")
        );
    }

    public void loadLoggingSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection loggingSettings = config.getConfigurationSection("logging-settings");
        if (!configFile.contains("logging-settings")) {
            pluginLogger.warn("Configuration section logging-settings not found!");
            configFile.createSection("logging-settings");
            configFile.set("logging-settings.logging-pas", true);
            configFile.set("logging-settings.logging-join", true);
            configFile.set("logging-settings.logging-enable-disable", true);
            configFile.set("logging-settings.logging-command-execution", true);
            save(plugin.getDataFilePath(), configFile, "config.yml");
            pluginLogger.info("Created section logging-settings");
            loggingSettings = configFile.getConfigurationSection("logging-settings");
        }
        this.loggingSettings = new LoggingSettings(
                loggingSettings.getBoolean("logging-pas", true),
                loggingSettings.getBoolean("logging-join", true),
                loggingSettings.getBoolean("logging-enable-disable", true),
                loggingSettings.getBoolean("logging-command-execution", true)
        );
    }

    public void loadFailCommands(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection commands = config.getConfigurationSection("commands");
        if (!configFile.contains("commands")) {
            pluginLogger.warn("Configuration section commands not found!");
            configFile.createSection("commands");
            configFile.set("commands.not-in-config", Collections.emptyList());
            configFile.set("commands.not-in-opwhitelist", Collections.emptyList());
            configFile.set("commands.have-blacklisted-perm", Collections.emptyList());
            configFile.set("commands.not-admin-ip", Collections.emptyList());
            configFile.set("commands.failed-pass", Collections.emptyList());
            configFile.set("commands.failed-time", Collections.emptyList());
            configFile.set("commands.failed-rejoin", Collections.emptyList());
            save(plugin.getDataFilePath(), configFile, "config.yml");
            pluginLogger.info("Created section main-settings");
            commands = configFile.getConfigurationSection("commands");
        }
        this.commands = new Commands(
                commands.getStringList("not-in-config"),
                commands.getStringList("not-in-opwhitelist"),
                commands.getStringList("have-blacklisted-perm"),
                commands.getStringList("not-admin-ip"),
                commands.getStringList("failed-pass"),
                commands.getStringList("failed-time"),
                commands.getStringList("failed-rejoin")
        );
    }

    public void loadAccessData(FileConfiguration config) {
        Set<String> perms = new HashSet<>(config.getStringList("permissions"));
        List<String> allowedCommands = config.getStringList("allowed-commands");

        ConfigurationSection secureSettings = config.getConfigurationSection("secure-settings");

        List<String> opWhitelist = new ArrayList<>();
        Set<String> blacklistedPerms = new HashSet<>();
        Map<String, List<String>> ipWhitelist = new HashMap<>();

        if (secureSettings.getBoolean("enable-op-whitelist")) {
            opWhitelist = config.getStringList("op-whitelist");
        }
        if (secureSettings.getBoolean("enable-permission-blacklist")) {
            blacklistedPerms = new HashSet<>(config.getStringList("blacklisted-perms"));
        }
        if (secureSettings.getBoolean("enable-ip-whitelist")) {
            for (String ipwlPlayer : config.getConfigurationSection("ip-whitelist").getKeys(false)) {
                List<String> ips = config.getStringList("ip-whitelist." + ipwlPlayer);
                ipWhitelist.put(ipwlPlayer, ips);
            }
        }
        this.accessData = new AccessData(perms, allowedCommands, opWhitelist, blacklistedPerms, ipWhitelist);
    }

    public void setupExcluded(FileConfiguration config) {
        if (config.getBoolean("secure-settings.enable-excluded-players")) {
            ConfigurationSection excludedPlayers = config.getConfigurationSection("excluded-players");
            this.excludedPlayers = new ExcludedPlayers(
                    excludedPlayers.getStringList("admin-pass"),
                    excludedPlayers.getStringList("op-whitelist"),
                    excludedPlayers.getStringList("ip-whitelist"),
                    excludedPlayers.getStringList("blacklisted-perms")
            );
        }
    }

    public void loadUspMessages(FileConfiguration message) {
        ConfigurationSection uspmsg = message.getConfigurationSection("uspmsg");
        this.uspMessages = new UspMessages(
                getMessage(uspmsg, "consoleonly"),
                getMessage(uspmsg, "reloaded"),
                getMessage(uspmsg, "rebooted"),
                getMessage(uspmsg, "playernotfound"),
                getMessage(uspmsg, "alreadyinconfig"),
                getMessage(uspmsg, "playeronly"),
                getMessage(uspmsg, "logout"),
                getMessage(uspmsg, "notinconfig"),
                getMessage(uspmsg, "playeradded"),
                getMessage(uspmsg, "playerremoved"),
                getMessage(uspmsg, "ipadded"),
                getMessage(uspmsg, "setpassusage"),
                getMessage(uspmsg, "addopusage"),
                getMessage(uspmsg, "remopusage"),
                getMessage(uspmsg, "ipremoved"),
                getMessage(uspmsg, "remipusage"),
                getMessage(uspmsg, "addipusage"),
                getMessage(uspmsg, "rempassusage"),
                getMessage(uspmsg, "usage"),
                getMessage(uspmsg, "usage-logout"),
                getMessage(uspmsg, "usage-reload"),
                getMessage(uspmsg, "usage-reboot"),
                getMessage(uspmsg, "usage-encrypt"),
                getMessage(uspmsg, "usage-setpass"),
                getMessage(uspmsg, "usage-rempass"),
                getMessage(uspmsg, "usage-addop"),
                getMessage(uspmsg, "usage-remop"),
                getMessage(uspmsg, "usage-addip"),
                getMessage(uspmsg, "usage-remip"),
                getMessage(uspmsg, "otherdisabled")
        );
    }

    public void loadMsgMessages(FileConfiguration message) {
        ConfigurationSection msg = message.getConfigurationSection("msg");
        this.messages = new Messages(
                getMessage(msg, "message"),
                getMessage(msg, "incorrect"),
                getMessage(msg, "correct"),
                getMessage(msg, "noneed"),
                getMessage(msg, "cantbenull"),
                getMessage(msg, "playeronly")
        );
    }

    public void loadTitleMessages(FileConfiguration message) {
        ConfigurationSection titles = message.getConfigurationSection("titles");
        this.titles = new Titles(
                getMessage(titles, "message").split(";"),
                getMessage(titles, "incorrect").split(";"),
                getMessage(titles, "correct").split(";")
        );
    }

    public void loadBroadcastMessages(FileConfiguration message) {
        ConfigurationSection broadcasts = message.getConfigurationSection("broadcasts");
        this.broadcasts = new Broadcasts(
                getMessage(broadcasts, "failed"),
                getMessage(broadcasts, "passed"),
                getMessage(broadcasts, "joined"),
                getMessage(broadcasts, "captured")
        );
    }

    public String getMessage(ConfigurationSection section, String key) {
        return Utils.colorize(section.getString(key, "&4&lERROR&r: " + key + " does not exist!").replace("%prefix%", mainSettings.prefix()), serializer);
    }

    public FileConfiguration getFile(String path, String fileName) {
        File file = new File(path, fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public void save(String path, FileConfiguration config, String fileName) {
        plugin.getRunner().runAsync(() -> {
            try {
                config.save(new File(path, fileName));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }
}