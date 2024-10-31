package ru.overwrite.protect.bukkit.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.overwrite.protect.bukkit.utils.logging.Logger;
import ru.overwrite.protect.bukkit.ServerProtectorManager;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Config {

    private final ServerProtectorManager plugin;
    private final Logger logger;

    public Config(ServerProtectorManager plugin) {
        this.plugin = plugin;
        this.logger = plugin.getPluginLogger();
    }

    public String serializer;

    public Set<String> perms,
            blacklisted_perms,
            geyser_names;

    public Map<String, List<String>> ip_whitelist;

    public Map<String, String> per_player_passwords;

    public List<String> encryption_settings_encrypt_methods;

    public List<String> effect_settings_effects;

    public List<String> allowed_commands;

    public List<String> op_whitelist;

    public List<String> excluded_admin_pass,
            excluded_op_whitelist,
            excluded_ip_whitelist,
            excluded_blacklisted_perms;

    public List<String> commands_not_in_config,
            commands_not_in_opwhitelist,
            commands_have_blacklisted_perm,
            commands_not_admin_ip,
            commands_failed_pass,
            commands_failed_time,
            commands_failed_rejoin;

    public List<List<String>> encryption_settings_old_encrypt_methods;

    public String[] titles_message, titles_incorrect, titles_correct;
    public String[] sound_settings_on_capture, sound_settings_on_pas_fail, sound_settings_on_pas_correct;

    public String geyser_prefix;

    public String uspmsg_consoleonly,
            uspmsg_reloaded,
            uspmsg_rebooted,
            uspmsg_playernotfound,
            uspmsg_alreadyinconfig,
            uspmsg_playeronly,
            uspmsg_logout,
            uspmsg_notinconfig,
            uspmsg_playeradded,
            uspmsg_playerremoved,
            uspmsg_ipadded,
            uspmsg_setpassusage,
            uspmsg_addopusage,
            uspmsg_remopusage,
            uspmsg_ipremoved,
            uspmsg_remipusage,
            uspmsg_addipusage,
            uspmsg_rempassusage,
            uspmsg_usage,
            uspmsg_usage_logout,
            uspmsg_usage_reload,
            uspmsg_usage_reboot,
            uspmsg_usage_encrypt,
            uspmsg_usage_setpass,
            uspmsg_usage_rempass,
            uspmsg_usage_addop,
            uspmsg_usage_remop,
            uspmsg_usage_addip,
            uspmsg_usage_remip,
            uspmsg_otherdisabled;

    public String msg_message,
            msg_incorrect,
            msg_correct,
            msg_noneed,
            msg_cantbenull,
            msg_playeronly;

    public String broadcasts_failed,
            broadcasts_passed,
            broadcasts_joined,
            broadcasts_captured;

    public String bossbar_message,
            bossbar_settings_bar_color,
            bossbar_settings_bar_style;

    public String main_settings_prefix,
            main_settings_pas_command;

    public boolean encryption_settings_enable_encryption,
            encryption_settings_auto_encrypt_passwords;

    public boolean blocking_settings_block_item_drop,
            blocking_settings_block_item_pickup,
            blocking_settings_block_tab_complete,
            blocking_settings_block_damage,
            blocking_settings_damaging_entity,
            blocking_settings_block_inventory_open,
            blocking_settings_hide_on_entering,
            blocking_settings_hide_other_on_entering,
            blocking_settings_allow_orientation_change;

    public boolean main_settings_papi_support,
            main_settings_use_command,
            main_settings_enable_admin_commands;

    public boolean punish_settings_enable_attempts,
            punish_settings_enable_time,
            punish_settings_enable_rejoin,
            bossbar_settings_enable_bossbar;

    public boolean secure_settings_enable_op_whitelist,
            secure_settings_enable_notadmin_punish,
            secure_settings_enable_permission_blacklist,
            secure_settings_enable_ip_whitelist,
            secure_settings_only_console_usp,
            secure_settings_enable_excluded_players,
            secure_settings_call_event_on_password_enter;

    public boolean session_settings_session,
            session_settings_session_time_enabled;

    public boolean message_settings_send_title,
            message_settings_enable_broadcasts,
            message_settings_enable_console_broadcasts;

    public boolean sound_settings_enable_sounds;

    public boolean effect_settings_enable_effects;

    public boolean logging_settings_logging_pas,
            logging_settings_logging_join,
            logging_settings_logging_enable_disable,
            logging_settings_logging_command_execution;

    public int encryption_settings_salt_length,
            punish_settings_max_attempts, punish_settings_time, punish_settings_max_rejoins,
            session_settings_session_time;

    public long main_settings_check_interval;

    public void setupPasswords(FileConfiguration dataFile) {
        per_player_passwords = new ConcurrentHashMap<>();
        ConfigurationSection data = dataFile.getConfigurationSection("data");
        boolean shouldSave = false;
        for (String nick : data.getKeys(false)) {
            String playerNick = nick;
            if (geyser_prefix != null && !geyser_prefix.isBlank() && geyser_names.contains(nick)) {
                playerNick = geyser_prefix + nick;
            }
            if (!encryption_settings_enable_encryption) {
                per_player_passwords.put(playerNick, data.getString(nick + ".pass"));
            } else {
                if (encryption_settings_auto_encrypt_passwords) {
                    if (data.getString(nick + ".pass") != null) {
                        String encryptedPas = Utils.encryptPassword(data.getString(nick + ".pass"), Utils.generateSalt(encryption_settings_salt_length), encryption_settings_encrypt_methods);
                        dataFile.set("data." + nick + ".encrypted-pass", encryptedPas);
                        dataFile.set("data." + nick + ".pass", null);
                        shouldSave = true;
                        plugin.dataFile = dataFile;
                    }
                }
                per_player_passwords.put(playerNick, data.getString(nick + ".encrypted-pass"));
            }
        }
        if (shouldSave) {
            save(plugin.path, dataFile, plugin.dataFileName);
        }
    }

    public void loadMainSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection mainSettings = config.getConfigurationSection("main-settings");
        if (!configFile.contains("main-settings")) {
            logger.warn("Configuration section main-settings not found!");
            configFile.createSection("main-settings");
            configFile.set("main-settings.serializer", "LEGACY");
            configFile.set("main-settings.prefix", "[UltimateServerProtector]");
            configFile.set("main-settings.pas-command", "pas");
            configFile.set("main-settings.use-command", true);
            configFile.set("main-settings.enable-admin-commands", false);
            configFile.set("main-settings.check-interval", 40);
            configFile.set("main-settings.papi-support", false);
            save(plugin.path, configFile, "config.yml");
            logger.info("Created section main-settings");
        }
        serializer = mainSettings.getString("serializer", "LEGACY").toUpperCase();
        main_settings_prefix = mainSettings.getString("prefix", "[UltimateServerProtector]");
        main_settings_pas_command = mainSettings.getString("pas-command", "pas");
        main_settings_use_command = mainSettings.getBoolean("use-command", true);
        main_settings_enable_admin_commands = mainSettings.getBoolean("enable-admin-commands", false);
        main_settings_check_interval = mainSettings.getLong("check-interval", 40);
        main_settings_papi_support = mainSettings.getBoolean("papi-support", false);
    }

    public void loadEncryptionSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection encryptionSettings = config.getConfigurationSection("encryption-settings");
        if (!configFile.contains("encryption-settings")) {
            logger.warn("Configuration section encryption-settings not found!");
            configFile.createSection("encryption-settings");
            configFile.set("encryption-settings.encrypt-method", "");
            configFile.set("encryption-settings.salt-length", 24);
            configFile.set("encryption-settings.auto-encrypt-passwords", true);
            configFile.set("encryption-settings.old-encrypt-methods", Collections.emptyList());
            configFile.set("encryption-settings.encrypt-method", "");
            save(plugin.path, configFile, "config.yml");
            logger.info("Created section encryption-settings");
        }
        encryption_settings_enable_encryption = encryptionSettings.getBoolean("enable-encryption", false);
        String encryptionMethod = encryptionSettings.getString("encrypt-method", "").trim();
        encryption_settings_encrypt_methods = encryptionMethod.contains(";")
                ? List.of(encryptionMethod.split(";"))
                : List.of(encryptionMethod);
        encryption_settings_old_encrypt_methods = new ArrayList<>();
        encryption_settings_salt_length = encryptionSettings.getInt("salt-length", 24);
        encryption_settings_auto_encrypt_passwords = encryptionSettings.getBoolean("auto-encrypt-passwords", true);
        if (!encryption_settings_enable_encryption) {
            return;
        }
        List<String> oldEncryptionMethods = encryptionSettings.getStringList("old-encrypt-methods");
        for (String oldEncryptionMethod : oldEncryptionMethods) {
            if (oldEncryptionMethod.contains(";")) {
                encryption_settings_old_encrypt_methods.add(List.of(oldEncryptionMethod.trim().split(";")));
            } else {
                encryption_settings_old_encrypt_methods.add(List.of(oldEncryptionMethod.trim()));
            }
        }
    }

    public void loadGeyserSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection geyserSettings = config.getConfigurationSection("geyser-settings");
        if (!configFile.contains("geyser-settings")) {
            logger.warn("Configuration section geyser-settings not found!");
            configFile.createSection("geyser-settings");
            configFile.set("geyser-settings.geyser-prefix", ".");
            configFile.set("geyser-settings.geyser-nicknames", List.of("test99999"));
            save(plugin.path, configFile, "config.yml");
            logger.info("Created section geyser-settings");
        }
        geyser_prefix = geyserSettings.getString("geyser-prefix", ".");
        geyser_names = new HashSet<>(geyserSettings.getStringList("geyser-nicknames"));
    }

    public void loadAdditionalChecks(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection blockingSettings = config.getConfigurationSection("blocking-settings");
        if (!configFile.contains("blocking-settings")) {
            logger.warn("Configuration section blocking-settings not found!");
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
            save(plugin.path, configFile, "config.yml");
            logger.info("Created section blocking-settings");
        }
        blocking_settings_block_item_drop = blockingSettings.getBoolean("block-item-drop", true);
        blocking_settings_block_item_pickup = blockingSettings.getBoolean("block-item-pickup", true);
        blocking_settings_block_tab_complete = blockingSettings.getBoolean("block-tab-complete", true);
        blocking_settings_block_damage = blockingSettings.getBoolean("block-damage", true);
        blocking_settings_damaging_entity = blockingSettings.getBoolean("block-damaging-entity", true);
        blocking_settings_block_inventory_open = blockingSettings.getBoolean("block-inventory-open", false);
        blocking_settings_hide_on_entering = blockingSettings.getBoolean("hide-on-entering", true);
        blocking_settings_hide_other_on_entering = blockingSettings.getBoolean("hide-other-on-entering", true);
        blocking_settings_allow_orientation_change = blockingSettings.getBoolean("allow-orientation-change", false);
    }

    public void loadSessionSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection sessionSettings = config.getConfigurationSection("session-settings");
        if (!configFile.contains("session-settings")) {
            logger.warn("Configuration section session-settings not found!");
            configFile.createSection("session-settings");
            configFile.set("session-settings.session", true);
            configFile.set("session-settings.session-time-enabled", true);
            configFile.set("session-settings.session-time", 21600);
            save(plugin.path, configFile, "config.yml");
            logger.info("Created section session-settings");
        }
        session_settings_session = sessionSettings.getBoolean("session", true);
        session_settings_session_time_enabled = sessionSettings.getBoolean("session-time-enabled", true);
        session_settings_session_time = sessionSettings.getInt("session-time", 21600);
    }

    public void loadPunishSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection punishSettings = config.getConfigurationSection("punish-settings");
        if (!configFile.contains("punish-settings")) {
            logger.warn("Configuration section punish-settings not found!");
            configFile.createSection("punish-settings");
            configFile.set("punish-settings.enable-attempts", true);
            configFile.set("punish-settings.max-attempts", 3);
            configFile.set("punish-settings.enable-time", true);
            configFile.set("punish-settings.time", 60);
            configFile.set("punish-settings.enable-rejoin", true);
            configFile.set("punish-settings.max-rejoins", 3);
            save(plugin.path, configFile, "config.yml");
            logger.info("Created section punish-settings");
        }
        punish_settings_enable_attempts = punishSettings.getBoolean("enable-attempts", true);
        punish_settings_max_attempts = punishSettings.getInt("max-attempts", 3);
        punish_settings_enable_time = punishSettings.getBoolean("enable-time", true);
        punish_settings_time = punishSettings.getInt("time", 60);
        punish_settings_enable_rejoin = punishSettings.getBoolean("enable-rejoin", true);
        punish_settings_max_rejoins = punishSettings.getInt("max-rejoins", 3);
    }

    public void loadSecureSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection secureSettings = config.getConfigurationSection("secure-settings");
        if (!configFile.contains("secure-settings")) {
            logger.warn("Configuration section secure-settings not found!");
            configFile.createSection("secure-settings");
            configFile.set("secure-settings.enable-op-whitelist", false);
            configFile.set("secure-settings.enable-notadmin-punish", false);
            configFile.set("secure-settings.enable-permission-blacklist", false);
            configFile.set("secure-settings.enable-ip-whitelist", false);
            configFile.set("secure-settings.only-console-usp", false);
            configFile.set("secure-settings.enable-excluded-players", false);
            configFile.set("secure-settings.call-event-on-password-enter", false);
            save(plugin.path, configFile, "config.yml");
            logger.info("Created section secure-settings");
        }
        secure_settings_enable_op_whitelist = secureSettings.getBoolean("enable-op-whitelist", false);
        secure_settings_enable_notadmin_punish = secureSettings.getBoolean("enable-notadmin-punish", false);
        secure_settings_enable_permission_blacklist = secureSettings.getBoolean("enable-permission-blacklist", false);
        secure_settings_enable_ip_whitelist = secureSettings.getBoolean("enable-ip-whitelist", false);
        secure_settings_only_console_usp = secureSettings.getBoolean("only-console-usp", false);
        secure_settings_enable_excluded_players = secureSettings.getBoolean("enable-excluded-players", false);
        secure_settings_call_event_on_password_enter = secureSettings.getBoolean("call-event-on-password-enter", false);
    }

    public void loadMessageSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection messageSettings = config.getConfigurationSection("message-settings");
        if (!configFile.contains("message-settings")) {
            logger.warn("Configuration section message-settings not found!");
            configFile.createSection("message-settings");
            configFile.set("message-settings.send-titles", true);
            configFile.set("message-settings.enable-broadcasts", true);
            configFile.set("message-settings.enable-console-broadcasts", true);
            save(plugin.path, configFile, "config.yml");
            logger.info("Created section message-settings");
        }
        message_settings_send_title = messageSettings.getBoolean("send-titles", true);
        message_settings_enable_broadcasts = messageSettings.getBoolean("enable-broadcasts", true);
        message_settings_enable_console_broadcasts = messageSettings.getBoolean("enable-console-broadcasts", true);
    }

    public void loadBossbarSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection bossbarSettings = config.getConfigurationSection("bossbar-settings");
        if (!configFile.contains("bossbar-settings")) {
            logger.warn("Configuration section bossbar-settings not found!");
            configFile.createSection("bossbar-settings");
            configFile.set("bossbar-settings.enable-bossbar", false);
            configFile.set("bossbar-settings.bar-color", "RED");
            configFile.set("bossbar-settings.bar-style", "SEGMENTED_12");
            save(plugin.path, configFile, "config.yml");
            logger.info("Created section bossbar-settings");
        }
        if (!bossbarSettings.getBoolean("enable-bossbar", true)) {
            return;
        }
        bossbar_settings_enable_bossbar = bossbarSettings.getBoolean("enable-bossbar", true);
        bossbar_settings_bar_color = bossbarSettings.getString("bar-color", "RED");
        bossbar_settings_bar_style = bossbarSettings.getString("bar-style", "SEGMENTED_12");
        ConfigurationSection bossbar = plugin.messageFile.getConfigurationSection("bossbar");
        bossbar_message = getMessage(bossbar, "message");
    }

    public void loadSoundSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection soundSettings = config.getConfigurationSection("sound-settings");
        if (!configFile.contains("sound-settings")) {
            logger.warn("Configuration section sound-settings not found!");
            configFile.createSection("sound-settings");
            configFile.set("sound-settings.enable-sounds", true);
            configFile.set("sound-settings.bar-color", "RED");
            configFile.set("sound-settings.bar-style", "SEGMENTED_12");
            save(plugin.path, configFile, "config.yml");
            logger.info("Created section sound-settings");
        }
        sound_settings_enable_sounds = soundSettings.getBoolean("enable-sounds");
        if (!sound_settings_enable_sounds) {
            return;
        }
        sound_settings_on_capture = soundSettings.getString("on-capture", "ENTITY_ITEM_BREAK;1.0;1.0").split(";");
        sound_settings_on_pas_fail = soundSettings.getString("on-pas-fail", "ENTITY_VILLAGER_NO;1.0;1.0").split(";");
        sound_settings_on_pas_correct = soundSettings.getString("on-pas-correct", "ENTITY_PLAYER_LEVELUP;1.0;1.0").split(";");
    }

    public void loadEffects(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection effectSettings = config.getConfigurationSection("effect-settings");
        if (!configFile.contains("effect-settings")) {
            logger.warn("Configuration section effect-settings not found!");
            configFile.createSection("effect-settings");
            configFile.set("effect-settings.enable-effects", true);
            configFile.set("effect-settings.effects", List.of("BLINDNESS;3"));
            save(plugin.path, configFile, "config.yml");
            logger.info("Created section effect-settings");
        }
        effect_settings_enable_effects = effectSettings.getBoolean("enable-effects", true);
        effect_settings_effects = effectSettings.getStringList("effects");
    }

    public void loadLoggingSettings(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection loggingSettings = config.getConfigurationSection("logging-settings");
        if (!configFile.contains("logging-settings")) {
            logger.warn("Configuration section logging-settings not found!");
            configFile.createSection("logging-settings");
            configFile.set("logging-settings.logging-pas", true);
            configFile.set("logging-settings.logging-join", true);
            configFile.set("logging-settings.logging-enable-disable", true);
            configFile.set("logging-settings.logging-command-execution", true);
            save(plugin.path, configFile, "config.yml");
            logger.info("Created section logging-settings");
        }
        logging_settings_logging_pas = loggingSettings.getBoolean("logging-pas", true);
        logging_settings_logging_join = loggingSettings.getBoolean("logging-join", true);
        logging_settings_logging_enable_disable = loggingSettings.getBoolean("logging-enable-disable", true);
        logging_settings_logging_command_execution = loggingSettings.getBoolean("logging-command-execution", true);
    }

    public void loadFailCommands(FileConfiguration config, FileConfiguration configFile) {
        ConfigurationSection commands = config.getConfigurationSection("commands");
        if (!configFile.contains("commands")) {
            logger.warn("Configuration section commands not found!");
            configFile.createSection("commands");
            configFile.set("commands.not-in-config", Collections.emptyList());
            configFile.set("commands.not-in-opwhitelist", Collections.emptyList());
            configFile.set("commands.have-blacklisted-perm", Collections.emptyList());
            configFile.set("commands.not-admin-ip", Collections.emptyList());
            configFile.set("commands.failed-pass", Collections.emptyList());
            configFile.set("commands.failed-time", Collections.emptyList());
            configFile.set("commands.failed-rejoin", Collections.emptyList());
            save(plugin.path, configFile, "config.yml");
            logger.info("Created section main-settings");
        }
        commands_not_in_config = commands.getStringList("not-in-config");
        commands_not_in_opwhitelist = commands.getStringList("not-in-opwhitelist");
        commands_have_blacklisted_perm = commands.getStringList("have-blacklisted-perm");
        commands_not_admin_ip = commands.getStringList("not-admin-ip");
        commands_failed_pass = commands.getStringList("failed-pass");
        commands_failed_time = commands.getStringList("failed-time");
        commands_failed_rejoin = commands.getStringList("failed-rejoin");
    }

    public void loadPerms(FileConfiguration config) {
        perms = new HashSet<>(config.getStringList("permissions"));
    }

    public void loadLists(FileConfiguration config) {
        allowed_commands = new ArrayList<>(config.getStringList("allowed-commands"));
        ConfigurationSection secureSettings = config.getConfigurationSection("secure-settings");
        if (secureSettings.getBoolean("enable-op-whitelist")) {
            op_whitelist = new ArrayList<>(config.getStringList("op-whitelist"));
        }
        if (secureSettings.getBoolean("enable-permission-blacklist")) {
            blacklisted_perms = new HashSet<>(config.getStringList("blacklisted-perms"));
        }
        if (secureSettings.getBoolean("enable-ip-whitelist")) {
            ip_whitelist = new HashMap<>();
            for (String ipwl_player : config.getConfigurationSection("ip-whitelist").getKeys(false)) {
                List<String> ips = new ArrayList<>(config.getStringList("ip-whitelist." + ipwl_player));
                ip_whitelist.put(ipwl_player, ips);
            }
        }
    }

    public void setupExcluded(FileConfiguration config) {
        if (config.getBoolean("secure-settings.enable-excluded-players")) {
            ConfigurationSection excludedPlayers = config.getConfigurationSection("excluded-players");
            excluded_admin_pass = new ArrayList<>(excludedPlayers.getStringList("admin-pass"));
            excluded_op_whitelist = new ArrayList<>(excludedPlayers.getStringList("op-whitelist"));
            excluded_ip_whitelist = new ArrayList<>(excludedPlayers.getStringList("ip-whitelist"));
            excluded_blacklisted_perms = new ArrayList<>(excludedPlayers.getStringList("blacklisted-perms"));
        }
    }

    public void loadUspMessages(FileConfiguration message) {
        ConfigurationSection uspmsg = message.getConfigurationSection("uspmsg");
        uspmsg_consoleonly = getMessage(uspmsg, "consoleonly");
        uspmsg_playeronly = getMessage(uspmsg, "playeronly");
        uspmsg_logout = getMessage(uspmsg, "logout");
        uspmsg_reloaded = getMessage(uspmsg, "reloaded");
        uspmsg_rebooted = getMessage(uspmsg, "rebooted");
        uspmsg_playernotfound = getMessage(uspmsg, "playernotfound");
        uspmsg_alreadyinconfig = getMessage(uspmsg, "alreadyinconfig");
        uspmsg_notinconfig = getMessage(uspmsg, "notinconfig");
        uspmsg_playeradded = getMessage(uspmsg, "playeradded");
        uspmsg_playerremoved = getMessage(uspmsg, "playerremoved");
        uspmsg_ipadded = getMessage(uspmsg, "ipadded");
        uspmsg_setpassusage = getMessage(uspmsg, "setpassusage");
        uspmsg_addopusage = getMessage(uspmsg, "addopusage");
        uspmsg_addipusage = getMessage(uspmsg, "addipusage");
        uspmsg_rempassusage = getMessage(uspmsg, "rempassusage");
        uspmsg_remopusage = getMessage(uspmsg, "remopusage");
        uspmsg_ipremoved = getMessage(uspmsg, "ipremoved");
        uspmsg_remipusage = getMessage(uspmsg, "remipusage");
        uspmsg_usage = getMessage(uspmsg, "usage");
        uspmsg_usage_logout = getMessage(uspmsg, "usage-logout");
        uspmsg_usage_reload = getMessage(uspmsg, "usage-reload");
        uspmsg_usage_reboot = getMessage(uspmsg, "usage-reboot");
        uspmsg_usage_encrypt = getMessage(uspmsg, "usage-encrypt");
        uspmsg_usage_setpass = getMessage(uspmsg, "usage-setpass");
        uspmsg_usage_rempass = getMessage(uspmsg, "usage-rempass");
        uspmsg_usage_addop = getMessage(uspmsg, "usage-addop");
        uspmsg_usage_remop = getMessage(uspmsg, "usage-remop");
        uspmsg_usage_addip = getMessage(uspmsg, "usage-addip");
        uspmsg_usage_remip = getMessage(uspmsg, "usage-remip");
        uspmsg_otherdisabled = getMessage(uspmsg, "otherdisabled");
    }

    public void loadMsgMessages(FileConfiguration message) {
        ConfigurationSection msg = message.getConfigurationSection("msg");
        msg_message = getMessage(msg, "message");
        msg_incorrect = getMessage(msg, "incorrect");
        msg_correct = getMessage(msg, "correct");
        msg_noneed = getMessage(msg, "noneed");
        msg_cantbenull = getMessage(msg, "cantbenull");
        msg_playeronly = getMessage(msg, "playeronly");
    }

    public void loadTitleMessages(FileConfiguration message) {
        ConfigurationSection titles = message.getConfigurationSection("titles");
        titles_message = getMessage(titles, "message").split(";");
        titles_incorrect = getMessage(titles, "incorrect").split(";");
        titles_correct = getMessage(titles, "correct").split(";");
    }

    public void loadBroadcastMessages(FileConfiguration message) {
        ConfigurationSection broadcasts = message.getConfigurationSection("broadcasts");
        broadcasts_failed = getMessage(broadcasts, "failed");
        broadcasts_passed = getMessage(broadcasts, "passed");
        broadcasts_joined = getMessage(broadcasts, "joined");
        broadcasts_captured = getMessage(broadcasts, "captured");
    }

    public String getMessage(ConfigurationSection section, String key) {
        return Utils.colorize(section.getString(key, "&4&lERROR&r: " + key + " does not exist!").replace("%prefix%", main_settings_prefix), serializer);
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