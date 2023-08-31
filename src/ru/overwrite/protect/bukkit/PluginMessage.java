package ru.overwrite.protect.bukkit;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class PluginMessage implements PluginMessageListener {
	
	private final ServerProtectorManager instance;
	
	public PluginMessage(ServerProtectorManager plugin) {
        instance = plugin;
    }
	
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
	    if (!channel.equals("BungeeCord")) return;
	    ByteArrayDataInput input = ByteStreams.newDataInput(message);
	    String subchannel = input.readUTF();
	    if (subchannel.equalsIgnoreCase("serverprotector")) {
	    	for (Player ps : Bukkit.getOnlinePlayers()) {
    			if (ps.hasPermission("serverprotector.admin")) {
    				ps.sendMessage(input.readUTF());
    			}
	    	}
	    }
	}
	
	public void sendCrossProxy(Player player, String message) {
	    ByteArrayDataOutput out = ByteStreams.newDataOutput();
	    out.writeUTF("Forward");
	    out.writeUTF("ALL");
	    out.writeUTF("serverprotector");
	    out.writeUTF(message);
	    player.sendPluginMessage(instance, "BungeeCord", out.toByteArray());
	}

}
