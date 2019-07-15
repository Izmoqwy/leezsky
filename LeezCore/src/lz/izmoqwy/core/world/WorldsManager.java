package lz.izmoqwy.core.world;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

public class WorldsManager {

	public static void registerPersistentVoidWorld(String worldName) {
		Bukkit.createWorld(new WorldCreator(worldName).generator(new VoidChunkGenerator())
				.generateStructures(false).type(WorldType.NORMAL).environment(World.Environment.NORMAL));
	}

}
