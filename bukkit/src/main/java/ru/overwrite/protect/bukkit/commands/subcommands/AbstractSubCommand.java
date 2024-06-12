package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.command.CommandSender;
import ru.overwrite.protect.bukkit.PasswordHandler;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.utils.Config;

public abstract class AbstractSubCommand implements SubCommand {

    public abstract String getName();

    protected final ServerProtectorManager plugin;
    protected final ServerProtectorAPI api;
    protected final Config pluginConfig;
    protected final PasswordHandler passwordHandler;

    public AbstractSubCommand(ServerProtectorManager plugin) {
        this.plugin = plugin;
        this.api = plugin.getPluginAPI();
        this.pluginConfig = plugin.getPluginConfig();
        this.passwordHandler = plugin.getPasswordHandler();
    }

    protected void sendHelp(CommandSender sender, String label) {
        sendCmdMessage(sender, pluginConfig.uspmsg_usage, label, "serverprotector.protect");
        sendCmdMessage(sender, pluginConfig.uspmsg_usage_logout, label, "serverprotector.protect");
        if (!sender.hasPermission("serverprotector.admin")) {
            return;
        }
        sendCmdMessage(sender, pluginConfig.uspmsg_usage_reload, label, "serverprotector.reload");
        sendCmdMessage(sender, pluginConfig.uspmsg_usage_reboot, label, "serverprotector.reboot");
        if (pluginConfig.encryption_settings_enable_encryption) {
            sendCmdMessage(sender, pluginConfig.uspmsg_usage_encrypt, label, "serverprotector.encrypt");
        }
        if (!pluginConfig.main_settings_enable_admin_commands) {
            sender.sendMessage(pluginConfig.uspmsg_otherdisabled);
            return;
        }
        sendCmdMessage(sender, pluginConfig.uspmsg_usage_setpass, label, "serverprotector.setpass");
        sendCmdMessage(sender, pluginConfig.uspmsg_usage_rempass, label, "serverprotector.rempass");
        sendCmdMessage(sender, pluginConfig.uspmsg_usage_addop, label, "serverprotector.addop");
        sendCmdMessage(sender, pluginConfig.uspmsg_usage_remop, label, "serverprotector.remop");
        sendCmdMessage(sender, pluginConfig.uspmsg_usage_addip, label, "serverprotector.addip");
        sendCmdMessage(sender, pluginConfig.uspmsg_usage_remip, label, "serverprotector.remip");
    }

    protected void sendCmdMessage(CommandSender sender, String msg, String label, String permission) {
        if (sender.hasPermission(permission)) {
            sender.sendMessage(msg.replace("%cmd%", label));
        }
    }

    protected void sendCmdMessage(CommandSender sender, String msg, String label) {
        sender.sendMessage(msg.replace("%cmd%", label));
    }
}
