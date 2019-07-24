package lz.izmoqwy.market.blackmarket;

import lz.izmoqwy.market.blackmarket.stuff.titane.*;
import org.bukkit.configuration.ConfigurationSection;

public class BMStuff {

	public static TitaneHelmet TITANE_HELMET;
	public static TitaneChestplate TITANE_CHESTPLATE;
	public static TitaneLeggings TITANE_LEGGINGS;
	public static TitaneBoots TITANE_BOOTS;
	public static TitaneSword TITANE_SWORD;
	public static TitaneShield TITANE_SHIELD;

	public static TitanePickaxe TITANE_PICKAXE;
	public static TitaneAxe TITANE_AXE;
	public static TitaneShovel TITANE_SHOVEL;

	protected static void init(ConfigurationSection prices) {
		// Try todo: adjust prices to the current EXR of each res

		ConfigurationSection titane = prices != null ? prices.getConfigurationSection("titane") : null;
		TITANE_HELMET = new TitaneHelmet(i(titane, "helmet"));
		TITANE_CHESTPLATE = new TitaneChestplate(i(titane, "chestplate"));
		TITANE_LEGGINGS = new TitaneLeggings(i(titane, "leggings"));
		TITANE_BOOTS = new TitaneBoots(i(titane, "boots"));
		TITANE_SWORD = new TitaneSword(i(titane, "sword"));
		TITANE_SHIELD = new TitaneShield(i(titane, "shield"));

		TITANE_PICKAXE = new TitanePickaxe(i(titane, "pickaxe"));
		TITANE_AXE = new TitaneAxe(i(titane, "axe"));
		TITANE_SHOVEL = new TitaneShovel(i(titane, "shovel"));
	}

	private static long i(ConfigurationSection section, String key) {
		if (section == null)
			return (long) 1e12;

		return (long) Double.parseDouble(section.getString(key, "1e12"));
	}

}
