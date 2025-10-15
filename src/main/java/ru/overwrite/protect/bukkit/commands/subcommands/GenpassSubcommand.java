package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.command.CommandSender;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.utils.Utils;

public class GenpassSubcommand extends AbstractSubCommand {

    public GenpassSubcommand(ServerProtectorManager plugin) {
        super(plugin, "genpass", "serverprotector.genpass", false);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        sender.sendMessage(Utils.generatePassword(args.length > 1 ? Integer.parseInt(args[1]) : 12));
        return true;
    }
}