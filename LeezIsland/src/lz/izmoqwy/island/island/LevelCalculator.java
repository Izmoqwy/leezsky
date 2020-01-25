package lz.izmoqwy.island.island;

import lz.izmoqwy.core.api.database.exceptions.SQLActionImpossibleException;
import lz.izmoqwy.island.Storage;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bukkit.Material.COBBLESTONE;

public class LevelCalculator {

	private static final Map<Material, Integer> blockValues = new HashMap<Material, Integer>() {{
		put(COBBLESTONE, 1);
	}};

	public static long calculateExperience(List<Chunk> chunks) {
		long experience = 0;

		for (Chunk chunk : chunks) {
			int chunkX = chunk.getX() << 4, chunkZ = chunk.getZ() << 4;
			for (int x = chunkX; x < chunkX + 16; x++) {
				for (int z = chunkZ; z < chunkZ + 16; z++) {
					for (int y = 0; y < 256; y++) {
						final Block block = chunk.getBlock(x, y, z);
						if (block == null || block.getType() == Material.AIR)
							continue;

						experience += blockValues.getOrDefault(block.getType(), 0);
					}
				}
			}
		}

		return experience;
	}

	public static long update(Island island, long experience) {
		int level = (int) Math.floor(experience / 100d);

		island.setLevel(level);
		try {
			Storage.ISLANDS.setInt("level", level, "island_id", island.ID);
		}
		catch (SQLActionImpossibleException e) {
			e.printStackTrace();
		}

		return experience % 100;
	}

	public static int getRank(Island island) {
		int position = -1;
		try {
			PreparedStatement preparedStatement =
					Storage.DB.prepare("SELECT position FROM ( SELECT ROW_NUMBER() OVER ( ORDER BY level DESC ) AS position, island_id FROM " + Storage.ISLANDS + " ) WHERE island_id = ?");
			preparedStatement.setString(1, island.ID);

			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next())
				position = resultSet.getInt("position");

			preparedStatement.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return position;
	}

}
