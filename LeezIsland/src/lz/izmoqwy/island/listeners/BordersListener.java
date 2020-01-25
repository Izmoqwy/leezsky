package lz.izmoqwy.island.listeners;

import lz.izmoqwy.core.utils.ServerUtil;
import lz.izmoqwy.island.BorderAPI;
import lz.izmoqwy.island.LeezIsland;
import lz.izmoqwy.island.grid.GridManager;
import lz.izmoqwy.island.island.Island;
import lz.izmoqwy.island.players.SkyblockPlayer;
import lz.izmoqwy.island.players.Wrapper;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class BordersListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if (GridManager.notOnGrid(event.getTo().getWorld()))
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
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (GridManager.notOnGrid(event.getPlayer().getWorld()))
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
		else {
			SkyblockPlayer skyblockPlayer = Wrapper.wrapPlayer(event.getPlayer());
			if (skyblockPlayer != null) {
				if (skyblockPlayer.hasIsland()) {
					if (skyblockPlayer.hasIsland()) {
						skyblockPlayer.bukkit().teleport(skyblockPlayer.getIsland().getHomeLocation());
					}
					else
						ServerUtil.performCommand("spawn " + skyblockPlayer.bukkit().getName());
				}
				skyblockPlayer.sendMessage(ChatColor.RED + "Vous étiez dans un endroit inconnu lors de votre re-connexion, vous avez été téléporté ailleurs.");
			}
		}
	}

}
