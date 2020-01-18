package lz.izmoqwy.core.utils;

import com.google.common.collect.Lists;
import lz.izmoqwy.core.api.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;

import java.util.List;
import java.util.Map;

public class ItemUtil {

	private ItemUtil() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @param player    the player to give the items to
	 * @param rewardItm items to give
	 * @return if some items were dropped or not
	 * @deprecated use {@link ItemUtil#give(Player, ItemStack...)} instead
	 * Give items to player's inventory and drop remaining items that can't be given
	 */
	@Deprecated
	public static boolean giveItems(Player player, ItemStack rewardItm) {
		if (player.getInventory().firstEmpty() == -1) {
			int nb = 0, max = 0;
			for (ItemStack itm : player.getInventory().all(rewardItm.getType()).values()) {
				if (itm.getData().getData() != rewardItm.getData().getData())
					continue;
				if (itm.hasItemMeta() && rewardItm.hasItemMeta() && itm.getItemMeta().equals(rewardItm.getItemMeta())) {
					nb += itm.getAmount();
					max += itm.getMaxStackSize();
				}
			}
			if ((max - nb) >= rewardItm.getAmount()) {
				player.getInventory().addItem(rewardItm);
				return true;
			}
			else {
				while (rewardItm.getAmount() > 64) {
					rewardItm.setAmount(rewardItm.getAmount() - 64);
					ItemStack itm = rewardItm.clone();
					itm.setAmount(64);
					player.getWorld().dropItem(player.getLocation(), itm);
				}
				player.getWorld().dropItem(player.getLocation(), rewardItm);
				return false;
			}
		}
		else {
			player.getInventory().addItem(rewardItm);
			return true;
		}
	}

	/**
	 * Give items to a player
	 * (does not use built-in give methods)
	 *
	 * @param player The player to give the items to
	 * @param items  The items to give
	 * @return Items that couldn't be added
	 */
	public static List<ItemStack> give(Player player, ItemStack... items) {
		PlayerInventory inventory = player.getInventory();
		final int inventorySize = 36;
		List<ItemStack> remaining = Lists.newArrayList();

		all:
		for (ItemStack item : items) {
			int total = item.getAmount(), toGive = total;
			final int max = item.getMaxStackSize();

			// Partials
			for (int i = 0; i < inventorySize; i++) {
				ItemStack content = inventory.getItem(i);
				if (content != null && content.getAmount() < max && content.isSimilar(item)) {
					int _amount = Math.min(max - content.getAmount(), toGive);
					content.setAmount(content.getAmount() + _amount);
					toGive -= _amount;
					if (toGive == 0)
						continue all;
				}
			}

			// Empties
			for (int i = 0; i < inventorySize; i++) {
				ItemStack content = inventory.getItem(i);
				if (content == null) {
					int _amount = Math.min(toGive, max);
					item = item.clone();
					item.setAmount(_amount);
					inventory.setItem(i, item);
					toGive -= _amount;
					if (toGive == 0)
						continue all;
				}
			}

			item = item.clone();
			item.setAmount(toGive);
			remaining.add(item);
		}

		return remaining;
	}

	/**
	 * Remove items from player's inventory
	 * (does not use built-in remove methods)
	 *
	 * @param player The player to remove the items from
	 * @param items  The items to remove
	 * @return Items that couldn't be removed (basically missing ones)
	 */
	public static List<ItemStack> take(Player player, ItemStack... items) {
		PlayerInventory inventory = player.getInventory();
		final int inventorySize = 36;
		List<ItemStack> remaining = Lists.newArrayList();

		all:
		for (ItemStack item : items) {
			int total = item.getAmount(), toTake = total;

			// check held item first
			if (inventory.getItemInMainHand() != null && inventory.getItemInMainHand().isSimilar(item)) {
				ItemStack held = inventory.getItemInMainHand();
				if (held.getAmount() > toTake) {
					held.setAmount(held.getAmount() - toTake);
					toTake = 0;
				}
				else {
					inventory.setItem(inventory.getHeldItemSlot(), null);
					toTake -= held.getAmount();
				}
				if (toTake == 0)
					continue;
			}

			for (int i = 0; i < inventorySize; i++) {
				ItemStack content = inventory.getItem(i);
				if (content != null && content.isSimilar(item)) {
					if (content.getAmount() > toTake) {
						content.setAmount(content.getAmount() - toTake);
						toTake = 0;
					}
					else {
						inventory.setItem(i, null);
						toTake -= content.getAmount();
					}
					if (toTake == 0)
						continue all;
				}
			}

			item = item.clone();
			item.setAmount(toTake);
			remaining.add(item);
		}

		return remaining;
	}

	private static ItemStack buildItem(MaterialData materialData, int amount, String name, List<String> lore, Map<Enchantment, Integer> enchantsMap, ItemFlag... flags) {
		ItemStack item = new ItemStack(materialData.getItemType(), amount, materialData.getData());
		ItemMeta meta = item.getItemMeta();
		if (name != null)
			meta.setDisplayName(name);
		if (lore != null)
			meta.setLore(lore);
		if (enchantsMap != null) {
			for (Map.Entry<Enchantment, Integer> entry : enchantsMap.entrySet()) {
				meta.addEnchant(entry.getKey(), entry.getValue(), true);
			}
		}
		for (ItemFlag flag : flags) {
			meta.addItemFlags(flag);
		}
		item.setItemMeta(meta);
		return item;
	}

	/**
	 * @deprecated use {@link ItemBuilder} instead
	 */
	@Deprecated
	public static ItemStack createItem(Material material) {
		return buildItem(new MaterialData(material), 1, null, null, null);
	}

	/**
	 * @deprecated use {@link ItemBuilder} instead
	 */
	@Deprecated
	public static ItemStack createItem(Material material, Map<Enchantment, Integer> enchantsMap, ItemFlag... flags) {
		return buildItem(new MaterialData(material), 1, null, null, enchantsMap, flags);
	}

	/**
	 * @deprecated use {@link ItemBuilder} instead
	 */
	@Deprecated
	public static ItemStack createItem(Material material, String name) {
		return buildItem(new MaterialData(material), 1, name, null, null);
	}

	/**
	 * @deprecated use {@link ItemBuilder} instead
	 */
	@Deprecated
	public static ItemStack createItem(Material material, String name, Map<Enchantment, Integer> enchantsMap, ItemFlag... flags) {
		return buildItem(new MaterialData(material), 1, name, null, enchantsMap, flags);
	}

	/**
	 * @deprecated use {@link ItemBuilder} instead
	 */
	@Deprecated
	public static ItemStack createItem(Material material, int amount) {
		return buildItem(new MaterialData(material), amount, null, null, null);
	}

	/**
	 * @deprecated use {@link ItemBuilder} instead
	 */
	@Deprecated
	public static ItemStack createItem(Material material, int amount, Map<Enchantment, Integer> enchantsMap, ItemFlag... flags) {
		return buildItem(new MaterialData(material), amount, null, null, enchantsMap, flags);
	}

	/**
	 * @deprecated use {@link ItemBuilder} instead
	 */
	@Deprecated
	public static ItemStack createItem(Material material, int amount, String name) {
		return buildItem(new MaterialData(material), amount, name, null, null);
	}

	/**
	 * @deprecated use {@link ItemBuilder} instead
	 */
	@Deprecated
	public static ItemStack createItem(Material material, int amount, String name, Map<Enchantment, Integer> enchantsMap, ItemFlag... flags) {
		return buildItem(new MaterialData(material), amount, name, null, enchantsMap, flags);
	}

	/**
	 * @deprecated use {@link ItemBuilder} instead
	 */
	@Deprecated
	public static ItemStack createItem(Material material, int amount, List<String> lore) {
		return buildItem(new MaterialData(material), amount, null, lore, null);
	}

	/**
	 * @deprecated use {@link ItemBuilder} instead
	 */
	@Deprecated
	public static ItemStack createItem(Material material, int amount, List<String> lore, Map<Enchantment, Integer> enchantsMap, ItemFlag... flags) {
		return buildItem(new MaterialData(material), amount, null, lore, enchantsMap, flags);
	}

	/**
	 * @deprecated use {@link ItemBuilder} instead
	 */
	@Deprecated
	public static ItemStack createItem(Material material, String name, List<String> lore) {
		return buildItem(new MaterialData(material), 1, name, lore, null);
	}

	/**
	 * @deprecated use {@link ItemBuilder} instead
	 */
	@Deprecated
	public static ItemStack createItem(Material material, String name, List<String> lore, Map<Enchantment, Integer> enchantsMap, ItemFlag... flags) {
		return buildItem(new MaterialData(material), 1, name, lore, enchantsMap, flags);
	}

	/**
	 * @deprecated use {@link ItemBuilder} instead
	 */
	@Deprecated
	public static ItemStack createItem(Material material, int amount, String name, List<String> lore) {
		return buildItem(new MaterialData(material), amount, name, lore, null);
	}

	/**
	 * @deprecated use {@link ItemBuilder} instead
	 */
	@Deprecated
	public static ItemStack createItem(Material material, int amount, String name, List<String> lore, Map<Enchantment, Integer> enchantsMap, ItemFlag... flags) {
		return buildItem(new MaterialData(material), amount, name, lore, enchantsMap, flags);
	}

	/**
	 * @deprecated use {@link ItemBuilder} instead
	 */
	@Deprecated
	public static ItemStack createItem(MaterialData materialData) {
		return buildItem(materialData, 1, null, null, null);
	}

	/**
	 * @deprecated use {@link ItemBuilder} instead
	 */
	@Deprecated
	public static ItemStack createItem(MaterialData materialData, Map<Enchantment, Integer> enchantsMap, ItemFlag... flags) {
		return buildItem(materialData, 1, null, null, enchantsMap, flags);
	}

	/**
	 * @deprecated use {@link ItemBuilder} instead
	 */
	@Deprecated
	public static ItemStack createItem(MaterialData materialData, String name) {
		return buildItem(materialData, 1, name, null, null);
	}

	/**
	 * @deprecated use {@link ItemBuilder} instead
	 */
	@Deprecated
	public static ItemStack createItem(MaterialData materialData, String name, Map<Enchantment, Integer> enchantsMap, ItemFlag... flags) {
		return buildItem(materialData, 1, name, null, enchantsMap, flags);
	}

	/**
	 * @deprecated use {@link ItemBuilder} instead
	 */
	@Deprecated
	public static ItemStack createItem(MaterialData materialData, int amount) {
		return buildItem(materialData, amount, null, null, null);
	}

	/**
	 * @deprecated use {@link ItemBuilder} instead
	 */
	@Deprecated
	public static ItemStack createItem(MaterialData materialData, int amount, Map<Enchantment, Integer> enchantsMap, ItemFlag... flags) {
		return buildItem(materialData, amount, null, null, enchantsMap, flags);
	}

	/**
	 * @deprecated use {@link ItemBuilder} instead
	 */
	@Deprecated
	public static ItemStack createItem(MaterialData materialData, int amount, String name) {
		return buildItem(materialData, amount, name, null, null);
	}

	/**
	 * @deprecated use {@link ItemBuilder} instead
	 */
	@Deprecated
	public static ItemStack createItem(MaterialData materialData, int amount, String name, Map<Enchantment, Integer> enchantsMap, ItemFlag... flags) {
		return buildItem(materialData, amount, name, null, enchantsMap, flags);
	}

	/**
	 * @deprecated use {@link ItemBuilder} instead
	 */
	@Deprecated
	public static ItemStack createItem(MaterialData materialData, int amount, List<String> lore) {
		return buildItem(materialData, amount, null, lore, null);
	}

	/**
	 * @deprecated use {@link ItemBuilder} instead
	 */
	@Deprecated
	public static ItemStack createItem(MaterialData materialData, int amount, List<String> lore, Map<Enchantment, Integer> enchantsMap, ItemFlag... flags) {
		return buildItem(materialData, amount, null, lore, enchantsMap, flags);
	}

	/**
	 * @deprecated use {@link ItemBuilder} instead
	 */
	@Deprecated
	public static ItemStack createItem(MaterialData materialData, String name, List<String> lore) {
		return buildItem(materialData, 1, name, lore, null);
	}

	/**
	 * @deprecated use {@link ItemBuilder} instead
	 */
	@Deprecated
	public static ItemStack createItem(MaterialData materialData, String name, List<String> lore, Map<Enchantment, Integer> enchantsMap, ItemFlag... flags) {
		return buildItem(materialData, 1, name, lore, enchantsMap, flags);
	}

	/**
	 * @deprecated use {@link ItemBuilder} instead
	 */
	@Deprecated
	public static ItemStack createItem(MaterialData materialData, int amount, String name, List<String> lore) {
		return buildItem(materialData, amount, name, lore, null);
	}

	/**
	 * @deprecated use {@link ItemBuilder} instead
	 */
	@Deprecated
	public static ItemStack createItem(MaterialData materialData, int amount, String name, List<String> lore, Map<Enchantment, Integer> enchantsMap, ItemFlag... flags) {
		return buildItem(materialData, amount, name, lore, enchantsMap, flags);
	}

	/**
	 * @deprecated use {@link ItemBuilder#ItemBuilder(SkullType)} instead
	 */
	@Deprecated
	public static ItemStack skull(String player, String name, List<String> lore) {
		ItemStack skullItem = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
		SkullMeta meta = (SkullMeta) skullItem.getItemMeta();
		meta.setOwner(player);
		meta.setDisplayName(name);
		meta.setLore(lore);
		skullItem.setItemMeta(meta);
		return skullItem;
	}

	/**
	 * @deprecated use {@link PlayerUtil#getAmountInInventory(Player, ItemStack)} instead
	 */
	@Deprecated
	public static int getAmountOf(ItemStack itm, Player player) {
		int amount = 0;
		for (ItemStack tItem : player.getInventory().all(itm.getType()).values()) {
			if (tItem != null && tItem.getType() == itm.getType() && tItem.getData().getData() == itm.getData().getData()) {
				amount += tItem.getAmount();
			}
		}
		return amount;
	}

}
