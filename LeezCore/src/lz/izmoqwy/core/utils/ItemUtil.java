package lz.izmoqwy.core.utils;

import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
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
	 * Give items to player's inventory and drop remaining items that can't be given
	 *
	 * @param player    the player to give the items to
	 * @param rewardItm items to give
	 * @return if some items were dropped or not
	 */
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


	/*
		Item Builder
	 */

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

	public static ItemStack createItem(Material material) {
		return buildItem(new MaterialData(material), 1, null, null, null);
	}

	public static ItemStack createItem(Material material, Map<Enchantment, Integer> enchantsMap, ItemFlag... flags) {
		return buildItem(new MaterialData(material), 1, null, null, enchantsMap, flags);
	}

	public static ItemStack createItem(Material material, String name) {
		return buildItem(new MaterialData(material), 1, name, null, null);
	}

	public static ItemStack createItem(Material material, String name, Map<Enchantment, Integer> enchantsMap, ItemFlag... flags) {
		return buildItem(new MaterialData(material), 1, name, null, enchantsMap, flags);
	}

	public static ItemStack createItem(Material material, int amount) {
		return buildItem(new MaterialData(material), amount, null, null, null);
	}

	public static ItemStack createItem(Material material, int amount, Map<Enchantment, Integer> enchantsMap, ItemFlag... flags) {
		return buildItem(new MaterialData(material), amount, null, null, enchantsMap, flags);
	}

	public static ItemStack createItem(Material material, int amount, String name) {
		return buildItem(new MaterialData(material), amount, name, null, null);
	}

	public static ItemStack createItem(Material material, int amount, String name, Map<Enchantment, Integer> enchantsMap, ItemFlag... flags) {
		return buildItem(new MaterialData(material), amount, name, null, enchantsMap, flags);
	}

	public static ItemStack createItem(Material material, int amount, List<String> lore) {
		return buildItem(new MaterialData(material), amount, null, lore, null);
	}

	public static ItemStack createItem(Material material, int amount, List<String> lore, Map<Enchantment, Integer> enchantsMap, ItemFlag... flags) {
		return buildItem(new MaterialData(material), amount, null, lore, enchantsMap, flags);
	}

	public static ItemStack createItem(Material material, String name, List<String> lore) {
		return buildItem(new MaterialData(material), 1, name, lore, null);
	}

	public static ItemStack createItem(Material material, String name, List<String> lore, Map<Enchantment, Integer> enchantsMap, ItemFlag... flags) {
		return buildItem(new MaterialData(material), 1, name, lore, enchantsMap, flags);
	}

	public static ItemStack createItem(Material material, int amount, String name, List<String> lore) {
		return buildItem(new MaterialData(material), amount, name, lore, null);
	}

	public static ItemStack createItem(Material material, int amount, String name, List<String> lore, Map<Enchantment, Integer> enchantsMap, ItemFlag... flags) {
		return buildItem(new MaterialData(material), amount, name, lore, enchantsMap, flags);
	}

	public static ItemStack createItem(MaterialData materialData) {
		return buildItem(materialData, 1, null, null, null);
	}

	public static ItemStack createItem(MaterialData materialData, Map<Enchantment, Integer> enchantsMap, ItemFlag... flags) {
		return buildItem(materialData, 1, null, null, enchantsMap, flags);
	}

	public static ItemStack createItem(MaterialData materialData, String name) {
		return buildItem(materialData, 1, name, null, null);
	}

	public static ItemStack createItem(MaterialData materialData, String name, Map<Enchantment, Integer> enchantsMap, ItemFlag... flags) {
		return buildItem(materialData, 1, name, null, enchantsMap, flags);
	}

	public static ItemStack createItem(MaterialData materialData, int amount) {
		return buildItem(materialData, amount, null, null, null);
	}

	public static ItemStack createItem(MaterialData materialData, int amount, Map<Enchantment, Integer> enchantsMap, ItemFlag... flags) {
		return buildItem(materialData, amount, null, null, enchantsMap, flags);
	}

	public static ItemStack createItem(MaterialData materialData, int amount, String name) {
		return buildItem(materialData, amount, name, null, null);
	}

	public static ItemStack createItem(MaterialData materialData, int amount, String name, Map<Enchantment, Integer> enchantsMap, ItemFlag... flags) {
		return buildItem(materialData, amount, name, null, enchantsMap, flags);
	}

	public static ItemStack createItem(MaterialData materialData, int amount, List<String> lore) {
		return buildItem(materialData, amount, null, lore, null);
	}

	public static ItemStack createItem(MaterialData materialData, int amount, List<String> lore, Map<Enchantment, Integer> enchantsMap, ItemFlag... flags) {
		return buildItem(materialData, amount, null, lore, enchantsMap, flags);
	}

	public static ItemStack createItem(MaterialData materialData, String name, List<String> lore) {
		return buildItem(materialData, 1, name, lore, null);
	}

	public static ItemStack createItem(MaterialData materialData, String name, List<String> lore, Map<Enchantment, Integer> enchantsMap, ItemFlag... flags) {
		return buildItem(materialData, 1, name, lore, enchantsMap, flags);
	}

	public static ItemStack createItem(MaterialData materialData, int amount, String name, List<String> lore) {
		return buildItem(materialData, amount, name, lore, null);
	}

	public static ItemStack createItem(MaterialData materialData, int amount, String name, List<String> lore, Map<Enchantment, Integer> enchantsMap, ItemFlag... flags) {
		return buildItem(materialData, amount, name, lore, enchantsMap, flags);
	}

	public static ItemStack skull(String player, String name, List<String> lore) {
		ItemStack skullItem = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
		SkullMeta meta = (SkullMeta) skullItem.getItemMeta();
		meta.setOwner(player);
		meta.setDisplayName(name);
		meta.setLore(lore);
		skullItem.setItemMeta(meta);
		return skullItem;
	}

	@SuppressWarnings("deprecation")
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
