package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.command.CommandSender;
import ru.overwrite.protect.bukkit.PasswordHandler;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.utils.Config;

public abstract class AbstractSubCommand implements SubCommand {

    protected final String name;
    protected final String permission;
    protected final boolean adminCommand;

    protected final ServerProtectorManager plugin;
    protected final ServerProtectorAPI api;
    protected final Config pluginConfig;
    protected final PasswordHandler passwordHandler;

    public AbstractSubCommand(ServerProtectorManager plugin, String name, String permission, boolean adminCommand) {
        this.plugin = plugin;
        this.api = plugin.getPluginAPI();
        this.pluginConfig = plugin.getPluginConfig();
        this.passwordHandler = plugin.getPasswordHandler();
        this.name = name;
        this.permission = permission;
        this.adminCommand = adminCommand;
    }

    public String getName() {
        return this.name;
    }

    public String getPermission() {
        return this.permission;
    }

    public boolean isAdminCommand() {
        return adminCommand;
    }

    protected void sendCmdUsage(CommandSender sender, String msg, String label) {
        sender.sendMessage(msg.replace("%cmd%", label));
    }

}