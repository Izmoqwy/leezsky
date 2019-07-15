package lz.izmoqwy.core.hooks.worldedit;

import lz.izmoqwy.core.hooks.interfaces.WorldEditHook;
import lz.izmoqwy.core.objects.Cuboid;
import net.minecraft.server.v1_12_R1.NBTCompressedStreamTools;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class DefaultHook implements WorldEditHook {

	public Collection<Chunk> getChunksAround(Location location, int off) {
		List<Integer> offset = new ArrayList<>();
		offset.add(0);

		for (int i = off; i > 0; i--) {
			offset.add(i);
			offset.add(-i);
		}

		World world = location.getWorld();
		int baseX = location.getChunk().getX();
		int baseZ = location.getChunk().getZ();

		Collection<Chunk> chunksAroundPlayer = new HashSet<>();
		for (int x : offset) {
			for (int z : offset) {
				Chunk chunk = world.getChunkAt(baseX + x, baseZ + z);
				chunksAroundPlayer.add(chunk);
			}
		}
		return chunksAroundPlayer;
	}

	@Override
	public boolean loadSchematic(File file, Location loc) {
		final int locY = loc.getBlockY();
		try {
			FileInputStream fis = new FileInputStream(file);
			Object nbtData = NBTCompressedStreamTools.a(fis);
			Method getShort = nbtData.getClass().getMethod("getShort", String.class);
			Method getByteArray = nbtData.getClass().getMethod("getByteArray", String.class);

			short width = ((short) getShort.invoke(nbtData, "Width"));
			short height = ((short) getShort.invoke(nbtData, "Height"));
			short length = ((short) getShort.invoke(nbtData, "Length"));

			byte[] blocks = ((byte[]) getByteArray.invoke(nbtData, "Blocks"));
			byte[] data = ((byte[]) getByteArray.invoke(nbtData, "Data"));

			fis.close();
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					for (int z = 0; z < length; z++) {
						int index = y * width * length + z * width + x;
						int b = blocks[index] & 0xFF;
						Material m = Material.getMaterial(b);
						if (m != Material.AIR) {
							Block block = new Location(loc.getWorld(), loc.getBlockX() - ((int) (width / 2)) + x, locY + y, loc.getBlockZ() - ((int) (length / 2)) + z).getBlock();
							block.setTypeIdAndData(m.getId(), data[index], true);
						}
					}
				}
			}
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean setBiome(Cuboid cuboid, Biome biome) {
		return false;
	}

	@Override
	public boolean setBiome(Location lowerNE, Location upperSW, Biome biome) {
		return false;
	}

}
