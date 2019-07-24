package lz.izmoqwy.market.blackmarket.stuff.titane;

import lombok.Getter;
import lz.izmoqwy.core.utils.ItemUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class TitaneHelmet extends TitaneStuffBase {

	@Getter
	private ItemStack item;

	public TitaneHelmet(long cost) {
		super(cost);

		this.item = ItemUtil.createItem(Material.IRON_HELMET, prefix() + "Casque en titane", defaultEnchants, ItemFlag.HIDE_ATTRIBUTES);
	}

}
