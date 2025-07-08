package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.utils.logging.Logger;

public class DebugPrintSubcommand extends AbstractSubCommand {

    public DebugPrintSubcommand(ServerProtectorManager plugin) {
        super(plugin, "debug", "serverprotector.debug", true);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        Logger logger = plugin.getPluginLogger();
        if (args.length < 2) {
            sender.sendMessage("§cSpecify option");
            return true;
        }
        switch (args[1]) {
            case "printconfigdata":
                logger.info(pluginConfig.getSystemMessages().baselineDefault());
                logger.info("§eExcluded players: " + pluginConfig.getExcludedPlayers().toString());
                logger.info("§6Access data: " + pluginConfig.getAccessData().toString());
                logger.info("§eAPI settings: " + pluginConfig.getApiSettings().toString());
                logger.info("§6Blocking settings: " + pluginConfig.getBlockingSettings().toString());
                logger.info("§eCommands: " + pluginConfig.getCommands().toString());
                logger.info("§6Effect settings: " + pluginConfig.getEffectSettings().toString());
                logger.info("§eEncryption settings: " + pluginConfig.getEncryptionSettings().toString());
                logger.info("§6Geyser settings: " + pluginConfig.getGeyserSettings().toString());
                logger.info("§eLogging settings: " + pluginConfig.getLoggingSettings().toString());
                logger.info("§eMain settings: " + pluginConfig.getMainSettings().toString());
                logger.info("§eMessage settings: " + pluginConfig.getMessageSettings().toString());
                logger.info("§6Punish settings: " + pluginConfig.getPunishSettings().toString());
                logger.info("§eSecure settings: " + pluginConfig.getSecureSettings().toString());
                logger.info("§6Session settings: " + pluginConfig.getSessionSettings().toString());
                logger.info("§eSound settings: " + pluginConfig.getSoundSettings().toString());
                logger.info(pluginConfig.getSystemMessages().baselineDefault());
                break;
            case "checkplayer":
                if (args.length < 3) {
                    sender.sendMessage("§cPlayer not provided");
                    break;
                }
                Player playerToCheck = Bukkit.getPlayer(args[2]);
                if (playerToCheck == null) {
                    sender.sendMessage("§cPlayer not found: " + args[2]);
                    break;
                }
                logger.info("§6Is captured: " + api.isCaptured(playerToCheck));
                logger.info("§6Is authorised: " + api.isAuthorised(playerToCheck));
                logger.info("§6Has session: " + api.hasSession(playerToCheck));
                logger.info("§6Is admin (has password): " + plugin.isAdmin(playerToCheck.getName()));
                if (pluginConfig.getSecureSettings().enableExcludedPlayers()) {
                    logger.info("§6Is excluded from adminPass: " + api.isExcluded(playerToCheck, pluginConfig.getExcludedPlayers().adminPass()));
                    logger.info("§6Is excluded from opWhitelist: " + api.isExcluded(playerToCheck, pluginConfig.getExcludedPlayers().opWhitelist()));
                    logger.info("§6Is excluded from ipWhitelist: " + api.isExcluded(playerToCheck, pluginConfig.getExcludedPlayers().ipWhitelist()));
                    logger.info("§6Is excluded from blacklistedPerms: " + api.isExcluded(playerToCheck, pluginConfig.getExcludedPlayers().blacklistedPerms()));
                    logger.info("§6Is excluded from alerts: " + api.isExcluded(playerToCheck, pluginConfig.getExcludedPlayers().alert()));
                }
                break;
            default:
                sender.sendMessage("§cUnknown type: " + args[1]);
                return false;
        }
        return true;
    }
}
