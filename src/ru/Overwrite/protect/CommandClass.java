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
import ru.Overwrite.protect.utils.RGBcolors;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class CommandClass implements CommandExecutor {

    public static HashMap<Player, Integer> attemps = new HashMap<>();

    public boolean onCommand(CommandSender sender, Command cmd, String commandlabel, String[] args) {
        FileConfiguration config = Main.getInstance().getConfig();
        FileConfiguration data = Config.getFile(config.getString("main-settings.data-file"));
        FileConfiguration message = Config.getFile("message.yml");
        if (cmd.getName().equalsIgnoreCase(config.getString("main-settings.pas-command"))) {
            if (!(sender instanceof Player)) {
                Bukkit.getLogger().info(RGBcolors.translate(config.getString("main-settings.prefix") + message.getString("msg.playeronly")));
                return true;
            }
            Player p = (Player)sender;
            if (Main.getInstance().login.containsKey(p.getPlayer())) {
                if (args.length == 0) {
                    sender.sendMessage(RGBcolors.translate((config.getString("main-settings.prefix") + message.getString("msg.cantbenull"))));
                } else if (args[0].equals(data.getString("data." + sender.getName() + ".pass"))) {
                    correctPassword(p);
                } else {
                    sender.sendMessage(RGBcolors.translate((config.getString("main-settings.prefix") + message.getString("msg.incorrect"))));
                    onFail(p);
                    if (!attempsFULL(p) && config.getBoolean("punish-settings.enable-attemps")) {
                        for (String c : config.getStringList("commands.failed-pass")) {
                            Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), (c.replace("%player%", p.getName())));
                        }
                    }
                }
                return true;
            } else {
                sender.sendMessage(RGBcolors.translate(config.getString("main-settings.prefix") + message.getString("msg.noneed")));
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
                    Main.getInstance().reloadConfig();
                    sender.sendMessage(RGBcolors.translate(config.getString("main-settings.prefix") + message.getString("uspmsg.reloaded")));
                    return true;
                }
                if (args[0].equalsIgnoreCase("reboot")) {
                    Main.getInstance().reloadConfig();
                    Bukkit.getScheduler().cancelTasks(Main.getInstance());
                    Main.getInstance().login.clear();
                    Main.getInstance().ips.clear();
                    Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(), Runner::run, 20L, 40L);
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
                    sender.sendMessage(RGBcolors.translate(config.getString("main-settings.prefix") + message.getString("uspmsg.rebooted")));
                    return true;
                }
                if (args[0].equalsIgnoreCase("setpass") && config.getBoolean("secure-settings.enable-admin-commands")) {
                    String nickname = args[1];
                    if (Main.getInstance().isAdmin(nickname)) {
                        sender.sendMessage(RGBcolors.translate(config.getString("main-settings.prefix") + message.getString("uspmsg.alreadyinconfig")));
                        return true;
                    }
                    if (args.length > 1 && args.length < 4) {
                        addAdmin(nickname, args[2]);
                        sender.sendMessage(RGBcolors.translate(config.getString("main-settings.prefix") + message.getString("uspmsg.playeradded").replace("%nick%", nickname)));
                        return true;
                    }
                    sender.sendMessage(RGBcolors.translate(config.getString("main-settings.prefix") + "§f/usp setpass (ник) (пароль)"));
                    return true;
                }
                if (args[0].equalsIgnoreCase("addop") && config.getBoolean("secure-settings.enable-admin-commands")) {
                    if (args.length > 1) {
                        List<String> wl = config.getStringList("op-whitelist");
                        wl.add(args[1]);
                        config.set("op-whitelist", wl);
                        Main.getInstance().saveConfig();
                        sender.sendMessage(RGBcolors.translate(config.getString("main-settings.prefix") + message.getString("uspmsg.playeradded").replace("%nick%", args[1])));
                        return true;
                    }
                    sender.sendMessage(RGBcolors.translate(config.getString("main-settings.prefix") + "§f/usp addop (ник)"));
                    return true;
                }
                if (args[0].equalsIgnoreCase("addip") && sender.hasPermission("serverprotector.admin") && config.getBoolean("secure-settings.enable-admin-commands")) {
                    if (args.length > 1 && args[1] != null) {
                        List<String> ipwl = config.getStringList("ip-whitelist");
                        ipwl.add(args[1]);
                        config.set("ip-whitelist", ipwl);
                        sender.sendMessage(RGBcolors.translate(config.getString("main-settings.prefix") + message.getString("uspmsg.ipadded").replace("%nick%", args[1])));
                        return true;
                    }
                    sender.sendMessage(RGBcolors.translate(config.getString("main-settings.prefix") + "§f/usp addip (ip)"));
                    return true;
                }
                if (args[0].equalsIgnoreCase("help") || !args[0].equalsIgnoreCase("addip") || (!args[0].equalsIgnoreCase("addop"))
                        || !args[0].equalsIgnoreCase("setpass") || !args[0].equalsIgnoreCase("reload")
                        || !args[0].equalsIgnoreCase("reboot")) {
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
            } else {
                sender.sendMessage("§7This server is using §cUltimateServerProtector §7- the most powerfull secutity plugin made by §5Overwrite");
            }
        }
        return true;
    }

    private void onFail(Player p) {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("[dd-MM-yyy] HH:mm:ss -");
        FileConfiguration config = Main.getInstance().getConfig();
        FileConfiguration message = Config.getFile("message.yml");
        if (!attemps.containsKey(p)) {
            attemps.put(p, 1);
            if (config.getBoolean("sound-settings.enable-sounds")) {
                p.playSound(p.getLocation(), Sound.valueOf(config.getString("sound-settings.on-pas-fail")),
                        (float)config.getDouble("sound-settings.volume"), (float)config.getDouble("sound-settings.pitch"));
            }
            if (config.getBoolean("message-settings.enable-broadcasts")) {
                Bukkit.broadcast(RGBcolors.translate(config.getString("main-settings.prefix") +
                        ((message.getString("broadcasts.failed").replace("%player%", p.getName()).replace("%ip%", p.getAddress().getAddress().getHostAddress())))), "serverprotector.admin");
            }
            if (config.getBoolean("logging-settings.logging-pas")) {
                Main.getInstance().logToFile((message.getString("log-format.failed").replace("%player%", p.getName()).replace("%ip%", p.getAddress().getAddress().getHostAddress())
                        .replace("%date%", formatter.format(date))));
            }
            if (config.getBoolean("message-settings.enable-console-broadcasts")) {
                Bukkit.getConsoleSender().sendMessage(RGBcolors.translate(config.getString("main-settings.prefix") +
                        ((message.getString("broadcasts.failed").replace("%player%", p.getName()).replace("%ip%", p.getAddress().getAddress().getHostAddress())))));
            }
        } else {
            attemps.put(p,(attemps.get(p)).intValue() + 1);
            if (config.getBoolean("sound-settings.enable-sounds")) {
                p.playSound(p.getLocation(), Sound.valueOf(config.getString("sound-settings.on-pas-fail")),
                        (float)config.getDouble("sound-settings.volume"), (float)config.getDouble("sound-settings.pitch"));
            }
            if (config.getBoolean("message-settings.enable-broadcasts")) {
                Bukkit.broadcast(RGBcolors.translate(config.getString("main-settings.prefix") +
                        ((message.getString("broadcasts.failed").replace("%player%", p.getName()).replace("%ip%", p.getAddress().getAddress().getHostAddress())))), "serverprotector.admin");
            }
            if (config.getBoolean("logging-settings.logging-pas")) {
                Main.getInstance().logToFile((message.getString("log-format.failed").replace("%player%", p.getName()).replace("%ip%", p.getAddress().getAddress().getHostAddress())
                        .replace("%date%", formatter.format(date))));
            }
            if (config.getBoolean("message-settings.enable-console-broadcasts")) {
                Bukkit.getConsoleSender().sendMessage(RGBcolors.translate(config.getString("main-settings.prefix") +
                        ((message.getString("broadcasts.failed").replace("%player%", p.getName()).replace("%ip%", p.getAddress().getAddress().getHostAddress())))));
            }
        }
    }


    public boolean attempsFULL(Player p) {
        if (!attemps.containsKey(p))
            return true;
        FileConfiguration config = Main.getInstance().getConfig();
        return (attemps.get(p) < config.getInt("punish-settings.max-attempts"));
    }

    public void addAdmin(String nick, String pas) {
        FileConfiguration config = Main.getInstance().getConfig();
        FileConfiguration data = Config.getFile(config.getString("main-settings.data-file"));
        data.set("data.$nick.pass".replace("$nick", nick), pas);
        Config.save(data, config.getString("main-settings.data-file"));
    }

    public void correctPassword(Player p) {
        Date date = new Date();
        FileConfiguration message = Config.getFile("message.yml");
        SimpleDateFormat formatter = new SimpleDateFormat("[dd-MM-yyy] HH:mm:ss -");
        FileConfiguration config = Main.getInstance().getConfig();
        Main.getInstance().login.remove(p, 0);
        p.sendMessage(RGBcolors.translate(config.getString("main-settings.prefix") + message.getString("msg.correct")));
        if (config.getBoolean("sound-settings.enable-sounds")) {
            p.playSound(p.getLocation(), Sound.valueOf(config.getString("sound-settings.on-pas-correct")),
                    (float)config.getDouble("sound-settings.volume"), (float)config.getDouble("sound-settings.pitch"));
        }
        if (config.getBoolean("effect-settings.enable-effects")) {
            for (PotionEffect s : p.getActivePotionEffects()) {
                p.removePotionEffect(s.getType());
            }
        }
        if (config.getBoolean("message-settings.enable-broadcasts")) {
            Bukkit.broadcast(RGBcolors.translate(config.getString("main-settings.prefix") +
                    (RGBcolors.translate(message.getString("broadcasts.passed").replace("%player%", p.getName()).replace("%ip%", p.getAddress().getAddress().getHostAddress())))), "serverprotector.admin");
        }
        if (config.getBoolean("message-settings.enable-console-broadcasts")) {
            Bukkit.getConsoleSender().sendMessage(RGBcolors.translate(config.getString("main-settings.prefix") +
                    ((message.getString("broadcasts.passed").replace("%player%", p.getName()).replace("%ip%", p.getAddress().getAddress().getHostAddress())))));
        }
        if (config.getBoolean("session-settings.session")) {
            Main.getInstance().ips.put(p.getName()+p.getAddress().getAddress().getHostAddress(), "focku");
        }
        if (config.getBoolean("logging-settings.logging-pas")) {
            Main.getInstance().logToFile(message.getString("log-format.passed").replace("%player%", p.getName()).replace("%ip%", p.getAddress().getAddress().getHostAddress())
                    .replace("%date%", formatter.format(date)));
        }
        if (config.getBoolean("session-settings.session-time-enabled")) {
            Main.getInstance().getServer().getScheduler().scheduleAsyncDelayedTask(Main.getInstance(), new Runnable() {
                public void run() {
                    if (!Main.getInstance().login.containsKey(p))
                    if (!Main.getInstance().login.containsKey(p));
                    Main.getInstance().ips.remove(p.getName()+p.getAddress().getAddress().getHostAddress());
                }
            }, config.getInt("session-settings.session-time")*20);
        }
    }
}

