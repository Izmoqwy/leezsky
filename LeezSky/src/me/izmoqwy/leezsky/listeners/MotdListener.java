package me.izmoqwy.leezsky.listeners;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.map.MinecraftFont;

public class MotdListener implements Listener {

	private final String fullMotd;

	public MotdListener() {
		String line1 = "§eLeezsky §f§l═§3 Skyblock";
		String line2 = "§cLe serveur n'est pas encore ouvert";

		this.fullMotd = centerMOTD(line1, line1) + "\n" + centerMOTD("A_" + line2, "§f▚ " + line2);
	}

	@EventHandler
	public void onPing(ServerListPingEvent event) {
		event.setMaxPlayers(Bukkit.getOnlinePlayers().size() + 3);
		event.setMotd(fullMotd);
	}

	private String centerMOTD(String width, String text) {
		int spaces = (int) Math.floor((241 - MinecraftFont.Font.getWidth(ChatColor.stripColor(width))) / 8d);
		return StringUtils.repeat(" ", spaces) + text;
	}

}
