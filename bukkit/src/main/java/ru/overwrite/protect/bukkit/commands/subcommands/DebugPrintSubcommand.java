package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.utils.logging.Logger;

import java.util.List;

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
            case "printexcluded":
                logger.info(pluginConfig.getExcludedPlayers().toString());
                break;
            case "printaccess":
                logger.info(pluginConfig.getAccessData().toString());
                break;
            case "printapi":
                logger.info(pluginConfig.getApiSettings().toString());
                break;
            case "printblocking":
                logger.info(pluginConfig.getBlockingSettings().toString());
                break;
            case "printbossbar":
                logger.info(pluginConfig.getBossbarSettings().toString());
                break;
            case "printbroadcasts":
                logger.info(pluginConfig.getBroadcasts().toString());
                break;
            case "printcommands":
                logger.info(pluginConfig.getCommands().toString());
                break;
            case "printeffects":
                logger.info(pluginConfig.getEffectSettings().toString());
                break;
            case "printencryption":
                logger.info(pluginConfig.getEncryptionSettings().toString());
                break;
            case "printgeyser":
                logger.info(pluginConfig.getGeyserSettings().toString());
                break;
            case "printlogging":
                logger.info(pluginConfig.getLoggingSettings().toString());
                break;
            case "printlogmessages":
                logger.info(pluginConfig.getLogMessages().toString());
                break;
            case "printmain":
                logger.info(pluginConfig.getMainSettings().toString());
                break;
            case "printmessages":
                logger.info(pluginConfig.getMessages().toString());
                break;
            case "printmessagesettings":
                logger.info(pluginConfig.getMessageSettings().toString());
                break;
            case "printpunish":
                logger.info(pluginConfig.getPunishSettings().toString());
                break;
            case "printsecure":
                logger.info(pluginConfig.getSecureSettings().toString());
                break;
            case "printsession":
                logger.info(pluginConfig.getSessionSettings().toString());
                break;
            case "printsound":
                logger.info(pluginConfig.getSoundSettings().toString());
                break;
            case "printsystemmessages":
                logger.info(pluginConfig.getSystemMessages().toString());
                break;
            case "printtitles":
                logger.info(pluginConfig.getTitles().toString());
                break;
            case "printusp":
                logger.info(pluginConfig.getUspMessages().toString());
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
                    logger.info("§6Is excluded from adminPass: " + plugin.isExcluded(playerToCheck, pluginConfig.getExcludedPlayers().adminPass()));
                    logger.info("§6Is excluded from opWhitelist: " + plugin.isExcluded(playerToCheck, pluginConfig.getExcludedPlayers().opWhitelist()));
                    logger.info("§6Is excluded from ipWhitelist: " + plugin.isExcluded(playerToCheck, pluginConfig.getExcludedPlayers().ipWhitelist()));
                    logger.info("§6Is excluded from blacklistedPerms: " + plugin.isExcluded(playerToCheck, pluginConfig.getExcludedPlayers().blacklistedPerms()));
                    logger.info("§6Is excluded from alerts: " + plugin.isExcluded(playerToCheck, pluginConfig.getExcludedPlayers().alert()));
                }
            default:
                sender.sendMessage("§cUnknown type: " + args[1]);
                return false;
        }
        return true;
    }
}
