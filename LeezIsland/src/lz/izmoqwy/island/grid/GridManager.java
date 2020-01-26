package lz.izmoqwy.island.grid;

import com.google.common.collect.Maps;
import lombok.Getter;
import lz.izmoqwy.core.api.database.exceptions.SQLActionImpossibleException;
import lz.izmoqwy.core.world.VoidChunkGenerator;
import lz.izmoqwy.island.LeezIsland;
import lz.izmoqwy.island.Storage;
import lz.izmoqwy.island.island.Island;
import lz.izmoqwy.island.players.Wrapper;
import org.bukkit.*;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class GridManager {

	@Getter
	private static World world;
	private static String worldName;

	private static Grid grid;
	private static TreeMap<Integer, TreeMap<Integer, String>> islandsGrid = Maps.newTreeMap();

	public static void load() {
		try {
			if (Storage.SETTINGS.hasResult("valueString", "setting_name", "Grid")) {
				String parsedGrid = Storage.SETTINGS.getString("valueString", "setting_name", "Grid");
				String[] splitted = parsedGrid.split(Pattern.quote("|")), coords = splitted[2].split(Pattern.quote(":"));
				grid = new Grid(Integer.parseInt(splitted[0]), Integer.parseInt(splitted[1]), Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));
			}
		}
		catch (SQLActionImpossibleException e) {
			e.printStackTrace();
		}

		world = Bukkit.getWorld(LeezIsland.WORLD_NAME);
		if (world == null && (!new File(Bukkit.getWorldContainer(), LeezIsland.WORLD_NAME + "/").exists())) {
			if (grid != null) {
				System.err.println("[LeezIsland] Le monde \"" + LeezIsland.WORLD_NAME + "\" n\u0027est pas pr\u00E9sent sur le serveur alors que la grille poss\u00E8de des donn\u00E9es.. "
						+ "Pour \u00E9viter toute corruption, sauvegardez le fichier \"plugins/LeezIsland/storage.db\" et supprimez le du serveur.");
			}
			else {
				Bukkit.createWorld(new WorldCreator(LeezIsland.WORLD_NAME).generator(new VoidChunkGenerator())
						.generateStructures(false).type(WorldType.NORMAL).environment(World.Environment.NORMAL));
				world = Bukkit.getWorld(LeezIsland.WORLD_NAME);
			}
		}
		else if (world == null) {
			Bukkit.createWorld(new WorldCreator(LeezIsland.WORLD_NAME).generator(new VoidChunkGenerator())
					.generateStructures(false).type(WorldType.NORMAL).environment(World.Environment.NORMAL));
			world = Bukkit.getWorld(LeezIsland.WORLD_NAME);
		}
		worldName = world.getName();

		if (grid == null)
			grid = new Grid(0, 0, 0, 0);

		IslandManager.load();
	}

	public static void loadAllIslands() throws SQLException {
		PreparedStatement preparedStatement = Storage.DB.prepare("SELECT island_id, general FROM " + Storage.ISLANDS + " WHERE 1");
		ResultSet resultSet = preparedStatement.executeQuery();

		while (resultSet.next()) {
			String[] general = resultSet.getString("general").split(Pattern.quote("|"));
			addToGrid(resultSet.getString("island_id"),
					Double.parseDouble(general[1]), Double.parseDouble(general[2]));
		}

		resultSet.close();
	}

	protected static Map.Entry<Integer, Integer> next() {
		return grid.next();
	}

	public static String getCurrentID() {
		return grid.getCurrentID();
	}

	protected static void saveGrid() {
		try {
			if (Storage.SETTINGS.hasResult("valueString", "setting_name", "Grid")) {
				Storage.SETTINGS.setString("valueString", grid.toString(), "setting_name", "Grid");
			}
			else {
				Storage.DB.execute("INSERT INTO " + Storage.SETTINGS + "(setting_name, valueString) VALUES (\"Grid\", \"" + grid.toString() + "\")");
			}
		}
		catch (SQLActionImpossibleException | SQLException e) {
			e.printStackTrace();
		}
	}

	public static Entry<Integer, Integer> getMiddle(int x, int z) {
		return grid.getMiddle(x, z);
	}

	public static boolean notOnGrid(World world) {
		return world.getName().compareTo(worldName) > 0;
	}

	protected static void addToGrid(Island newIsland) {
		addToGrid(newIsland.ID, newIsland.getMiddleX(), newIsland.getMiddleZ());
	}

	private static void addToGrid(String ID, double middleX, double middleZ) {
		LoadIsland island = new LoadIsland(middleX, middleZ);
		TreeMap<Integer, String> zEntry;

		final int lowerX = (int) island.getLowerX();
		if (islandsGrid.containsKey(lowerX)) {
			zEntry = islandsGrid.get(lowerX);
		}
		else
			zEntry = new TreeMap<>();

		zEntry.put((int) island.getLowerZ(), ID);
		islandsGrid.put(lowerX, zEntry);
	}

	protected static void removeFromGrid(Island island) {
		removeFromGrid(island.getMiddleX(), island.getMiddleZ());
	}

	private static void removeFromGrid(double middleX, double middleZ) {
		LoadIsland island = new LoadIsland(middleX, middleZ);
		TreeMap<Integer, String> zEntry;

		final int lowerX = (int) island.getLowerX();
		if (islandsGrid.containsKey(lowerX)) {
			zEntry = islandsGrid.get(lowerX);
		}
		else
			zEntry = new TreeMap<>();

		zEntry.remove((int) island.getLowerZ());
		if (zEntry.isEmpty())
			islandsGrid.remove(lowerX);
		else
			islandsGrid.replace(lowerX, zEntry);
	}

	public static Island getIslandAt(Location location) {
		if (location == null) return null;
		if (!world.equals(location.getWorld())) return null;

		return getIslandAt(location.getBlockX(), location.getBlockZ());
	}

	// get island at location without checking if the location is valid
	public static Island getIslandAtSafe(Location location) {
		return getIslandAt(location.getBlockX(), location.getBlockZ());
	}

	public static Island getIslandAt(int x, int z) {
		Entry<Integer, TreeMap<Integer, String>> entry = islandsGrid.floorEntry(x);
		if (entry != null) {
			Entry<Integer, String> entryZ = entry.getValue().floorEntry(z);
			if (entryZ != null) {
				Island island;
				try {
					island = Wrapper.wrapIsland(entryZ.getValue());
				}
				catch (SQLActionImpossibleException e) {
					e.printStackTrace();
					System.err.println("[LeezIsland] Impossible de charger l'île avec l'ID: " + entryZ.getValue());
					return null;
				}
				if (island != null && island.isInBounds(x, z))
					return island;
			}
		}
		return null;
	}

}
