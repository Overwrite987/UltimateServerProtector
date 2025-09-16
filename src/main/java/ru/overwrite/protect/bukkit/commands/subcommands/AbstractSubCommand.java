package ru.overwrite.protect.bukkit.commands.subcommands;

import lombok.Getter;
import org.bukkit.command.CommandSender;
import ru.overwrite.protect.bukkit.PasswordHandler;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.configuration.Config;

public abstract class AbstractSubCommand implements SubCommand {

    @Getter
    protected final String name;
    @Getter
    protected final String permission;
    @Getter
    protected final boolean adminCommand;

    protected final ServerProtectorManager plugin;
    protected final ServerProtectorAPI api;
    protected final Config pluginConfig;
    protected final PasswordHandler passwordHandler;

    protected AbstractSubCommand(ServerProtectorManager plugin, String name, String permission, boolean adminCommand) {
        this.plugin = plugin;
        this.api = plugin.getApi();
        this.pluginConfig = plugin.getPluginConfig();
        this.passwordHandler = plugin.getPasswordHandler();
        this.name = name;
        this.permission = permission;
        this.adminCommand = adminCommand;
    }

    protected void sendCmdUsage(CommandSender sender, String msg, String label) {
        sender.sendMessage(msg.replace("%cmd%", label));
    }

}
