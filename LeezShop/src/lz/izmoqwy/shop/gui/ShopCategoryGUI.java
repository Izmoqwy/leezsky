package lz.izmoqwy.shop.gui;

import lz.izmoqwy.core.api.ItemBuilder;
import lz.izmoqwy.core.gui.MinecraftGUIListener;
import lz.izmoqwy.core.gui.UniqueMinecraftGUI;
import lz.izmoqwy.shop.LeezShop;
import lz.izmoqwy.shop.ShopManager;
import lz.izmoqwy.shop.obj.ShopCategory;
import lz.izmoqwy.shop.obj.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ShopCategoryGUI extends UniqueMinecraftGUI implements MinecraftGUIListener {

	private ShopCategory category;
	private short page;

	public ShopCategoryGUI(ShopMainGUI parent, Player player, ShopCategory category, short page) {
		super(parent, "§6Shop §8» §e" + ChatColor.stripColor(category.getDisplayName()), player);
		this.category = category;
		this.page = page;

		int pageOffset = (page - 1) * 36;
		int limit = Math.min(36, category.getItems().size() - pageOffset);
		if (limit > 0) {
			for (int i = 0; i < limit; i++) {
				ShopItem shopItem = category.getItems().get(pageOffset + i);
				setItem(i, new ItemBuilder(shopItem.getItemStack())
						.name(ChatColor.YELLOW + shopItem.getName())
						.appendLore(ShopManager.get.getShopItemLore(shopItem))
						.toItemStack(), true);
			}
		}

		ShopManager.get.navPreset(this);
		int index = (getRows() - 1) * 9;
		if (page > 1) {
			setItem(index + 3, new ItemBuilder(Material.PAPER)
					.name(ChatColor.GOLD + "Page précédente")
					.appendLore(ChatColor.GRAY + "Retourner à la page " + ChatColor.YELLOW + (page - 1))
					.toItemStack());
		}
		setItem(index + 4, category.getIcon());
		if (page < category.getPages()) {
			setItem(index + 5, new ItemBuilder(Material.PAPER)
					.name(ChatColor.GOLD + "Page suivante")
					.appendLore(ChatColor.GRAY + "Aller à la page " + ChatColor.YELLOW + (page + 1))
					.toItemStack());
		}

		addListener(this);
	}

	@Override
	public void onRichClick(Player player, ItemStack clickedItem, int slot, InventoryClickEvent event) {
		if (clickedItem.getType() == Material.AIR)
			return;

		if (slot < (getRows() - 2) * 9) {
			ShopItem shopItem = category.getItems().get((page - 1) * 36 + slot);
			if (event.getClick() == ClickType.LEFT) {
				if (!shopItem.isBuyable()) {
					player.sendMessage(LeezShop.PREFIX + "§cCet objet ne s'achète pas.");
					return;
				}

				new ShopItemGUI(this, shopItem, true, player).open();
			}
			else if (event.getClick() == ClickType.RIGHT) {
				if (!shopItem.isSellable()) {
					player.sendMessage(LeezShop.PREFIX + "§cCet objet ne se vend pas.");
					return;
				}

				new ShopItemGUI(this, shopItem, false, player).open();
			}
		}
		else if (clickedItem.getType() == Material.PAPER) {
			short newPage = (short) (slot % 9 == 4 ? page - 1 : page + 1);
			new ShopCategoryGUI((ShopMainGUI) getParent(), player, category, newPage).open();
		}
	}

}
