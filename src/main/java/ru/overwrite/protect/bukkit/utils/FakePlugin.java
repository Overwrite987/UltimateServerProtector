package ru.overwrite.protect.bukkit.utils;

import com.destroystokyo.paper.utils.PaperPluginLogger;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Random;

@UtilityClass
public class FakePlugin extends JavaPlugin {

    public static Plugin createFakePlugin() {
        try {
            Unsafe unsafe = getUnsafe();
            FakePlugin plugin = (FakePlugin) unsafe.allocateInstance(FakePlugin.class);

            Class<JavaPlugin> javaPluginClass = JavaPlugin.class;

            Field isEnabledField = javaPluginClass.getDeclaredField("isEnabled");
            isEnabledField.setAccessible(true);
            isEnabledField.set(plugin, true);

            Field loaderField = javaPluginClass.getDeclaredField("loader");
            loaderField.setAccessible(true);
            loaderField.set(plugin, new JavaPluginLoader(Bukkit.getServer()));

            Field serverField = javaPluginClass.getDeclaredField("server");
            serverField.setAccessible(true);
            serverField.set(plugin, Bukkit.getServer());

            PluginDescriptionFile description = new PluginDescriptionFile(generateName(), "1.0", "a");

            Field descriptionField = javaPluginClass.getDeclaredField("description");
            descriptionField.setAccessible(true);
            descriptionField.set(plugin, description);

            Field classLoaderField = javaPluginClass.getDeclaredField("classLoader");
            classLoaderField.setAccessible(true);
            classLoaderField.set(plugin, new ClassLoader() {
            });

            Field loggerField = javaPluginClass.getDeclaredField("logger");
            loggerField.setAccessible(true);
            loggerField.set(plugin, PaperPluginLogger.getLogger(description));

            return plugin;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Unsafe getUnsafe() throws ReflectiveOperationException {
        final Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        return (Unsafe) theUnsafe.get(null);
    }

    private static final Random random = new Random();
    private static final char[] CHARS = {
            'a','b','c','d','e','f','g','h','i','j','k','l','m',
            'n','o','p','q','r','s','t','u','v','w','x','y','z',
            'A','B','C','D','E','F','G','H','I','J','K','L','M',
            'N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
            '1','2','3','4','5','6','7','8','9','0'
    };

    public static String generateName() {
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            sb.append(CHARS[random.nextInt(CHARS.length)]);
        }
        return sb.toString();
    }
}