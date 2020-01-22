package lz.izmoqwy.shop.obj;

import lombok.Getter;
import lz.izmoqwy.core.api.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.regex.Pattern;

@Getter
public class ShopCategory {

	private String displayName, description;
	private ItemStack icon;
	private List<ShopItem> items;

	private int displaySlot;
	private short pages;

	public ShopCategory(String displayName, String description, ItemStack icon, List<ShopItem> items, int displaySlot, boolean shine) {
		this.displayName = displayName;
		this.description = description;

		ItemBuilder iconBuilder = new ItemBuilder(icon)
				.name(ChatColor.YELLOW + displayName);
		if (description != null && !description.trim().isEmpty()) {
			iconBuilder.appendLore(ChatColor.GRAY, description.split(Pattern.quote("\n")));
		}
		if (shine)
			iconBuilder.quickEnchant();
		this.icon = iconBuilder.toItemStack();

		this.items = items;
		this.displaySlot = displaySlot;
		this.pages = (short) Math.ceil(items.size() / 36d);
	}

}
