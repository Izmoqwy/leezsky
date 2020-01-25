package lz.izmoqwy.island.island;

import lz.izmoqwy.core.api.database.exceptions.SQLActionImpossibleException;
import lz.izmoqwy.island.Storage;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.material.MaterialData;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelCalculator {

	public static long calcXP(List<Chunk> chunks) {
		long xp = 0;
		for (Chunk chunk : chunks) {
			int cx = chunk.getX() << 4;
			int cz = chunk.getZ() << 4;
			for (int x = cx; x < cx + 16; x++) {
				for (int z = cz; z < cz + 16; z++) {
					for (int y = 0; y < 256; y++) {
						Block block = chunk.getBlock(x, y, z);
						MaterialData data = new MaterialData(block.getType(), block.getData());
						if (Values.blocks.containsKey(data))
							xp += Values.blocks.get(data);
					}
				}
			}
		}
		return xp;
	}

	public static long update(Island island, long xp) {
		int level = (int) Math.floor(xp / 100.D);

		island.setLevel(level);
		try {
			Storage.ISLANDS.setInt("level", level, "island_id", island.ID);
		}
		catch (SQLActionImpossibleException e) {
			e.printStackTrace();
		}

		return xp % 100;
	}

	/*
		Ne marchera que en 1.13 et + à cause de la version de sqlite
	 */
	public static int getPosition(Island island, String sql) {
		try {
			PreparedStatement statement = Storage.DB.prepare("SELECT position FROM ( SELECT ROW_NUMBER() OVER ( ORDER BY level DESC ) AS position, island_id FROM " + Storage.ISLANDS + " ) WHERE island_id = ?");
			statement.setString(1, island.ID);
			ResultSet rs = statement.executeQuery();

			int position = -1;
			if (rs.next()) {
				position = rs.getInt("position");
			}
			statement.close();
			return position;
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	static class Values {

		public static final Map<MaterialData, Integer> blocks;

		static {
			blocks = new HashMap<MaterialData, Integer>() {
				{
					/*
						Basic blocks
					 */
					put(md(Material.COBBLESTONE), 1);

					/*
						Ores
					 */

					/*
						Redstone
					 */

					/*
						Other blocks
					 */
				}
			};
		}

		private static MaterialData md(Material material) {
			return new MaterialData(material);
		}

		private static MaterialData md(Material material, short data) {
			return new MaterialData(material, (byte) data);
		}

	}

}
