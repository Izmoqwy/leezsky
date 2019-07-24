package lz.izmoqwy.market.blackmarket.stuff.titane;

import lombok.Getter;
import lz.izmoqwy.core.utils.ItemUtil;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class TitaneBoots extends TitaneStuffBase {

	@Getter
	private ItemStack item;

	public TitaneBoots(int cost) {
		super(cost);

		Map<Enchantment, Integer> enchantments = defaultEnchants;
		enchantments.put(Enchantment.DEPTH_STRIDER, 3);
		enchantments.put(Enchantment.WATER_WORKER, 3);
		this.item = ItemUtil.createItem(Material.IRON_BOOTS, prefix() + "Bottes en titane", enchantments, ItemFlag.HIDE_ATTRIBUTES);
	}

}
