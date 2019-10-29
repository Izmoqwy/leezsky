package lz.izmoqwy.leezisland;

import lz.izmoqwy.core.nms.NmsAPI;
import lz.izmoqwy.leezisland.grid.GridManager;
import lz.izmoqwy.leezisland.island.Island;
import lz.izmoqwy.leezisland.players.SkyblockPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class BorderAPI {
	
	public static void setOwnBorder(SkyblockPlayer player) {
		setBorder(player.bukkit(), player.getIsland());
	}
	
	public static void setBorder(Player player, Island island) {
		NmsAPI.packet.setBorder(player, island.getRange() * 2 + 1, new Location(GridManager.getWorld(), island.getMiddleX() + .5, 0, island.getMiddleZ() + .5));
	}

}
