package me.izmoqwy.hardpermissions.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import me.izmoqwy.hardpermissions.Configs;
import me.izmoqwy.hardpermissions.Group;

public class DefaultChat implements Listener {

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onAsyncChat(AsyncPlayerChatEvent event) {

		Player player = event.getPlayer();
		Group group = Configs.getPlayerGroupOrDefault(player);
		player.setDisplayName(ChatColor.translateAlternateColorCodes('&', group.getPrefix() + player.getName() + group.getSuffix() + ChatColor.RESET));

		event.setFormat("%1$s§8:§r <color>%2$s"
				.replace("<color>", group.getChatcolor()));

	}

}
