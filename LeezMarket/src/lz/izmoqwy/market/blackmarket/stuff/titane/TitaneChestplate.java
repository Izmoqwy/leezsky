package lz.izmoqwy.market.blackmarket.stuff.titane;

import lombok.Getter;
import lz.izmoqwy.core.utils.ItemUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class TitaneChestplate extends TitaneStuffBase {

	@Getter
	private ItemStack item;

	public TitaneChestplate(int cost) {
		super(cost);

		this.item = ItemUtil.createItem(Material.IRON_CHESTPLATE, prefix() + "Plastron en titane", defaultEnchants, ItemFlag.HIDE_ATTRIBUTES);
	}

}
