package ru.Overwrite.protect;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import ru.Overwrite.protect.utils.Config;
import ru.Overwrite.protect.utils.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandClass implements CommandExecutor {

    public final Map<Player, Integer> attempts = new HashMap<>();
    private final Main plugin;

    public CommandClass(Main plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        FileConfiguration config = plugin.getConfig();
        FileConfiguration data = Config.getFile(config.getString("main-settings.data-file"));
        if (cmd.getName().equalsIgnoreCase(config.getString("main-settings.pas-command"))) {
            if (!(sender instanceof Player)) {
                Bukkit.getLogger().info(Main.getMessageFull("msg.playeronly"));
                return true;
            }
            Player p = (Player)sender;
            if (plugin.login.containsKey(p.getPlayer())) {
                if (args.length == 0) {
                    sender.sendMessage(Main.getMessageFull("msg.cantbenull"));
                } else if (args[0].equals(data.getString("data." + sender.getName() + ".pass"))) {
                    correctPassword(p);
                } else {
                    sender.sendMessage(Main.getMessageFull("msg.incorrect"));
                    onFail(p);
                    if (!attemptsMax(p) && config.getBoolean("punish-settings.enable-attemps")) {
                        for (String c : config.getStringList("commands.failed-pass")) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), (c.replace("%player%", p.getName())));
                        }
                    }
                }
                return true;
            } else {
                sender.sendMessage(Main.getMessageFull("msg.noneed"));
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
                    sender.sendMessage(Main.getMessageFull("uspmsg.reloaded"));
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
                    sender.sendMessage(Main.getMessageFull("uspmsg.rebooted"));
                    return true;
                }
                if (args[0].equalsIgnoreCase("setpass") && config.getBoolean("secure-settings.enable-admin-commands")) {
                    String nickname = args[1];
                    if (plugin.isAdmin(nickname)) {
                        sender.sendMessage(Main.getMessageFull("uspmsg.alreadyinconfig"));
                        return true;
                    }
                    if (args.length < 4) {
                        addAdmin(nickname, args[2]);
                        sender.sendMessage(Main.getMessageFull("uspmsg.playeradded", s -> s.replace("%nick%", nickname)));
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

    private void onFail(Player p) {
        Date date = new Date();
        FileConfiguration config = plugin.getConfig();
        if (!attempts.containsKey(p)) {
            attempts.put(p, 1);
        } else {
            attempts.put(p, attempts.get(p) + 1);
        }
        if (config.getBoolean("sound-settings.enable-sounds")) {
            p.playSound(p.getLocation(), Sound.valueOf(config.getString("sound-settings.on-pas-fail")),
                    (float)config.getDouble("sound-settings.volume"), (float)config.getDouble("sound-settings.pitch"));
        }
        if (config.getBoolean("logging-settings.logging-pas")) {
            plugin.logAction("log-format.failed", p, date);
        }
        String msg = Main.getMessageFull("broadcast.failed", s -> s.replace("%player%", p.getName()).replace("%ip%", Utils.getIp(p)));
        if (config.getBoolean("message-settings.enable-broadcasts")) {
            Bukkit.broadcast(msg, "serverprotector.admin");
        }
        if (config.getBoolean("message-settings.enable-console-broadcasts")) {
            Bukkit.getConsoleSender().sendMessage(msg);
        }
    }


    public boolean attemptsMax(Player p) {
        if (!attempts.containsKey(p))
            return true;
        FileConfiguration config = plugin.getConfig();
        return (attempts.get(p) < config.getInt("punish-settings.max-attempts"));
    }

    public void addAdmin(String nick, String pas) {
        FileConfiguration config = plugin.getConfig();
        FileConfiguration data = Config.getFile(config.getString("main-settings.data-file"));
        data.set("data." + nick + ".pass", pas);
        Config.save(data, config.getString("main-settings.data-file"));
    }

    public void correctPassword(Player p) {
        Date date = new Date();
        FileConfiguration config = plugin.getConfig();
        plugin.login.remove(p, 0);
        p.sendMessage(Main.getMessageFull("msg.correct"));
        if (config.getBoolean("sound-settings.enable-sounds")) {
            p.playSound(p.getLocation(), Sound.valueOf(config.getString("sound-settings.on-pas-correct")),
                    (float)config.getDouble("sound-settings.volume"), (float)config.getDouble("sound-settings.pitch"));
        }
        if (config.getBoolean("effect-settings.enable-effects")) {
            for (PotionEffect s : p.getActivePotionEffects()) {
                p.removePotionEffect(s.getType());
            }
        }
        if (config.getBoolean("session-settings.session")) {
            plugin.ips.put(p.getName()+Utils.getIp(p), "focku");
        }
        if (config.getBoolean("logging-settings.logging-pas")) {
            plugin.logAction("log-format.passed", p, date);
        }
        if (config.getBoolean("session-settings.session-time-enabled")) {
            plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                if (!plugin.login.containsKey(p)) {
                    plugin.ips.remove(p.getName() + Utils.getIp(p));
                }
            }, config.getInt("session-settings.session-time") * 20L);
        }
        String msg = Main.getMessageFull("broadcasts.passed", s -> s.replace("%player%", p.getName()).replace("%ip%", Utils.getIp(p)));
        if (config.getBoolean("message-settings.enable-broadcasts")) {
            Bukkit.broadcast(msg, "serverprotector.admin");
        }
        if (config.getBoolean("message-settings.enable-console-broadcasts")) {
            Bukkit.getConsoleSender().sendMessage(msg);
        }
    }
}

