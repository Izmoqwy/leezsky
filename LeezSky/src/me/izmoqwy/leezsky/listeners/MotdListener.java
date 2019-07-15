package me.izmoqwy.leezsky.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

public class MotdListener implements Listener {

	@EventHandler
	public void onPing(ServerListPingEvent event) {
		event.setMaxPlayers(Bukkit.getOnlinePlayers().size() + 3);
		event.setMotd("§6LeezSky §f§l - §bSkyblock\n§5➥ §dServeur actuellement en développement");
	}

}
