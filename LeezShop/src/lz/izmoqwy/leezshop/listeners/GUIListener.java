package lz.izmoqwy.leezshop.listeners;

import lz.izmoqwy.core.Economy;
import lz.izmoqwy.core.utils.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import lz.izmoqwy.leezshop.LeezShop;
import lz.izmoqwy.leezshop.ShopLoader;
import lz.izmoqwy.leezshop.obj.ShopItem;

public class GUIListener implements Listener {

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onGUI(final InventoryClickEvent event) {

		if (event.getInventory() == null ||
				event.getInventory().getName() == null ||
				!event.getInventory().getName().startsWith("§6Shop")) return;

		event.setCancelled(true);

		Inventory inventory = event.getInventory();
		if (inventory == null || inventory != event.getClickedInventory()) return;

		Player player = (Player) event.getWhoClicked();
		if (player == null) return;
		ItemStack clicked = event.getCurrentItem();
		if (clicked == null || clicked.getType() == Material.AIR) return;

		if (clicked.isSimilar(ShopLoader.closeBtn)) {
			player.closeInventory();
			return;
		}

		String title = event.getInventory().getName();
		int slot = event.getSlot();
		ClickType click = event.getClick();
		if (title.equalsIgnoreCase("§6Shop")) {
			/*
			 * Dans le menu
			 */
			Inventory section = ShopLoader.loadSection(slot);
			if (section != null)
				player.openInventory(section);
		}

		else if (title.equalsIgnoreCase("§6Shop §8» §aAchat")) {
			/*
			 * Menu d'achat d'un item -> Slot 13
			 */

			ShopItem item = ShopLoader.getItem(inventory.getItem(13).getData());
			if (item == null) {
				int s_ = inventory.getSize() - 5;
				String sec_ = ChatColor.stripColor(inventory.getItem(s_).getItemMeta().getDisplayName());
				if (ShopLoader.specials.containsKey(sec_)) {
					int slot_ = Integer.parseInt(inventory.getItem(s_).getItemMeta().getLore().get(inventory.getItem(s_).getItemMeta().getLore().size() - 1).replace("§8Item: §7", ""));
					item = ShopLoader.specials.get(sec_).get(slot_);
				}
				if (item == null) return;
			}

			if (clicked.isSimilar(ShopLoader.backBtn)) {
				player.openInventory(ShopLoader.loadSection(item.parent.getSlot()));
				return;
			}

			if (slot < inventory.getSize() - 18) {

				if (item.isBuyable()) {
					String tlt = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
					int amt = tlt.endsWith("stacks") ? Integer.parseInt(tlt.replace("stacks", "").trim()) * 64 : Integer.parseInt(tlt);
					double price = Economy.round(amt * item.getBuyPrice());
					if (!Economy.withdraw(player, price)) {
						player.sendMessage(LeezShop.PREFIX + "§cVous n'avez pas assez d'argent.");
						return;
					}

					ItemStack toGive = ItemUtil.createItem(item.getData(), amt);
					String idf = toGive.getData().getData() == 0 ? "" : "(" + toGive.getData().getData() + ")";

					if (!item.getCommands().isEmpty()) {
						for (String cmd : item.getCommands()) {
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{VAR_player}", player.getName()));
						}
					}
					else {
						if (!ItemUtil.giveItems(player, toGive)) {
							player.sendMessage(LeezShop.PREFIX + "§cVotre inventaire est saturé, certains objets sont tombés par terre.");
						}
					}
					player.sendMessage(LeezShop.PREFIX + "§2Vous avez §lacheté§r §a" + amt + " " + toGive.getType().name() + idf + " §2pour: §e" + price + "$§2.");
				}
				else {
					player.sendMessage(LeezShop.PREFIX + "§cCet objet ne s'achète pas.");
				}
			}
		}

		else if (title.equalsIgnoreCase("§6Shop §8» §aVente")) {
			/*
			 * Menu de vente d'un item -> Slot 13
			 */

			ShopItem item = ShopLoader.getItem(inventory.getItem(13).getData());
			if (item == null) {
				int s_ = inventory.getSize() - 5;
				String sec_ = ChatColor.stripColor(inventory.getItem(s_).getItemMeta().getDisplayName());
				if (ShopLoader.specials.containsKey(sec_)) {
					int slot_ = Integer.parseInt(inventory.getItem(s_).getItemMeta().getLore().get(inventory.getItem(s_).getItemMeta().getLore().size() - 1).replace("§8Item: §7", ""));
					item = ShopLoader.specials.get(sec_).get(slot_);
				}
				if (item == null) return;
			}

			if (slot < inventory.getSize() - 18) {

				if (item.isSellable() || slot == 22) {
					String tlt = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
					int amt;
					if (slot == 22) {
						amt = ItemUtil.getAmountOf(ItemUtil.createItem(item.getData()), player);
						if (amt <= 0) {
							player.sendMessage(LeezShop.PREFIX + "§cVous n'avez pas cet objet dans votre inventaire.");
							return;
						}
					}
					else
						amt = tlt.endsWith("stacks") ? Integer.parseInt(tlt.replace("stacks", "").trim()) * 64 : Integer.parseInt(tlt);
					double price = Economy.round(amt * item.getSellPrice());

					ItemStack toRevoke = ItemUtil.createItem(item.getData(), amt);
					String idf = toRevoke.getData().getData() == 0 ? "" : "(" + toRevoke.getData().getData() + ")";

					if (!player.getInventory().containsAtLeast(toRevoke, amt)) {
						player.sendMessage(LeezShop.PREFIX + "§cVous n'avez pas §4" + amt + " " + toRevoke.getType().name() + idf);
						return;
					}

					player.getInventory().removeItem(toRevoke);
					Economy.deposit(player, price);
					player.sendMessage(LeezShop.PREFIX + "§2Vous avez §lvendu§r §a" + amt + " " + toRevoke.getType().name() + idf + " §2pour: §e" + price + "$§2.");
				}
				else {
					player.sendMessage(LeezShop.PREFIX + "§cCet objet ne se vend pas.");
				}
			}
		}

		else if (title.startsWith("§6Shop §8»")) {
			/*
			 * Dans une catégorie
			 */
			if (clicked.isSimilar(ShopLoader.backBtn)) {
				player.openInventory(ShopLoader.loadInventory());
				return;
			}
			if (slot < 36) {
				ShopItem item = ShopLoader.getItem(clicked.getData());
				if (item == null) {
					title = ChatColor.stripColor(title.replaceFirst("§6Shop §8» ", ""));
					if (ShopLoader.specials.containsKey(title)) {
						item = ShopLoader.specials.get(title).get(slot);
					}
					if (item == null) return;
				}

				if (click == ClickType.LEFT) {
					if (item.isBuyable()) {
						player.openInventory(ShopLoader.loadBuyInventory(item, slot));
					}
					else {
						player.sendMessage(LeezShop.PREFIX + "§cCet objet ne s'achète pas.");
					}
				}
				else if (click == ClickType.RIGHT) {
					if (item.isSellable()) {
						player.openInventory(ShopLoader.loadSellInventory(item, player, slot));
					}
					else {
						player.sendMessage(LeezShop.PREFIX + "§cCet objet ne se vend pas.");
					}
				}
			}

		}

	}

}
