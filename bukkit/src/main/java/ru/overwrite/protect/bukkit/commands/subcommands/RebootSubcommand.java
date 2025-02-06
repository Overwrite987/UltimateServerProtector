package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import ru.overwrite.protect.bukkit.ServerProtectorManager;

public class RebootSubcommand extends AbstractSubCommand {

    public RebootSubcommand(ServerProtectorManager plugin) {
        super(plugin, "reboot", "serverprotector.reboot", false);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        plugin.getRunner().cancelTasks();
        plugin.reloadConfigs();
        plugin.getPerPlayerTime().clear();
        api.clearCaptured();
        api.clearSaved();
        api.clearSessions();
        for (String playerName : passwordHandler.getBossbars().keySet()) {
            passwordHandler.getBossbars().get(playerName).removeAll();
        }
        passwordHandler.getBossbars().clear();
        passwordHandler.getAttempts().clear();
        FileConfiguration newconfig = plugin.getConfig();
        plugin.startTasks(newconfig);
        sender.sendMessage(pluginConfig.getUspMessages().rebooted());
        return true;
    }
}
