package lz.izmoqwy.leezisland.listeners;

import lz.izmoqwy.leezisland.island.GeneralPermission;
import lz.izmoqwy.leezisland.grid.GridManager;
import lz.izmoqwy.leezisland.island.Island;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

import java.util.Random;

public class CobbleGeneratorListener implements Listener {

	private static final Random random = new Random();
	private static final BlockFace[] faces = new BlockFace[]
			{BlockFace.SELF, BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onFromTo(BlockFromToEvent event) {
		final int flowId = event.getBlock().getTypeId();
		if (flowId >= 8 && flowId <= 11) {
			Block block = event.getToBlock();
			if (block.getTypeId() == 0) {
				if (generatesCobble(flowId, block)) {
					Island island = GridManager.getIslandAt(block.getLocation());
					if (island == null || !island.hasGeneralPermission(GeneralPermission.GENENABLED))
						return;

					final double luck = random.nextInt(10000) / 100.D;

					/*
						Todo: Manage things with island-scoped percentages
					 */
					Material material = Material.AIR;
					if (luck < 50) {
						material = Material.STONE;
					}

					if (material == Material.AIR)
						material = Material.COBBLESTONE;

					block.setType(material);
				}
			}
		}
	}

	private boolean generatesCobble(int id, Block block) {
		int mirrorID1 = (id == 8 || id == 9 ? 10 : 8);
		int mirrorID2 = (id == 8 || id == 9 ? 11 : 9);
		for (BlockFace face : faces) {
			Block r = block.getRelative(face, 1);
			if (r.getTypeId() == mirrorID1 || r.getTypeId() == mirrorID2) {
				return true;
			}
		}
		return false;
	}

}
