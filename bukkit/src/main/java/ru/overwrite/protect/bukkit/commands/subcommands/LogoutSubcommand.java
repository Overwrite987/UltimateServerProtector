package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.api.ServerProtectorLogoutEvent;
import ru.overwrite.protect.bukkit.utils.Utils;

public class LogoutSubcommand extends AbstractSubCommand {

    public LogoutSubcommand(ServerProtectorManager plugin) {
        super(plugin, "logout", "serverprotector.protect", false);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(pluginConfig.getUspMessages().playerOnly());
            return false;
        }
        if (api.isAuthorised(p)) {
            plugin.getRunner().run(() -> {
                new ServerProtectorLogoutEvent(p, Utils.getIp(p)).callEvent();
                api.deauthorisePlayer(p);
            });
            p.kickPlayer(pluginConfig.getUspMessages().logout());
            return true;
        }
        return false;
    }
}