package ru.overwrite.protect.bukkit;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import ru.overwrite.protect.bukkit.utils.Config;
import ru.overwrite.protect.bukkit.utils.Utils;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
public class PasswordHandler {
 private final ServerProtector plugin;
 private final Map<Player, Integer> attempts;
 public PasswordHandler(ServerProtector plugin) {
 this.plugin = plugin;
 this.attempts = new HashMap<>();
 }
 public void checkPassword(Player player, String input, boolean resync) {
 FileConfiguration config = plugin.getConfig();
 FileConfiguration data;
 if (ServerProtector.fullpath) {
 data = Config.getFileFullPath(config.getString("file-settings.data-file"));
 } else {
 data = Config.getFile(config.getString("file-settings.data-file"));
 }
 if (input.equals(data.getString("data." + player.getName() + ".pass"))) {
 if (resync) {
 Bukkit.getScheduler().runTask(plugin, () -> correctPassword(player));
 } else {
 correctPassword(player);
 }
 } else {
 player.sendMessage(ServerProtectorManager.getMessage("msg.incorrect"));
 failedPassword(player);
 if (!isAttemptsMax(player) && config.getBoolean("punish-settings.enable-attemps")) {
 if (resync) {
 Bukkit.getScheduler().runTask(plugin, () -> failedPasswordCommands(player));
 plugin.login.remove(player);
 } else {
 failedPasswordCommands(player);
 }
 }
 }
 }
 public void clearAttempts() {
 attempts.clear();
 }
 private boolean isAttemptsMax(Player player) {
 if (!attempts.containsKey(player)) return true;
 FileConfiguration config = plugin.getConfig();
 return (attempts.get(player) < config.getInt("punish-settings.max-attempts"));
 }
 private void failedPasswordCommands(Player player) {
 for (String command : plugin.getConfig().getStringList("commands.failed-pass")) {
 Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
 }
 }
 private void failedPassword(Player player) {
 Date date = new Date();
 FileConfiguration config = plugin.getConfig();
 attempts.put(player, attempts.getOrDefault(player, 0) + 1);
 if (config.getBoolean("sound-settings.enable-sounds")) {
 player.playSound(player.getLocation(), Sound.valueOf(config.getString("sound-settings.on-pas-fail")),
 (float)config.getDouble("sound-settings.volume"), (float)config.getDouble("sound-settings.pitch"));
 }
 if (config.getBoolean("logging-settings.logging-pas")) {
 plugin.logAction("log-format.failed", player, date);
 }
 String msg = ServerProtectorManager.getMessage("broadcasts.failed", s -> s.replace("%player%", player.getName()).replace("%ip%", Utils.getIp(player)));
 if (config.getBoolean("message-settings.enable-broadcasts")) {
 Bukkit.broadcast(msg, "serverprotector.admin");
 }
 if (config.getBoolean("message-settings.enable-console-broadcasts")) {
 Bukkit.getConsoleSender().sendMessage(msg);
 }
 }
 private void correctPassword(Player player) {
 Date date = new Date();
 FileConfiguration config = plugin.getConfig();
 plugin.login.remove(player, 0);
 player.sendMessage(ServerProtectorManager.getMessage("msg.correct"));
 if (config.getBoolean("session-settings.session")) {
 ServerProtector.getInstance().time.remove(player);
 }
 if (config.getBoolean("sound-settings.enable-sounds")) {
 player.playSound(player.getLocation(), Sound.valueOf(config.getString("sound-settings.on-pas-correct")),
 (float)config.getDouble("sound-settings.volume"), (float)config.getDouble("sound-settings.pitch"));
 }
 if (config.getBoolean("effect-settings.enable-effects")) {
 for (PotionEffect s : player.getActivePotionEffects()) {
 player.removePotionEffect(s.getType());
 }
 }
 if (config.getBoolean("session-settings.session")) {
 plugin.ips.add(player.getName()+Utils.getIp(player));
 }
 if (config.getBoolean("session-settings.session-time-enabled")) {
 plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
 if (!plugin.login.containsKey(player)) {
 plugin.ips.remove(player.getName() + Utils.getIp(player));
 }
 }, config.getInt("session-settings.session-time") * 20L);
 }
 if (config.getBoolean("logging-settings.logging-pas")) {
 plugin.logAction("log-format.passed", player, date);
 }
 if (config.getBoolean("bossbar-settings.enable-bossbar")) {
 if (Runner.bossbar == null) {
 return;
 }
 if (Runner.bossbar.getPlayers().contains(player)) {
 Runner.bossbar.removePlayer(player);
 }
 }
 String msg = ServerProtectorManager.getMessage("broadcasts.passed", s -> s.replace("%player%", player.getName()).replace("%ip%", Utils.getIp(player)));
 if (config.getBoolean("message-settings.enable-broadcasts")) {
 Bukkit.broadcast(msg, "serverprotector.admin");
 }
 if (config.getBoolean("message-settings.enable-console-broadcasts")) {
 Bukkit.getConsoleSender().sendMessage(msg);
 }
 }
}
