package lz.izmoqwy.leezshop;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Pattern;

import lz.izmoqwy.core.utils.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import lz.izmoqwy.leezshop.obj.ItemPrice;
import lz.izmoqwy.leezshop.obj.ShopItem;
import lz.izmoqwy.leezshop.obj.ShopSection;

public class ShopLoader {

	private static Map<MaterialData, ShopItem> itemsMap = Maps.newHashMap();
	private static Map<Integer, ShopSection> sections = Maps.newHashMap();
	public static Map<String, Map<Integer, ShopItem>> specials = Maps.newHashMap();

	private static int maxSize;
	public static final Map<Enchantment, Integer> defaultEnchants = new HashMap<Enchantment, Integer>() {
		private static final long serialVersionUID = 1L;

		{
			put(Enchantment.ARROW_FIRE, 1);
		}

	};
	public static final ItemStack closeBtn = ItemUtil.createItem(Material.BARRIER, "§4Fermer", Collections.singletonList("§cFermer le menu"), defaultEnchants, ItemFlag.HIDE_ENCHANTS),
			backBtn = ItemUtil.createItem(Material.ENDER_PEARL, "§cRetour", Collections.singletonList("§cRetourner au menu"), defaultEnchants, ItemFlag.HIDE_ENCHANTS);

	@SuppressWarnings("deprecation")
	public static void load() {

		long startLoad = System.currentTimeMillis();
		YamlConfiguration config = (YamlConfiguration) LeezShop.getInstance().getConfig();

		maxSize = config.getInt("gui-size");
		if (maxSize > 6) maxSize = 6;
		if (maxSize < 1) maxSize = 1;

		ConfigurationSection shop = config.getConfigurationSection("shop");
		for (String section : shop.getKeys(false)) {
			ConfigurationSection sec = shop.getConfigurationSection(section);

			String sectionName = sec.getString("display-name");
			String[] sectionType = sec.getString("type").split(Pattern.quote(":"));
			MaterialData data;
			if (sectionType.length == 1)
				data = new MaterialData(Integer.parseInt(sectionType[0]));
			else if (sectionType.length == 2)
				data = new MaterialData(Integer.parseInt(sectionType[0]), (byte) Integer.parseInt(sectionType[1]));
			else {
				LeezShop.getInstance().getLogger().warning("La représentation de la catégorie \"" + sectionName + "\" n'est pas valide.");
				continue;
			}
			String description = sec.getString("description");

			List<ShopItem> items = Lists.newArrayList();
			ConfigurationSection secContent = sec.getConfigurationSection("content");
			for (String item : secContent.getKeys(false)) {
				ConfigurationSection itm = secContent.getConfigurationSection(item);

				String name = itm.getString("name");
				String[] type = itm.getString("type").split(Pattern.quote(":"));
				List<String> commands = itm.getStringList("commands");
				if (commands == null) commands = Lists.newArrayList();
				ShopItem shopItem = new ShopItem(name, commands,
						(type.length == 1 ? new MaterialData(Integer.parseInt(type[0])) :
								new MaterialData(Integer.parseInt(type[0]), (byte) Integer.parseInt(type[1]))),
						new ItemPrice(itm.getDouble("price.buy"), itm.getDouble("price.sell")));

				items.add(shopItem);
				itemsMap.putIfAbsent(shopItem.getData(), shopItem);

			}

			ShopSection shopSection = new ShopSection(sectionName, sec.getBoolean("enchanted", false), sec.getInt("slot", 1), description, data, items);
			sections.putIfAbsent(shopSection.getSlot(), shopSection);

			boolean special = sec.getBoolean("special", false);
			if (special) {
				Map<Integer, ShopItem> spe = Maps.newHashMap();
				int slot = 0;
				for (ShopItem itm : items) {
					itm.parent = shopSection;
					spe.put(slot++, itm);
				}
				ShopLoader.specials.putIfAbsent(sectionName, spe);

			}
			else {
				for (ShopItem itm : items) {
					itm.parent = shopSection;
				}
			}
		}

		double time = (System.currentTimeMillis() - startLoad) / 1000D;
		LeezShop.getInstance().getLogger().info("Le shop s'est chargé en " + time + "s");

	}

	private static ItemStack createSkull(String url, String displayName, List<String> lore) {

		ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		if (url.isEmpty()) return head;

		SkullMeta headMeta = (SkullMeta) head.getItemMeta();

		if (displayName != null) headMeta.setDisplayName(displayName);
		if (lore != null) headMeta.setLore(lore);

		GameProfile profile = new GameProfile(UUID.randomUUID(), null);

		profile.getProperties().put("textures", new Property("textures", url));

		try {
			Field profileField = headMeta.getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			profileField.set(headMeta, profile);

		}
		catch (IllegalArgumentException | NoSuchFieldException | SecurityException | IllegalAccessException error) {
			error.printStackTrace();
		}
		head.setItemMeta(headMeta);
		return head;
	}

	public static ItemStack getSellHead(String name, List<String> lore) {

		return createSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDM3ODYyY2RjMTU5OTk4ZWQ2YjZmZGNjYWFhNDY3NTg2N2Q0NDg0ZGI1MTJhODRjMzY3ZmFiZjRjYWY2MCJ9fX0=", name, lore);

	}

	private static void setBacks(Inventory inventory, boolean close) {
		int size = inventory.getSize();
		inventory.setItem(size - 9, closeBtn);
		if (!close)
			inventory.setItem(size - 1, backBtn);
	}

	@SuppressWarnings({"deprecation"})
	private static void setFloor(Inventory inventory, ShopSection section, int slot) {
		int size = inventory.getSize();
		ItemStack glass = ItemUtil.createItem(new MaterialData(Material.STAINED_GLASS_PANE, (byte) 5), "§2-§a§k*§2-");
		for (int i = (size - 18); i < (size - 9); i++) {
			inventory.setItem(i, glass);
		}
		inventory.setItem(size - 8, glass);
		if (section != null) {
			if (slot >= 0)
				inventory.setItem(size - 5, getIcon(section, "§8Item: §7" + slot));
			else inventory.setItem(size - 5, getIcon(section));
		}
		inventory.setItem(size - 2, glass);


		setBacks(inventory, false);
	}

	public static ItemStack getIcon(ShopSection sec, String... addedLore) {
		List<String> lore = sec.getDescription() == null || sec.getDescription().isEmpty() ? Lists.newArrayList() : Arrays.asList(("§7" + sec.getDescription().replaceAll("\n", "\n§e")).split(Pattern.quote("\n")));
		lore.addAll(Arrays.asList(addedLore));
		if (sec.isEnchanted()) {
			return ItemUtil.createItem(sec.getIcon(), "§e" + sec.getDisplayName(), lore, defaultEnchants, ItemFlag.HIDE_ENCHANTS);
		}
		else return ItemUtil.createItem(sec.getIcon(), "§e" + sec.getDisplayName(), lore);
	}

	public static List<String> getShopItemLore(ShopItem item) {
		List<String> lore = Lists.newArrayList();
		if (item.isBuyable()) {
			lore.add("§2Clique gauche pour acheter");
			lore.add("§9» §3Prix d'achat: §e" + item.getBuyPrice() + "$ §bunité");
		}
		else {
			lore.add("§4Cet objet ne s'achète pas");
		}
		if (item.isSellable()) {
			lore.add("§aClique droit pour vendre");
			lore.add("§9» §3Prix de vente: §e" + item.getSellPrice() + "$ §bunité");
		}
		else {
			lore.add("§cCet objet ne se vend pas");
		}
		return lore;
	}

	public static Inventory loadInventory() {
		Inventory inventory = Bukkit.createInventory(null, maxSize * 9, "§6Shop");

		for (ShopSection sec : sections.values()) {
			if (sec.getSlot() <= inventory.getSize()) {
				ItemStack item = getIcon(sec);
				inventory.setItem(sec.getSlot(), item);
			}
			else {
				LeezShop.getInstance().getLogger().warning("Le slot d'affichage de la catégorie \"" + sec.getDisplayName() + "\" est trop grand par rapport à l'inventaire !");
			}
		}

		setBacks(inventory, true);
		return inventory;
	}

	public static Inventory loadSection(int slot) {
		ShopSection section = sections.get(slot);
		if (section == null) return null;
		Inventory inventory = Bukkit.createInventory(null, 6 * 9, "§6Shop §8» §e" + ChatColor.stripColor(section.getDisplayName()));

		int i = 0;
		for (ShopItem item : section.getItems()) {

			if (i == 36) break;
			inventory.setItem(i, ItemUtil.createItem(item.getData(), "§e" + item.getName(), getShopItemLore(item)));
			i++;
		}

		setFloor(inventory, section, -1);
		return inventory;
	}


	private static void setupBuy(Inventory inventory, int slot, ShopItem item, int amount, String title) {
		if (title == null) title = (amount + "");
		inventory.setItem(slot, ItemUtil.createItem(item.getData(), "§a" + title, Arrays.asList("§9» §3Objet: §b" + item.getName(), "§9» §3Quantité: §b" + title, "§9» §3Prix: §e" + (item.getBuyPrice() * amount))));
	}

	private static void setupSell(Inventory inventory, int slot, ShopItem item, int amount, String title) {
		if (title == null) title = (amount + "");
		inventory.setItem(slot, ItemUtil.createItem(item.getData(), "§a" + title, Arrays.asList("§9» §3Objet: §b" + item.getName(), "§9» §3Quantité: §b" + title, "§9» §3Prix: §e" + (item.getBuyPrice() * amount))));
	}

	public static Inventory loadBuyInventory(ShopItem item, int from) {
		final String title = "§6Shop §8» §aAchat";
		Inventory inventory = Bukkit.createInventory(null, 6 * 9, title);

		switch (item.getData().toItemStack().getMaxStackSize()) {
			case 1:
				setupBuy(inventory, 13, item, 2, null);
				break;

			case 16:
				setupBuy(inventory, 12, item, 1, null);
				setupBuy(inventory, 13, item, 8, null);
				setupBuy(inventory, 14, item, 16, null);
				break;

			default:
				setupBuy(inventory, 11, item, 1, null);
				setupBuy(inventory, 12, item, 8, null);
				setupBuy(inventory, 13, item, 16, null);
				setupBuy(inventory, 14, item, 32, null);
				setupBuy(inventory, 15, item, 64, null);

				setupBuy(inventory, 21, item, 128, "2 stacks");
				setupBuy(inventory, 22, item, 256, "4 stacks");
				setupBuy(inventory, 23, item, 512, "8 stacks");
				break;
		}

		setFloor(inventory, item.parent, from);
		return inventory;
	}

	public static Inventory loadSellInventory(ShopItem item, Player player, int from) {
		Inventory inventory = Bukkit.createInventory(null, 6 * 9, "§6Shop §8» §aVente");

		setupSell(inventory, 11, item, 1, null);
		setupSell(inventory, 12, item, 8, null);
		setupSell(inventory, 13, item, 16, null);
		setupSell(inventory, 14, item, 32, null);
		setupSell(inventory, 15, item, 64, null);

		int amount = ItemUtil.getAmountOf(ItemUtil.createItem(item.getData()), player);
		inventory.setItem(22, getSellHead("§aTout vendre", Arrays.asList("§9» §3Objet: §b" + item.getName(), "§9» §3Quantité: §b" + amount, "§9» §3Prix: §e" + (item.getBuyPrice() * amount))));

		setFloor(inventory, item.parent, from);
		return inventory;
	}

	@SuppressWarnings("deprecation")
	public boolean isBuyable(ItemStack itm) {

		MaterialData defaultData = new MaterialData(itm.getType(), itm.getData().getData());
		if (itemsMap.containsKey(defaultData)) {

			return itemsMap.get(defaultData).isBuyable();

		}

		return false;
	}

	@SuppressWarnings("deprecation")
	public boolean isSellable(ItemStack itm) {

		MaterialData defaultData = new MaterialData(itm.getType(), itm.getData().getData());
		if (itemsMap.containsKey(defaultData)) {

			return itemsMap.get(defaultData).isSellable();

		}

		return false;
	}

	public static ShopItem getItem(MaterialData data) {
		return itemsMap.getOrDefault(data, null);
	}

}
