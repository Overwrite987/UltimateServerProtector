package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.utils.Utils;

public class RebootSubcommand extends AbstractSubCommand {

    public String getName() {
        return "reboot";
    }

    public RebootSubcommand(ServerProtectorManager plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (pluginConfig.secure_settings_only_console_usp && !(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(pluginConfig.uspmsg_consoleonly);
            return false;
        }
        if (!sender.hasPermission("serverprotector.reboot")) {
            sendHelp(sender, label);
            return false;
        }

        plugin.getRunner().cancelTasks();
        plugin.reloadConfigs();
        plugin.time.clear();
        api.login.clear();
        api.ips.clear();
        api.saved.clear();
        if (Utils.bossbar != null) {
            Utils.bossbar.removeAll();
        }
        passwordHandler.attempts.clear();
        FileConfiguration newconfig = plugin.getConfig();
        plugin.startTasks(newconfig);
        sender.sendMessage(pluginConfig.uspmsg_rebooted);
        return true;
    }
}
