package lz.izmoqwy.market.blackmarket.stuff.titane;

import lz.izmoqwy.market.blackmarket.stuff.StuffBase;
import lz.izmoqwy.market.rpg.RPGResource;
import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.LinkedHashMap;

public abstract class TitaneStuffBase extends StuffBase {

	protected static final HashMap<Enchantment, Integer> defaultEnchants = new LinkedHashMap<Enchantment, Integer>() {
		{
			put(Enchantment.DURABILITY, 6);
		}
	};

	protected TitaneStuffBase(long cost) {
		super(RPGResource.TITANE, cost);
	}

	@Override
	public String prefix() {
		return "§7§l";
	}
}
