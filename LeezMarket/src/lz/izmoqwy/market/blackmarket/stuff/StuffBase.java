package lz.izmoqwy.market.blackmarket.stuff;

import lombok.Getter;
import lz.izmoqwy.core.utils.TextUtil;
import lz.izmoqwy.market.rpg.RPGResource;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public abstract class StuffBase {

	@Getter
	private RPGResource costRes;
	@Getter
	private int cost;

	protected StuffBase(RPGResource costRes, int cost) {
		this.costRes = costRes;
		this.cost = cost;
	}

	public abstract String prefix();

	public ItemStack getPresentationItem() {
		ItemStack item = getItem().clone();
		ItemMeta itemMeta = item.getItemMeta();
		itemMeta.setLore(Arrays.asList("§2Il vous sera demander de confirmer", "§9» §3Prix: §r" + costRes.getFullName(TextUtil.readbleNumber(getCost()))));
		itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		item.setItemMeta(itemMeta);
		return item;
	}
	public abstract ItemStack getItem();

}
