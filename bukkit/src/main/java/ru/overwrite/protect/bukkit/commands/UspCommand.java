package ru.overwrite.protect.bukkit.commands;

import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.commands.subcommands.*;
import ru.overwrite.protect.bukkit.configuration.Config;
import ru.overwrite.protect.bukkit.configuration.data.UspMessages;

import java.util.*;

public class UspCommand implements CommandExecutor, TabCompleter {

    private final ServerProtectorManager plugin;
    private final Config pluginConfig;

    private final Map<String, AbstractSubCommand> subCommands = new HashMap<>();

    public UspCommand(ServerProtectorManager plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
        registerSubCommands(plugin);
    }

    private void registerSubCommands(ServerProtectorManager plugin) {
        registerSubCommand(new LogoutSubcommand(plugin));
        registerSubCommand(new ReloadSubcommand(plugin));
        registerSubCommand(new RebootSubcommand(plugin));
        registerSubCommand(new EncryptSubcommand(plugin));
        registerSubCommand(new SetpassSubcommand(plugin));
        registerSubCommand(new AddopSubcommand(plugin));
        registerSubCommand(new AddipSubcommand(plugin));
        registerSubCommand(new RempassSubcommand(plugin));
        registerSubCommand(new RemopSubcommand(plugin));
        registerSubCommand(new RemipSubcommand(plugin));
        registerSubCommand(new UpdateSubcommand(plugin));
    }

    private void registerSubCommand(AbstractSubCommand subCmd) {
        subCommands.put(subCmd.getName(), subCmd);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }
        AbstractSubCommand subCommand = subCommands.get(args[0].toLowerCase());
        if (subCommand != null) {
            if (subCommand.isAdminCommand()) {
                if (!pluginConfig.getMainSettings().enableAdminCommands()) {
                    sendHelp(sender, label);
                    return false;
                }
                if (pluginConfig.getSecureSettings().onlyConsoleUsp() && !(sender instanceof ConsoleCommandSender)) {
                    sender.sendMessage(pluginConfig.getUspMessages().consoleOnly());
                    return false;
                }
            }
            if (!sender.hasPermission(subCommand.getPermission())) {
                sendHelp(sender, label);
                return false;
            }
            return subCommand.execute(sender, label, args);
        }
        if (sender.hasPermission("serverprotector.protect")) {
            sendHelp(sender, label);
            return true;
        }
        sender.sendMessage("§6❖ §7Running §c§lUltimateServerProtector " + plugin.getDescription().getVersion()
                + "§7 by §5OverwriteMC");
        return true;
    }

    private void sendHelp(CommandSender sender, String label) {
        UspMessages uspMessages = pluginConfig.getUspMessages();
        sendCmdMessage(sender, uspMessages.usage(), label, "protect");
        sendCmdMessage(sender, uspMessages.usageLogout(), label, "protect");
        if (!sender.hasPermission("admin")) {
            return;
        }
        sendCmdMessage(sender, uspMessages.usageReload(), label, "reload");
        sendCmdMessage(sender, uspMessages.usageReboot(), label, "reboot");
        if (pluginConfig.getEncryptionSettings().enableEncryption()) {
            sendCmdMessage(sender, uspMessages.usageEncrypt(), label, "encrypt");
        }
        if (!pluginConfig.getMainSettings().enableAdminCommands()) {
            sender.sendMessage(uspMessages.otherDisabled());
            return;
        }
        sendCmdMessage(sender, uspMessages.setPassUsage(), label, "setpass");
        sendCmdMessage(sender, uspMessages.usageRemPass(), label, "rempass");
        sendCmdMessage(sender, uspMessages.usageAddOp(), label, "addop");
        sendCmdMessage(sender, uspMessages.usageRemOp(), label, "remop");
        sendCmdMessage(sender, uspMessages.usageAddIp(), label, "addip");
        sendCmdMessage(sender, uspMessages.usageRemIp(), label, "remip");
    }

    private void sendCmdMessage(CommandSender sender, String msg, String label, String permission) {
        if (sender.hasPermission("serverprotector." + permission)) {
            sender.sendMessage(msg.replace("%cmd%", label));
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (pluginConfig.getSecureSettings().onlyConsoleUsp() && !(sender instanceof ConsoleCommandSender)) {
            return List.of();
        }
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("logout");
            completions.add("reload");
            completions.add("reboot");
            if (pluginConfig.getEncryptionSettings().enableEncryption()) {
                completions.add("encrypt");
            }
            if (pluginConfig.getMainSettings().enableAdminCommands()) {
                completions.add("setpass");
                completions.add("rempass");
                completions.add("addop");
                completions.add("remop");
                completions.add("addip");
                completions.add("remip");
            }
        }
        return getResult(args, completions);
    }

    private List<String> getResult(String[] args, List<String> completions) {
        List<String> result = new ArrayList<>();
        for (String c : completions) {
            if (startsWithIgnoreCase(c, args[args.length - 1])) {
                result.add(c);
            }
        }
        return result;
    }

    private boolean startsWithIgnoreCase(@NotNull String str, @NotNull String prefix) {
        return str.regionMatches(true, 0, prefix, 0, prefix.length());
    }
}
