package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import ru.overwrite.protect.bukkit.ServerProtectorManager;

public class ReloadSubcommand extends AbstractSubCommand {

    public String getName() {
        return "reload";
    }

    public ReloadSubcommand(ServerProtectorManager plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (pluginConfig.secure_settings_only_console_usp && !(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(pluginConfig.uspmsg_consoleonly);
            return false;
        }
        if (!sender.hasPermission("serverprotector.reload")) {
            sendHelp(sender, label);
            return false;
        }
        plugin.reloadConfigs();
        sender.sendMessage(pluginConfig.uspmsg_reloaded);
        return true;
    }
}
