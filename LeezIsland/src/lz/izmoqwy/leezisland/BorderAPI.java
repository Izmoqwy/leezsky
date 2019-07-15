package lz.izmoqwy.leezisland;

import lz.izmoqwy.core.nms.NmsAPI;
import lz.izmoqwy.leezisland.grid.GridManager;
import lz.izmoqwy.leezisland.island.Island;
import lz.izmoqwy.leezisland.players.SkyblockPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class BorderAPI {
	
	public static void setOwnBorder(SkyblockPlayer player) {
		NmsAPI.packet.setBorder(player.bukkit(), player.getIsland().getRange() * 2 + 1, new Location(GridManager.getWorld(), player.getIsland().getMiddleX(), 0, player.getIsland().getMiddleZ()));
	}
	
	public static void setBorder(Player player, Island island) {
		NmsAPI.packet.setBorder(player, island.getRange() * 2 + 1, new Location(GridManager.getWorld(), island.getMiddleX(), 0, island.getMiddleZ()));
	}

}
