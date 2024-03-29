package lz.izmoqwy.island;

import lz.izmoqwy.core.nms.NMS;
import lz.izmoqwy.island.grid.GridManager;
import lz.izmoqwy.island.island.Island;
import lz.izmoqwy.island.players.SkyblockPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class BorderAPI {

	public static void setOwnBorder(SkyblockPlayer player) {
		setBorder(player.bukkit(), player.getIsland());
	}

	public static void setBorder(Player player, Island island) {
		NMS.packet.setFakeBorder(player, island.getRange() * 2 + 1, new Location(GridManager.getWorld(), island.getMiddleX() + .5, 0, island.getMiddleZ() + .5));
	}

}
