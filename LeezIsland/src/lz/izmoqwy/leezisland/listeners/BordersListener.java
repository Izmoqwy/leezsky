/*
 * That file is a part of [Leezsky] LeezIsland
 * Copyright Izmoqwy
 * You can edit for your personal use.
 * You're not allowed to redistribute it at your own.
 */

package lz.izmoqwy.leezisland.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import lz.izmoqwy.leezisland.BorderAPI;
import lz.izmoqwy.leezisland.LeezIsland;
import lz.izmoqwy.leezisland.grid.GridManager;
import lz.izmoqwy.leezisland.island.Island;

public class BordersListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onTeleport(PlayerTeleportEvent event) {
		if (event.getTo().getWorld() != GridManager.getWorld())
			return;

		Island island = GridManager.getIslandAt(event.getTo());
		if (island != null) {
			new BukkitRunnable() {
				@Override
				public void run() {
					BorderAPI.setBorder(event.getPlayer(), island);
				}
			}.runTaskLater(LeezIsland.getInstance(), 5);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onConnect(PlayerJoinEvent event) {
		if (event.getPlayer().getLocation().getWorld() != GridManager.getWorld())
			return;

		Island island = GridManager.getIslandAt(event.getPlayer().getLocation());
		if (island != null) {
			new BukkitRunnable() {
				@Override
				public void run() {
					BorderAPI.setBorder(event.getPlayer(), island);
				}
			}.runTaskLater(LeezIsland.getInstance(), 10);
		}
	}

}
