/*
 * That file is a part of [Leezsky] LeezIsland
 * Copyright Izmoqwy
 * You can edit for your personal use.
 * You're not allowed to redistribute it at your own.
 */

package lz.izmoqwy.island.listeners;

import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import lz.izmoqwy.island.grid.GridManager;

public class GenerationListener implements Listener {

	@EventHandler
	public void onNewChunk(ChunkLoadEvent event) {
		if (event.isNewChunk() && event.getWorld() == GridManager.getWorld()) {
			for (int x = 0; x < 16; x++) {
				for (int z = 0; z < 16; z++) {
					final Block block = event.getChunk().getBlock(x, event.getChunk().getWorld().getHighestBlockYAt(x, z), z);
					block.setBiome(Biome.PLAINS);
				}
			}
		}
	}

}
