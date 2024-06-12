package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.api.ServerProtectorLogoutEvent;
import ru.overwrite.protect.bukkit.utils.Utils;

public class LogoutSubcommand extends AbstractSubCommand {

    public String getName() {
        return "logout";
    }

    public LogoutSubcommand(ServerProtectorManager plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(pluginConfig.uspmsg_playeronly);
            return false;
        }
        Player p = (Player)sender;
        if (!p.hasPermission("serverprotector.protect")) {
            sendHelp(sender, label);
            return false;
        }
        if (api.isAuthorised(p)) {
            plugin.getRunner().run(() -> {
                new ServerProtectorLogoutEvent(p, Utils.getIp(p)).callEvent();
                api.deauthorisePlayer(p);
            });
            p.kickPlayer(pluginConfig.uspmsg_logout);
            return true;
        }
        return false;
    }
}
