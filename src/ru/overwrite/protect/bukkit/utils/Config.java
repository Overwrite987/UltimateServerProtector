package ru.overwrite.protect.bukkit.utils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.overwrite.protect.bukkit.ServerProtector;
import java.io.File;
import java.io.IOException;
public class Config { 
 private static final ServerProtector instance = ServerProtector.getInstance();
 public static FileConfiguration getFile(String fileName) {
 File file = new File(ServerProtector.getInstance().getDataFolder(), fileName);
 if (!file.exists()) {
 instance.saveResource(fileName, false);
 }
 return YamlConfiguration.loadConfiguration(file);
 } 
 public static FileConfiguration getFileFullPath(String fileName) {
 File file = new File(instance.getConfig().getString("file-settings.data-file-path"), fileName);
 if (!file.exists()) {
 instance.saveResource(fileName, false);
 }
 return YamlConfiguration.loadConfiguration(file);
 }
 public static void save(FileConfiguration config, String fileName) {
 Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
 try {
 config.save(new File(instance.getDataFolder(), fileName));
 } catch (IOException ex) {
 ex.printStackTrace();
 }
 });
 } 
 public static void saveFullPath(FileConfiguration config, String fileName) {
 Bukkit.getScheduler().runTaskAsynchronously(ServerProtector.getInstance(), () -> {
 try {
 config.save(new File(instance.getConfig().getString("file-settings.data-file-path"), fileName));
 } catch (IOException ex) {
 ex.printStackTrace();
 }
 });
 }
}
