package lz.izmoqwy.core.api;

import com.google.common.collect.Lists;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftSkull;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;

import java.util.Arrays;
import java.util.List;

public class ItemBuilder {

	private ItemStack itemStack;
	private ItemMeta itemMeta;

	public ItemBuilder(Material material) {
		itemStack = new ItemStack(material);
		meta();
	}

	public ItemBuilder(MaterialData materialData) {
		itemStack = new ItemStack(materialData.getItemType());
		itemStack.setData(materialData);
		meta();
	}

	public ItemBuilder(SkullType skullType) {
		itemStack = new ItemStack(Material.SKULL_ITEM);
		itemStack.setDurability((short) skullType.ordinal());
		meta();
	}

	public ItemBuilder(ItemStack from) {
		this.itemStack = from.clone();
		meta();
	}

	private void meta() {
		this.itemMeta = itemStack.getItemMeta();
	}

	public ItemBuilder amount(int amount) {
		itemStack.setAmount(amount);
		return this;
	}

	public ItemBuilder damage(short damage) {
		itemStack.setDurability(damage);
		return this;
	}

	@SuppressWarnings("deprecation")
	public ItemBuilder dyeColor(DyeColor color) {
		return damage(color.getDyeData());
	}

	public ItemBuilder name(String displayName) {
		return name(displayName, true);
	}

	public ItemBuilder name(String displayName, boolean colorTransform) {
		if (colorTransform)
			displayName = ChatColor.translateAlternateColorCodes('&', displayName);
		itemMeta.setDisplayName(displayName);
		return this;
	}

	public ItemBuilder appendLore(String... lore) {
		return appendLore(Arrays.asList(lore));
	}

	public ItemBuilder appendLore(ChatColor color, String... lore) {
		return appendLore(Arrays.asList(lore), color);
	}

	public ItemBuilder appendLore(List<String> lore) {
		return appendLore(lore, null);
	}

	public ItemBuilder appendLore(List<String> lore, ChatColor chatColor) {
		List<String> lines = Lists.newArrayList();
		if (itemMeta.hasLore()) {
			lines.addAll(itemMeta.getLore());
		}
		for (String line : lore) {
			for (String _line : line.split("\\n")) {
				lines.add(chatColor != null ? chatColor + _line : _line);
			}
		}
		itemMeta.setLore(lines);
		return this;
	}

	public ItemBuilder appendInlineLore(String lore) {
		List<String> lines = Lists.newArrayList();
		if (itemMeta.hasLore()) {
			lines.addAll(itemMeta.getLore());
		}
		if (lines.isEmpty())
			lines.add(lore);
		else {
			int lastIndex = lines.size() - 1;
			lines.set(lastIndex, lines.get(lastIndex) + lore);
		}
		itemMeta.setLore(lines);
		return this;
	}

	public ItemBuilder removeLoreLine(int line) {
		if (checkLore(line)) {
			itemMeta.getLore().remove(line);
		}
		return this;
	}

	public ItemBuilder editLoreLine(int line, String value) {
		if (checkLore(line)) {
			List<String> lore = itemMeta.getLore();
			lore.set(line, value);
			itemMeta.setLore(lore);
		}
		else {
			int lines2add = itemMeta.hasLore() ? (line + 1) - itemMeta.getLore().size() : line + 1;
			List<String> lore = Lists.newArrayList();
			if (itemMeta.hasLore())
				lore.addAll(itemMeta.getLore());
			for (int i = 0; i < lines2add; i++) {
				lore.add(" ");
			}
			lore.set(line, value);
			itemMeta.setLore(lore);
		}
		return this;
	}

	private boolean checkLore(int line) {
		return itemMeta.hasLore() && line < itemMeta.getLore().size();
	}

	public ItemBuilder addEnchant(Enchantment enchantment, int level, boolean unsafe) {
		if (unsafe)
			itemStack.addUnsafeEnchantment(enchantment, level);
		else
			itemMeta.addEnchant(enchantment, level, true);
		return this;
	}

	public ItemBuilder addFlags(ItemFlag... itemFlags) {
		itemMeta.addItemFlags(itemFlags);
		return this;
	}

	// add hidden enchant to make the item shine
	public ItemBuilder quickEnchant() {
		return addEnchant(Enchantment.DAMAGE_ARTHROPODS, 1, false).addFlags(ItemFlag.HIDE_ENCHANTS);
	}

	public ItemBuilder makeUnbrekable() {
		return setUnbreakable(true, false);
	}

	public ItemBuilder setUnbreakable(boolean unbreakable, boolean hidden) {
		itemMeta.setUnbreakable(unbreakable);
		if (hidden && !itemMeta.hasItemFlag(ItemFlag.HIDE_UNBREAKABLE)) {
			itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		}
		return this;
	}

	@SuppressWarnings("deprecation")
	public ItemBuilder setSkullOwner(String owner) {
		if (itemMeta instanceof SkullMeta) {
			((SkullMeta) itemMeta).setOwner(owner);
		}
		return this;
	}

	public ItemBuilder copy() {
		return new ItemBuilder(toItemStack());
	}

	public ItemStack toItemStack() {
		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}

}
