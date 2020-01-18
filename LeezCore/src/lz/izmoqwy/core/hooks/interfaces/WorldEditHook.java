package lz.izmoqwy.core.hooks.interfaces;

import lz.izmoqwy.core.world.Cuboid;
import org.bukkit.Location;
import org.bukkit.block.Biome;

import java.io.File;

public interface WorldEditHook {

	boolean loadSchematic(File file, Location location);

	boolean setBiome(Cuboid cuboid, Biome biome);
	boolean setBiome(Location lowerNE, Location upperSW, Biome biome);

}
