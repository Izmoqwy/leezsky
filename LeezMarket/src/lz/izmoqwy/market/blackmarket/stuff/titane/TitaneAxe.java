package lz.izmoqwy.market.blackmarket.stuff.titane;

import lombok.Getter;
import lz.izmoqwy.core.utils.ItemUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class TitaneAxe extends TitaneStuffBase {

	@Getter
	private ItemStack item;

	public TitaneAxe(long cost) {
		super(cost);

		this.item = ItemUtil.createItem(Material.IRON_AXE, prefix() + "Hache en titane", defaultEnchants, ItemFlag.HIDE_ATTRIBUTES);
	}

}
