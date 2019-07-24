package lz.izmoqwy.market.blackmarket.stuff.titane;

import lombok.Getter;
import lz.izmoqwy.core.utils.ItemUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class TitaneShovel extends TitaneStuffBase {

	@Getter
	private ItemStack item;

	public TitaneShovel(long cost) {
		super(cost);

		this.item = ItemUtil.createItem(Material.IRON_SPADE, prefix() + "Pelle en titane", defaultEnchants, ItemFlag.HIDE_ATTRIBUTES);
	}

}
