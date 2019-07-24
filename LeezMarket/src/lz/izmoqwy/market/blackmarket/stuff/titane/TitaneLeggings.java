package lz.izmoqwy.market.blackmarket.stuff.titane;

import lombok.Getter;
import lz.izmoqwy.core.utils.ItemUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class TitaneLeggings extends TitaneStuffBase {

	@Getter
	private ItemStack item;

	public TitaneLeggings(long cost) {
		super(cost);

		this.item = ItemUtil.createItem(Material.IRON_LEGGINGS, prefix() + "Jambières en titane", defaultEnchants, ItemFlag.HIDE_ATTRIBUTES);
	}

}
