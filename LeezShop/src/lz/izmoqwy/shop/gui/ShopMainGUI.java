package lz.izmoqwy.shop.gui;

import lz.izmoqwy.core.gui.MinecraftGUIListener;
import lz.izmoqwy.core.gui.UniqueMinecraftGUI;
import lz.izmoqwy.shop.ShopManager;
import lz.izmoqwy.shop.obj.ShopCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ShopMainGUI extends UniqueMinecraftGUI implements MinecraftGUIListener {

	public ShopMainGUI(Player player) {
		super(null, "§6Shop", player);

		setRows(ShopManager.get.getMainGuiRows());
		for (ShopCategory category : ShopManager.get.getCategories()) {
			setItem(category.getDisplaySlot(), category.getIcon());
		}

		addListener(this);
	}

	@Override
	public void onClick(Player player, ItemStack clickedItem, int slot) {
		new ShopCategoryGUI(this, player, (ShopCategory) ShopManager.get.getCategories().stream().filter(category -> category.getDisplaySlot() == slot).toArray()[0], (short) 1)
				.open();
	}

}
