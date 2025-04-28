package ru.overwrite.protect.bukkit.configuration;

import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.configuration.data.*;
import ru.overwrite.protect.bukkit.utils.Utils;
import ru.overwrite.protect.bukkit.utils.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Getter
public final class Config {

    @Getter(AccessLevel.NONE)
    private final ServerProtectorManager plugin;
    @Getter(AccessLevel.NONE)
    private final Logger pluginLogger;

    public Config(ServerProtectorManager plugin) {
        this.plugin = plugin;
        this.pluginLogger = plugin.getPluginLogger();
    }

    private Map<String, String> perPlayerPasswords;

    public void setupPasswords(FileConfiguration dataFile) {
        ConfigurationSection data = dataFile.getConfigurationSection("data");
        boolean shouldSave = false;
        Map<String, String> perPlayerPasswords = new HashMap<>();
        for (String nick : data.getKeys(false)) {
            String playerNick = (this.geyserSettings.prefix() != null &&
                    !this.geyserSettings.prefix().isBlank() &&
                    this.geyserSettings.nicknames().contains(nick))
                    ? this.geyserSettings.prefix() + nick
                    : nick;
            if (!this.encryptionSettings.enableEncryption()) {
                perPlayerPasswords.put(playerNick, data.getString(nick + ".pass"));
                continue;
            }
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
        this.perPlayerPasswords = Map.copyOf(perPlayerPasswords);
        if (shouldSave) {
            save(plugin.getDataFilePath(), dataFile, plugin.getDataFileName());
        }
    }

    private MainSettings mainSettings;

    public void loadMainSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection mainSettings = config.getConfigurationSection("main-settings");
        if (!configFile.contains("main-settings")) {
            pluginLogger.warn("Configuration section main-settings not found!");
            ConfigurationSection section = configFile.createSection("main-settings");
            section.set("serializer", "LEGACY");
            section.set("prefix", "[UltimateServerProtector]");
            section.set("pas-command", "pas");
            section.set("use-command", true);
            section.set("enable-admin-commands", false);
            section.set("check-interval", 40);
            section.set("papi-support", false);
            section.set("suppress-api-warnings", false);
            save(plugin.getDataFilePath(), configFile, "config.yml");
            pluginLogger.info("Created section main-settings");
            mainSettings = configFile.getConfigurationSection("main-settings");
        }
        this.mainSettings = new MainSettings(
                mainSettings.getString("prefix", "[UltimateServerProtector]"),
                mainSettings.getString("pas-command", "pas"),
                mainSettings.getBoolean("use-command", true),
                mainSettings.getBoolean("enable-admin-commands", false),
                mainSettings.getLong("check-interval", 40),
                mainSettings.getBoolean("papi-support", false),
                mainSettings.getBoolean("suppress-api-warnings", false)
        );
    }

    private EncryptionSettings encryptionSettings;

    public void loadEncryptionSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection encryptionSettings = config.getConfigurationSection("encryption-settings");
        if (!configFile.contains("encryption-settings")) {
            pluginLogger.warn("Configuration section encryption-settings not found!");
            ConfigurationSection section = configFile.createSection("encryption-settings");
            section.set("enable-encryption", false);
            section.set("encrypt-method", "");
            section.set("old-encrypt-methods", List.of());
            section.set("salt-length", 24);
            section.set("auto-encrypt-passwords", true);
            save(plugin.getDataFilePath(), configFile, "config.yml");
            pluginLogger.info("Created section encryption-settings");
            encryptionSettings = configFile.getConfigurationSection("encryption-settings");
        }
        this.encryptionSettings = new EncryptionSettings(
                encryptionSettings.getBoolean("enable-encryption", false),
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

    private GeyserSettings geyserSettings;

    public void loadGeyserSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection geyserSettings = config.getConfigurationSection("geyser-settings");
        if (!configFile.contains("geyser-settings")) {
            pluginLogger.warn("Configuration section geyser-settings not found!");
            ConfigurationSection section = configFile.createSection("geyser-settings");
            section.set("geyser-prefix", ".");
            section.set("geyser-nicknames", List.of("test99999"));
            save(plugin.getDataFilePath(), configFile, "config.yml");
            pluginLogger.info("Created section geyser-settings");
            geyserSettings = configFile.getConfigurationSection("geyser-settings");
        }
        this.geyserSettings = new GeyserSettings(
                geyserSettings.getString("geyser-prefix", "."),
                new HashSet<>(geyserSettings.getStringList("geyser-nicknames"))
        );
    }

    private BlockingSettings blockingSettings;

    public void loadAdditionalChecks(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection blockingSettings = config.getConfigurationSection("blocking-settings");
        if (!configFile.contains("blocking-settings")) {
            pluginLogger.warn("Configuration section blocking-settings not found!");
            ConfigurationSection section = configFile.createSection("blocking-settings");
            section.set("block-item-drop", true);
            section.set("block-item-pickup", true);
            section.set("block-tab-complete", true);
            section.set("block-damage", true);
            section.set("block-damaging-entity", true);
            section.set("block-inventory-open", false);
            section.set("hide-on-entering", true);
            section.set("hide-other-on-entering", true);
            section.set("allow-orientation-change", false);
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

    private SessionSettings sessionSettings;

    public void loadSessionSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection sessionSettings = config.getConfigurationSection("session-settings");
        if (!configFile.contains("session-settings")) {
            pluginLogger.warn("Configuration section session-settings not found!");
            ConfigurationSection section = configFile.createSection("session-settings");
            section.set("session", true);
            section.set("session-time-enabled", true);
            section.set("session-time", 21600);
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

    private PunishSettings punishSettings;

    public void loadPunishSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection punishSettings = config.getConfigurationSection("punish-settings");
        if (!configFile.contains("punish-settings")) {
            pluginLogger.warn("Configuration section punish-settings not found!");
            ConfigurationSection section = configFile.createSection("punish-settings");
            section.set("enable-attempts", true);
            section.set("max-attempts", 3);
            section.set("enable-time", true);
            section.set("time", 60);
            section.set("enable-rejoin", true);
            section.set("max-rejoins", 3);
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

    private SecureSettings secureSettings;

    public void loadSecureSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection secureSettings = config.getConfigurationSection("secure-settings");
        if (!configFile.contains("secure-settings")) {
            pluginLogger.warn("Configuration section secure-settings not found!");
            ConfigurationSection section = configFile.createSection("secure-settings");
            section.set("enable-op-whitelist", false);
            section.set("enable-notadmin-punish", false);
            section.set("enable-permission-blacklist", false);
            section.set("enable-ip-whitelist", false);
            section.set("only-console-usp", false);
            section.set("enable-excluded-players", false);
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

    private ApiSettings apiSettings;

    public void loadApiSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection apiSettings = config.getConfigurationSection("api-settings");
        if (!configFile.contains("api-settings")) {
            pluginLogger.warn("Configuration section api-settings not found!");
            ConfigurationSection section = configFile.createSection("api-settings");
            section.set("allow-cancel-capture-event", false);
            section.set("call-event-on-password-enter", false);
            section.set("allowed-auth-api-calls-packages", List.of());
            save(plugin.getDataFilePath(), configFile, "config.yml");
            pluginLogger.info("Created section api-settings");
            apiSettings = configFile.getConfigurationSection("api-settings");
        }
        this.apiSettings = new ApiSettings(
                apiSettings.getBoolean("allow-cancel-capture-event", false),
                apiSettings.getBoolean("call-event-on-password-enter", false),
                apiSettings.getStringList("allowed-auth-api-calls-packages")
        );
    }

    private MessageSettings messageSettings;

    public void loadMessageSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection messageSettings = config.getConfigurationSection("message-settings");
        if (!configFile.contains("message-settings")) {
            pluginLogger.warn("Configuration section message-settings not found!");
            ConfigurationSection section = configFile.createSection("message-settings");
            section.set("send-titles", true);
            section.set("enable-broadcasts", true);
            section.set("enable-console-broadcasts", true);
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

    private BossbarSettings bossbarSettings;

    public void loadBossbarSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection bossbarSettings = config.getConfigurationSection("bossbar-settings");
        if (!configFile.contains("bossbar-settings")) {
            pluginLogger.warn("Configuration section bossbar-settings not found!");
            ConfigurationSection section = configFile.createSection("bossbar-settings");
            section.set("enable-bossbar", false);
            section.set("bar-color", "RED");
            section.set("bar-style", "SEGMENTED_12");
            save(plugin.getDataFilePath(), configFile, "config.yml");
            pluginLogger.info("Created section bossbar-settings");
            bossbarSettings = configFile.getConfigurationSection("bossbar-settings");
        }
        ConfigurationSection bossbar = plugin.getMessageFile().getConfigurationSection("bossbar");
        String bossbarMessage = getMessage(bossbar, "message");

        this.bossbarSettings = new BossbarSettings(
                bossbarSettings.getBoolean("enable-bossbar", true),
                BarColor.valueOf(bossbarSettings.getString("bar-color", "RED")),
                BarStyle.valueOf(bossbarSettings.getString("bar-style", "SEGMENTED_12")),
                bossbarMessage
        );
    }

    private SoundSettings soundSettings;

    public void loadSoundSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection soundSettings = config.getConfigurationSection("sound-settings");
        if (!configFile.contains("sound-settings")) {
            pluginLogger.warn("Configuration section sound-settings not found!");
            ConfigurationSection section = configFile.createSection("sound-settings");
            section.set("enable-sounds", false);
            section.set("on-capture", "ENTITY_ITEM_BREAK;1.0;1.0");
            section.set("on-pas-fail", "ENTITY_VILLAGER_NO;1.0;1.0");
            section.set("on-pas-correct", "ENTITY_PLAYER_LEVELUP;1.0;1.0");
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

    private EffectSettings effectSettings;

    public void loadEffects(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection effectSettings = config.getConfigurationSection("effect-settings");
        if (!configFile.contains("effect-settings")) {
            pluginLogger.warn("Configuration section effect-settings not found!");
            ConfigurationSection section = configFile.createSection("effect-settings");
            section.set("enable-effects", true);
            section.set("effects", List.of("BLINDNESS;3"));
            save(plugin.getDataFilePath(), configFile, "config.yml");
            pluginLogger.info("Created section effect-settings");
            effectSettings = configFile.getConfigurationSection("effect-settings");
        }
        List<PotionEffect> effectList = getEffectList(effectSettings.getStringList("effects"));
        this.effectSettings = new EffectSettings(
                effectSettings.getBoolean("enable-effects", true),
                effectList
        );
    }

    private List<PotionEffect> getEffectList(List<String> effects) {
        if (effects.isEmpty()) {
            return List.of();
        }
        List<PotionEffect> effectList = new ArrayList<>(effects.size());
        for (String effect : effects) {
            int separatorIndex = effect.indexOf(';');
            String effectName = separatorIndex > 0 ? effect.substring(0, separatorIndex) : effect;
            PotionEffectType type = PotionEffectType.getByName(effectName.toUpperCase());
            int level = separatorIndex > 0 ? Integer.parseInt(effect.substring(separatorIndex + 1)) - 1 : 0;
            effectList.add(new PotionEffect(type, Integer.MAX_VALUE, level));
        }
        return effectList;
    }

    private LoggingSettings loggingSettings;

    public void loadLoggingSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection loggingSettings = config.getConfigurationSection("logging-settings");
        if (!configFile.contains("logging-settings")) {
            pluginLogger.warn("Configuration section logging-settings not found!");
            ConfigurationSection section = configFile.createSection("logging-settings");
            section.set("logging-pas", true);
            section.set("logging-join", true);
            section.set("logging-enable-disable", true);
            section.set("logging-command-execution", true);
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

    private Commands commands;

    public void loadFailCommands(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection commands = config.getConfigurationSection("commands");
        if (!configFile.contains("commands")) {
            pluginLogger.warn("Configuration section commands not found!");
            ConfigurationSection section = configFile.createSection("commands");
            section.set("not-in-config", List.of());
            section.set("not-in-opwhitelist", List.of());
            section.set("have-blacklisted-perm", List.of());
            section.set("not-admin-ip", List.of());
            section.set("failed-pass", List.of());
            section.set("failed-time", List.of());
            section.set("failed-rejoin", List.of());
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

    private AccessData accessData;

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
            blacklistedPerms.addAll(config.getStringList("blacklisted-perms"));
        }
        if (secureSettings.getBoolean("enable-ip-whitelist")) {
            for (String ipwlPlayer : config.getConfigurationSection("ip-whitelist").getKeys(false)) {
                List<String> ips = config.getStringList("ip-whitelist." + ipwlPlayer);
                ipWhitelist.put(ipwlPlayer, ips);
            }
        }
        this.accessData = new AccessData(perms, allowedCommands, opWhitelist, blacklistedPerms, ipWhitelist);
    }

    private ExcludedPlayers excludedPlayers;

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

    private UspMessages uspMessages;

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

    private Messages messages;

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

    private Titles titles;

    public void loadTitleMessages(FileConfiguration message) {
        ConfigurationSection titles = message.getConfigurationSection("titles");
        this.titles = new Titles(
                getMessage(titles, "message").split(";"),
                getMessage(titles, "incorrect").split(";"),
                getMessage(titles, "correct").split(";")
        );
    }

    private Broadcasts broadcasts;

    public void loadBroadcastMessages(FileConfiguration message) {
        ConfigurationSection broadcasts = message.getConfigurationSection("broadcasts");
        this.broadcasts = new Broadcasts(
                getMessage(broadcasts, "failed"),
                getMessage(broadcasts, "passed"),
                getMessage(broadcasts, "joined"),
                getMessage(broadcasts, "captured"),
                getMessage(broadcasts, "disabled")
        );
    }

    private LogMessages logMessages;

    public void loadLogFormats(FileConfiguration message) {
        ConfigurationSection logMessages = message.getConfigurationSection("log-format");
        this.logMessages = new LogMessages(
                getMessage(logMessages, "enabled"),
                getMessage(logMessages, "disabled"),
                getMessage(logMessages, "failed"),
                getMessage(logMessages, "passed"),
                getMessage(logMessages, "joined"),
                getMessage(logMessages, "captured"),
                getMessage(logMessages, "command")
        );
    }

    private SystemMessages systemMessages;

    public void loadSystemMessages(FileConfiguration message) {
        ConfigurationSection system = message.getConfigurationSection("system");
        this.systemMessages = new SystemMessages(
                getMessage(system, "baseline-warn"),
                getMessage(system, "baseline-default"),
                getMessage(system, "paper-1"),
                getMessage(system, "paper-2"),
                getMessage(system, "bungeecord-1"),
                getMessage(system, "bungeecord-2"),
                getMessage(system, "bungeecord-3"),
                getMessage(system, "update-latest"),
                getMessage(system, "update-success-1"),
                getMessage(system, "update-success-2"),
                getMessage(system, "update-outdated-1"),
                getMessage(system, "update-outdated-2"),
                getMessage(system, "update-outdated-3")
        );
    }

    public String getMessage(ConfigurationSection section, String key) {
        if (section == null) {
            return Utils.COLORIZER.colorize("&4&lERROR&r: " + key + " does not exist!");
        }
        return Utils.COLORIZER.colorize(section.getString(key, "&4&lERROR&r: " + key + " does not exist!").replace("%prefix%", mainSettings.prefix()));
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
