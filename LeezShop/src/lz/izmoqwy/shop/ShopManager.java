package lz.izmoqwy.shop;

import com.google.common.collect.Lists;
import lombok.Getter;
import lz.izmoqwy.core.api.ItemBuilder;
import lz.izmoqwy.core.gui.UniqueMinecraftGUI;
import lz.izmoqwy.core.utils.StoreUtil;
import lz.izmoqwy.shop.obj.ShopCategory;
import lz.izmoqwy.shop.obj.ShopItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.List;

@Getter
public class ShopManager {

	public static final ShopManager get = new ShopManager();

	private List<ShopCategory> categories;
	private int mainGuiRows;

	private ShopManager() {
	}

	public void load(Plugin plugin) {
		File file = new File(plugin.getDataFolder(), "shop.yml");
		if (!StoreUtil.copyTemplateIfMissing(file, LeezShop.getInstance().getResource("templates/shop.yml")))
			return;

		YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
		mainGuiRows = configuration.getInt("gui-size", 3);
		System.out.println(mainGuiRows);
		categories = loadCategories(configuration);
	}

	private List<ShopCategory> loadCategories(YamlConfiguration configuration) {
		List<ShopCategory> categories = Lists.newArrayList();

		ConfigurationSection categoriesSection = configuration.getConfigurationSection("categories");
		if (categoriesSection == null)
			return categories;

		for (String categoryKey : categoriesSection.getKeys(false)) {
			ConfigurationSection categorySection = categoriesSection.getConfigurationSection(categoryKey);
			List<ShopItem> items = Lists.newArrayList();

			ConfigurationSection listSection = categorySection.getConfigurationSection("list");
			if (listSection != null) {
				for (String itemKey : listSection.getKeys(false)) {
					ConfigurationSection itemSection = listSection.getConfigurationSection(itemKey);
					ShopItem.ShopItemBuilder shopItemBuilder = ShopItem.builder()
							.name(itemSection.getString("name", "Sans nom"))
							.buyPrice(itemSection.getDouble("price.buy", 0))
							.sellPrice(itemSection.getDouble("price.sell", 0));

					ItemStack itemStack = StoreUtil.itemStackFromYAML(itemSection.getConfigurationSection("item"));
					if (itemStack == null) {
						LeezShop.getInstance().getLogger().warning("[Shop] Invalid item '" + itemKey + "' in category '" + categoryKey + "'");
						continue;
					}
					shopItemBuilder.itemStack(itemStack);

					List<String> commandList = itemSection.getStringList("commands");
					if (commandList != null)
						shopItemBuilder.commands(commandList);

					items.add(shopItemBuilder.build());
				}
			}

			ItemStack categoryIcon = StoreUtil.itemStackFromYAML(categorySection.getConfigurationSection("icon"));
			if (categoryIcon == null) {
				LeezShop.getInstance().getLogger().warning("[Shop] Invalid icon for category '" + categoryKey);
			}

			categories.add(new ShopCategory(
					categorySection.getString("name", "Sans nom"),
					categorySection.getString("description", "Aucune description"),
					categoryIcon != null ? categoryIcon : new ItemStack(Material.STONE),
					items,
					categorySection.getInt("slot", 1),
					categorySection.getBoolean("shine", false)
			));
		}

		return categories;
	}

	public void navPreset(UniqueMinecraftGUI shopGui) {
		final ItemStack SEP = new ItemBuilder(new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 5))
				.name("§2-§a§k*§2-")
				.toItemStack();

		final int sepLine = (shopGui.getRows() + 1) * 9;
		for (int i = shopGui.getRows() * 9; i < sepLine; i++) {
			shopGui.setItem(i, SEP, true);
		}

		shopGui.setRows(shopGui.getRows() + 1);

		int index = (shopGui.getRows() - 1) * 9;
		shopGui.setItem(index + 1, SEP);
		shopGui.setItem(index + 7, SEP);
		shopGui.addActionItems(index + 8, index);
	}

	public List<String> getShopItemLore(ShopItem shopItem) {
		List<String> lore = Lists.newArrayList();
		if (shopItem.isBuyable()) {
			lore.add("§aClique gauche pour acheter");
			lore.add("§9» §3Prix d'achat: §e" + shopItem.getBuyPrice() + "$ §b/unité");
		}
		else {
			lore.add("§4Cet objet ne s'achète pas");
		}

		if (shopItem.isSellable()) {
			lore.add("§2Clique droit pour vendre");
			lore.add("§9» §3Prix de vente: §e" + shopItem.getSellPrice() + "$ §b/unité");
		}
		else {
			lore.add("§cCet objet ne se vend pas");
		}
		return lore;
	}

}
