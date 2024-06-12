package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.command.CommandSender;

public interface SubCommand {

    boolean execute(CommandSender sender, String label, String[] args);
    
}
