package lz.izmoqwy.island.listeners;

import lz.izmoqwy.island.grid.GridManager;
import org.bukkit.block.Biome;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class ChunkGenerationListener implements Listener {

	@EventHandler
	public void onNewChunk(ChunkLoadEvent event) {
		if (event.isNewChunk() && event.getWorld() == GridManager.getWorld()) {
			for (int x = 0; x < 16; x++) {
				for (int z = 0; z < 16; z++) {
					event.getChunk().getBlock(x, event.getChunk().getWorld().getHighestBlockYAt(x, z), z).setBiome(Biome.PLAINS);
				}
			}
		}
	}

}
