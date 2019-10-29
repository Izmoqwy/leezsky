package lz.izmoqwy.core.builder;

import com.google.common.collect.Lists;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;

import java.util.Arrays;
import java.util.List;

public class ItemBuilder {

	private ItemStack is;
	private ItemMeta meta;

	public ItemBuilder(Material material) {
		is = new ItemStack(material);
		meta();
	}

	public ItemBuilder(MaterialData materialData) {
		is = new ItemStack(materialData.getItemType());
		is.setData(materialData);
		meta();
	}

	public ItemBuilder(SkullType skullType) {
		is = new ItemStack(Material.SKULL_ITEM);
		is.setDurability((short) skullType.ordinal());
		meta();
	}

	public ItemBuilder(ItemStack from) {
		this.is = from;
		meta();
	}

	private void meta() {
		this.meta = is.getItemMeta();
	}

	public ItemBuilder amount(int amount) {
		is.setAmount(amount);
		return this;
	}

	public ItemBuilder damage(short damage) {
		is.setDurability(damage);
		return this;
	}

	@SuppressWarnings("deprecation")
	public ItemBuilder dyeColor(DyeColor color) {
		return damage(color.getData());
	}

	public ItemBuilder name(String displayName) {
		return name(displayName, true);
	}

	public ItemBuilder name(String displayName, boolean colorTransform) {
		if (colorTransform)
			displayName = ChatColor.translateAlternateColorCodes('&', displayName);
		meta.setDisplayName(displayName);
		return this;
	}

	public ItemBuilder appendLore(String... lore) {
		return appendLore(Arrays.asList(lore));
	}

	public ItemBuilder appendLore(ChatColor color, String... lore) {
		return appendLore(Arrays.asList(lore), color);
	}

	public ItemBuilder appendLore(List<String> lore) {
		return appendLore(lore, ChatColor.RESET);
	}

	public ItemBuilder appendLore(List<String> lore, ChatColor chatColor) {
		List<String> lines = Lists.newArrayList();
		if (meta.hasLore()) {
			lines.addAll(meta.getLore());
		}
		for (String line : lore) {
			for (String _line : line.split("\\n")) {
				lines.add(chatColor + _line);
			}
		}
		meta.setLore(lines);
		return this;
	}

	public ItemBuilder appendInlineLore(String lore) {
		List<String> lines = Lists.newArrayList();
		if (meta.hasLore()) {
			lines.addAll(meta.getLore());
		}
		if (lines.isEmpty())
			lines.add(lore);
		else {
			int lastIndex = lines.size() - 1;
			lines.set(lastIndex, lines.get(lastIndex) + lore);
		}
		meta.setLore(lines);
		return this;
	}

	public ItemBuilder removeLoreLine(int line) {
		if (checkLore(line)) {
			meta.getLore().remove(line);
		}
		return this;
	}

	public ItemBuilder editLoreLine(int line, String value) {
		if (checkLore(line)) {
			List<String> lore = meta.getLore();
			lore.set(line, value);
			meta.setLore(lore);
		}
		else {
			int lines2add = meta.hasLore() ? (line + 1) - meta.getLore().size() : line + 1;
			List<String> lore = Lists.newArrayList();
			if (meta.hasLore())
				lore.addAll(meta.getLore());
			for (int i = 0; i < lines2add; i++) {
				lore.add(" ");
			}
			lore.set(line, value);
			meta.setLore(lore);
		}
		return this;
	}

	private boolean checkLore(int line) {
		return meta.hasLore() && line < meta.getLore().size();
	}

	public ItemBuilder addEnchant(Enchantment enchantment, int level, boolean unsafe) {
		if (unsafe)
			is.addUnsafeEnchantment(enchantment, level);
		else
			meta.addEnchant(enchantment, level, true);
		return this;
	}

	public ItemBuilder addFlags(ItemFlag... itemFlags) {
		meta.addItemFlags(itemFlags);
		return this;
	}

	public ItemBuilder makeUnbrekable() {
		return setUnbreakable(true, false);
	}

	public ItemBuilder setUnbreakable(boolean unbreakable, boolean hidden) {
		meta.spigot().setUnbreakable(unbreakable);
		if (hidden && !meta.hasItemFlag(ItemFlag.HIDE_UNBREAKABLE)) {
			meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		}
		return this;
	}

	public ItemBuilder skullOwner(String owner) {
		if (meta instanceof SkullMeta) {
			((SkullMeta) meta).setOwner(owner);
		}
		return this;
	}

	public ItemBuilder copy() {
		return new ItemBuilder(toItemStack());
	}

	public ItemStack toItemStack() {
		is.setItemMeta(meta);
		return is;
	}

}
