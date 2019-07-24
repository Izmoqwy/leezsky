package lz.izmoqwy.market.blackmarket.stuff.titane;

import lombok.Getter;
import lz.izmoqwy.core.utils.ItemUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class TitanePickaxe extends TitaneStuffBase {

	@Getter
	private ItemStack item;

	public TitanePickaxe(long cost) {
		super(cost);

		this.item = ItemUtil.createItem(Material.IRON_PICKAXE, prefix() + "Pioche en titane", defaultEnchants, ItemFlag.HIDE_ATTRIBUTES);
	}

}
