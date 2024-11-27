package ru.overwrite.protect.bukkit.utils.color;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class MiniMessageColorizer implements Colorizer {

    @Override
    public String colorize(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        Component component = MiniMessage.miniMessage().deserialize(message);
        return LegacyComponentSerializer.legacySection().serialize(component);
    }
}
