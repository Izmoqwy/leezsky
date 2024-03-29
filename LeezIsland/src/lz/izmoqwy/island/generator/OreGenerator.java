package lz.izmoqwy.island.generator;

import lz.izmoqwy.island.island.Island;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class OreGenerator {

	private static final Map<Material, Byte> DEFAULT_ORES = new HashMap<Material, Byte>() {
		{
			put(Material.COBBLESTONE, 55); // 32.4%
			put(Material.STONE, 45); // 26.5%
			put(Material.COAL_ORE, 30); // 17.6%
			put(Material.IRON_ORE, 20); // 11.8%
			put(Material.REDSTONE_ORE, 10); // 5.9%
			put(Material.LAPIS_ORE, 10); // 5.9%
		}

		private void put(Material material, int value) {
			super.put(material, (byte) value);
		}
	};

	private static final OreGeneratorSettings DEFAULT_SETTINGS = new OreGeneratorSettings(DEFAULT_ORES);

	private final BlockFace[] faces = new BlockFace[]
			{BlockFace.SELF, BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

	private final Random random = new Random();

	public Material randomize(OreGeneratorSettings settings) {
		final double luck = random.nextInt(10000) / 100.D;

		Material material = Material.COBBLESTONE;
		double sum = 0;
		for (Ore ore : settings.getOres()) {
			sum += ore.getChance();
			if (luck <= sum) {
				material = ore.getType();
				break;
			}
		}

		return material;
	}

	public OreGeneratorSettings fromIsland(Island island) {
		return DEFAULT_SETTINGS;
	}

	public Material randomize(Island island) {
		return randomize(fromIsland(island));
	}

	@SuppressWarnings("deprecation")
	public boolean shouldGenerates(Block block, int flowingId) {
		int mirrorID1 = (flowingId == 8 || flowingId == 9 ? 10 : 8);
		int mirrorID2 = (flowingId == 8 || flowingId == 9 ? 11 : 9);
		for (BlockFace face : faces) {
			Block r = block.getRelative(face, 1);
			if (r.getTypeId() == mirrorID1 || r.getTypeId() == mirrorID2) {
				return true;
			}
		}
		return false;
	}

}
