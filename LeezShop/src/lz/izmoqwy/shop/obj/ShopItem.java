package lz.izmoqwy.shop.obj;

import lombok.Builder;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Getter
@Builder
public class ShopItem {

	private String name;
	private ItemStack itemStack;
	private List<String> commands;
	private double buyPrice, sellPrice;

	public boolean isBuyable() {
		return buyPrice > 0;
	}

	public boolean isSellable() {
		return sellPrice > 0;
	}

}
