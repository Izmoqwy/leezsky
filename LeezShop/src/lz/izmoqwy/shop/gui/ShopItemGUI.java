package lz.izmoqwy.shop.gui;

import lz.izmoqwy.core.Economy;
import lz.izmoqwy.core.api.ItemBuilder;
import lz.izmoqwy.core.gui.MinecraftGUIListener;
import lz.izmoqwy.core.gui.UniqueMinecraftGUI;
import lz.izmoqwy.core.utils.ItemUtil;
import lz.izmoqwy.core.utils.PlayerUtil;
import lz.izmoqwy.core.utils.ServerUtil;
import lz.izmoqwy.core.utils.TextUtil;
import lz.izmoqwy.shop.LeezShop;
import lz.izmoqwy.shop.ShopManager;
import lz.izmoqwy.shop.obj.ShopItem;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static org.bukkit.ChatColor.*;

public class ShopItemGUI extends UniqueMinecraftGUI implements MinecraftGUIListener {

	private ShopItem item;
	private boolean buy;

	public ShopItemGUI(ShopCategoryGUI parent, ShopItem item, boolean buy, Player player) {
		super(parent, "§6Shop §8» " + (buy ? GREEN + "Achat" : DARK_GREEN + "Vente"), player);
		this.item = item;
		this.buy = buy;

		if (buy) {
			switch (item.getItemStack().getMaxStackSize()) {
				case 1:
					setRows(3);
					item(13, 2, null);
					break;

				case 16:
					setRows(3);
					item(12, 1, null);
					item(13, 8, null);
					item(14, 16, null);
					break;

				default:
					setRows(4);
					item(11, 1, null);
					item(12, 8, null);
					item(13, 16, null);
					item(14, 32, null);
					item(15, 64, null);

					item(21, 128, "2 stacks");
					item(22, 256, "4 stacks");
					item(23, 512, "8 stacks");
					break;
			}
		}
		else {
			setRows(4);
			item(11, 1, null);
			item(12, 8, null);
			item(13, 16, null);
			item(14, 32, null);
			item(15, 64, null);

			int amount = PlayerUtil.getAmountInInventory(player, item.getItemStack());
			setItem(22, new ItemBuilder(SkullType.PLAYER)
					.name(GREEN + "Tout vendre")
					.setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDM3ODYyY2RjMTU5OTk4ZWQ2YjZmZGNjYWFhNDY3NTg2N2Q0NDg0ZGI1MTJhODRjMzY3ZmFiZjRjYWY2MCJ9fX0=")
					.appendLore(BLUE,
							"» " + DARK_AQUA + "Objet: " + AQUA + item.getName(),
							"» " + DARK_AQUA + "Quantité: " + AQUA + amount,
							"» " + DARK_AQUA + "Prix: " + YELLOW + (item.getSellPrice() * amount)
					)
					.toItemStack());
		}

		ShopManager.get.navPreset(this);
		addListener(this);
	}

	private void item(int slot, int amount, String displayName) {
		String quantity = displayName != null ? displayName : Integer.toString(amount);
		setItem(slot, new ItemBuilder(item.getItemStack())
				.name(GREEN + quantity)
				.appendLore(BLUE,
						"» " + DARK_AQUA + "Objet: " + AQUA + item.getName(),
						"» " + DARK_AQUA + "Quantité: " + AQUA + quantity,
						"» " + DARK_AQUA + "Prix: " + YELLOW + ((buy ? item.getBuyPrice() : item.getSellPrice()) * amount)
				)
				.toItemStack());
	}

	@Override
	public void onClick(Player player, ItemStack clickedItem, int slot) {
		if (buy && item.isBuyable() && slot < (getRows() - 2) * 9) {
			int buyAmount = getAmountFromIcon(clickedItem);
			if (!Economy.withdraw(player, buyAmount * item.getBuyPrice())) {
				player.sendMessage(LeezShop.PREFIX + RED + "Vous n'avez pas assez d'argent.");
				return;
			}

			ItemStack toGive = item.getItemStack().clone();
			toGive.setAmount(buyAmount);

			if (item.getCommands().isEmpty() && ItemUtil.giveOrDrop(player, toGive)) {
				player.sendMessage(LeezShop.PREFIX + RED + "Votre inventaire est saturé, certains objets sont tombés par terre.");
			}

			item.getCommands().forEach(command -> ServerUtil.performCommand(command
					.replace("$player", player.getName())
					.replace("$amount", Integer.toString(buyAmount))));

			player.sendMessage(LeezShop.PREFIX +
					DARK_GREEN + "Vous avez " + BOLD + "acheté" + GREEN + " " + buyAmount + "x " + item.getName() + " " + DARK_GREEN + "pour " + YELLOW +
					TextUtil.humanReadableNumber(buyAmount * item.getBuyPrice()) + "$" + DARK_GREEN + ".");
		}
		else if (!buy && item.isSellable() && slot < (getRows() - 2) * 9) {
			int sellAmount = slot == 22 ? PlayerUtil.getAmountInInventory(player, item.getItemStack()) : getAmountFromIcon(clickedItem);
			if (sellAmount <= 0) {
				player.sendMessage(LeezShop.PREFIX + RED + "Vous n'avez pas cet objet dans votre inventaire.");
				return;
			}

			if (!player.getInventory().containsAtLeast(item.getItemStack(), sellAmount)) {
				player.sendMessage(LeezShop.PREFIX + RED + "Vous n'avez pas " + DARK_RED + sellAmount + "x " + item.getName());
				return;
			}

			ItemStack toRemove = item.getItemStack().clone();
			toRemove.setAmount(sellAmount);

			if (!ItemUtil.take(player, toRemove).isEmpty())
				return;

			Economy.deposit(player, sellAmount * item.getSellPrice());
			player.sendMessage(LeezShop.PREFIX +
					DARK_GREEN + "Vous avez " + BOLD + "vendu" + GREEN + " " + sellAmount + "x " + item.getName() + " " + DARK_GREEN + "pour " + YELLOW +
					TextUtil.humanReadableNumber(sellAmount * item.getSellPrice()) + "$" + DARK_GREEN + ".");
		}
	}

	private static int getAmountFromIcon(ItemStack icon) {
		String displayName = stripColor(icon.getItemMeta().getDisplayName());
		return displayName.endsWith("stacks") ? Integer.parseInt(displayName.replace("stacks", "").trim()) * icon.getMaxStackSize() : Integer.parseInt(displayName);
	}

}
