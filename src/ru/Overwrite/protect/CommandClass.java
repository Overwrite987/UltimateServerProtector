package ru.Overwrite.protect;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ru.Overwrite.protect.utils.Config;
import ru.Overwrite.protect.utils.Utils;

import java.util.List;

public class CommandClass implements CommandExecutor {
    private final Main plugin;

    public CommandClass(Main plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        FileConfiguration config = plugin.getConfig();
        if (cmd.getName().equalsIgnoreCase(config.getString("main-settings.pas-command"))) {
            if (!(sender instanceof Player)) {
                Bukkit.getLogger().info(Main.getMessagePrefixed("msg.playeronly"));
                return true;
            }
            Player p = (Player)sender;
            if (plugin.login.containsKey(p.getPlayer())) {
                if (args.length == 0) {
                    sender.sendMessage(Main.getMessagePrefixed("msg.cantbenull"));
                } else {
                    plugin.passwordHandler.checkPassword(p, args[0], false);
                }
                return true;
            } else {
                sender.sendMessage(Main.getMessagePrefixed("msg.noneed"));
            }
            return false;
        }
        if (cmd.getName().equalsIgnoreCase("ultimateserverprotector")) {
            if (sender.hasPermission("serverprotector.admin")) {
                if (args.length == 0) {
                    sender.sendMessage("§7§l> §7Использование:");
                    sender.sendMessage("§6§o/usp reload§7 - перезагрузить конфиг");
                    sender.sendMessage("§6§o/usp reboot§7 - перезапустить плагин");
                    if (config.getBoolean("secure-settings.enable-admin-commands")) {
                        sender.sendMessage("§6§o/usp setpass (ник) (пароль) §7- установить пароль игроку");
                        sender.sendMessage("§6§o/usp addop (ник) §7- добавить игрока в op-whitelist");
                        sender.sendMessage("§6§o/usp addip (ip) §7- добавить ip в ip-whitelist");
                    }
                    return true;
                }
                if (args[0].equalsIgnoreCase("reload")) {
                    plugin.reloadConfig();
                    sender.sendMessage(Main.getMessagePrefixed("uspmsg.reloaded"));
                    return true;
                }
                if (args[0].equalsIgnoreCase("reboot")) {
                    plugin.reloadConfig();
                    Bukkit.getScheduler().cancelTasks(plugin);
                    plugin.login.clear();
                    plugin.ips.clear();
                    Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, Runner::run, 20L, 40L);
                    Runner.startMSG();
                    if (config.getBoolean("punish-settings.enable-time")) {
                        Runner.startTimer();
                    }
                    if (config.getBoolean("punish-settings.notadmin-punish")) {
                        Runner.adminCheck();
                    }
                    if (config.getBoolean("secure-settings.enable-op-whitelist")) {
                        Runner.startOpCheck();
                    }
                    sender.sendMessage(Main.getMessagePrefixed("uspmsg.rebooted"));
                    return true;
                }
                if (args[0].equalsIgnoreCase("setpass") && config.getBoolean("secure-settings.enable-admin-commands")) {
                    String nickname = args[1];
                    if (plugin.isAdmin(nickname)) {
                        sender.sendMessage(Main.getMessagePrefixed("uspmsg.alreadyinconfig"));
                        return true;
                    }
                    if (args.length < 4) {
                        addAdmin(nickname, args[2]);
                        sender.sendMessage(Main.getMessagePrefixed("uspmsg.playeradded", s -> s.replace("%nick%", nickname)));
                        return true;
                    }
                    sender.sendMessage(Main.getPrefix() + Utils.colorize("§f/usp setpass (ник) (пароль)"));
                    return true;
                }
                if (args[0].equalsIgnoreCase("addop") && config.getBoolean("secure-settings.enable-admin-commands")) {
                    if (args.length > 1) {
                        List<String> wl = config.getStringList("op-whitelist");
                        wl.add(args[1]);
                        config.set("op-whitelist", wl);
                        plugin.saveConfig();
                        sender.sendMessage(Main.getMessage("uspmsg.playeradded", s -> s.replace("%nick%", args[1])));
                        return true;
                    }
                    sender.sendMessage(Main.getPrefix() + Utils.colorize("§f/usp addop (ник)"));
                    return true;
                }
                if (args[0].equalsIgnoreCase("addip") && sender.hasPermission("serverprotector.admin") && config.getBoolean("secure-settings.enable-admin-commands")) {
                    if (args.length > 1 && args[1] != null) {
                        List<String> ipwl = config.getStringList("ip-whitelist");
                        ipwl.add(args[1]);
                        config.set("ip-whitelist", ipwl);
                        sender.sendMessage(Main.getMessage("uspmsg.ipadded", s -> s.replace("%nick%", args[1])));
                        return true;
                    }
                    sender.sendMessage(Main.getPrefix() + Utils.colorize("§f/usp addip (ip)"));
                    return true;
                }
                sender.sendMessage("§7§l> §7Использование:");
                sender.sendMessage("§6§o/usp reload§7 - перезагрузить конфиг");
                sender.sendMessage("§6§o/usp reboot§7 - перезапустить плагин");
                if (config.getBoolean("secure-settings.enable-admin-commands")) {
                    sender.sendMessage("§6§o/usp setpass (ник) (пароль) §7- установить пароль игроку");
                    sender.sendMessage("§6§o/usp addop (ник) §7- добавить игрока в op-whitelist");
                    sender.sendMessage("§6§o/usp addip (ip) §7- добавить ip в ip-whitelist");
                }
                return true;
            } else {
                sender.sendMessage("§7This server is using §cUltimateServerProtector §7- the most powerful security plugin made by §5Overwrite");
            }
        }
        return true;
    }

    public void addAdmin(String nick, String pas) {
        FileConfiguration config = plugin.getConfig();
        FileConfiguration data = Config.getFile(config.getString("main-settings.data-file"));
        data.set("data." + nick + ".pass", pas);
        Config.save(data, config.getString("main-settings.data-file"));
    }
}

