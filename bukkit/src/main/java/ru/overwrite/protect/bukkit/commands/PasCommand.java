package ru.overwrite.protect.bukkit.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.overwrite.protect.bukkit.PasswordHandler;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.configuration.Config;

public final class PasCommand implements CommandExecutor {

    private final ServerProtectorManager plugin;
    private final ServerProtectorAPI api;
    private final PasswordHandler passwordHandler;
    private final Config pluginConfig;

    public PasCommand(ServerProtectorManager plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
        this.passwordHandler = plugin.getPasswordHandler();
        this.api = plugin.getPluginAPI();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getPluginLogger().info(pluginConfig.getMessages().playerOnly());
            return true;
        }
        if (!api.isCaptured(player)) {
            sender.sendMessage(pluginConfig.getMessages().noNeed());
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(pluginConfig.getMessages().cantBeNull());
            return true;
        }
        passwordHandler.checkPassword(player, args[0], false);
        return true;
    }
}
