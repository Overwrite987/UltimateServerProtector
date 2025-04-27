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
import ru.overwrite.protect.bukkit.configuration.data.Messages;

public final class PasCommand implements CommandExecutor {

    private final ServerProtectorManager plugin;
    private final ServerProtectorAPI api;
    private final PasswordHandler passwordHandler;
    private final Config pluginConfig;

    public PasCommand(ServerProtectorManager plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
        this.passwordHandler = plugin.getPasswordHandler();
        this.api = plugin.getApi();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        Messages messages = pluginConfig.getMessages();
        if (!(sender instanceof Player player)) {
            plugin.getPluginLogger().info(messages.playerOnly());
            return true;
        }
        if (!api.isCaptured(player)) {
            sender.sendMessage(messages.noNeed());
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(messages.cantBeNull());
            return true;
        }
        passwordHandler.checkPassword(player, args[0], false);
        return true;
    }
}
