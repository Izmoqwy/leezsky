package lz.izmoqwy.island.gui;

import com.google.common.collect.Maps;
import lz.izmoqwy.core.api.ItemBuilder;
import lz.izmoqwy.core.gui.MinecraftGUI;
import lz.izmoqwy.core.gui.MinecraftGUIListener;
import lz.izmoqwy.core.i18n.ItemNamer;
import lz.izmoqwy.core.i18n.LocaleManager;
import lz.izmoqwy.core.utils.MathUtil;
import lz.izmoqwy.island.generator.Ore;
import lz.izmoqwy.island.generator.OreGenerator;
import lz.izmoqwy.island.generator.OreGeneratorSettings;
import lz.izmoqwy.island.island.Island;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class OreGeneratorGUI extends MinecraftGUI implements MinecraftGUIListener {

	private static final Map<String, OreGeneratorGUI> current_shared = Maps.newHashMap();
	private static final OreGenerator oreGenerator = new OreGenerator();

	private Island island;

	private OreGeneratorGUI(MinecraftGUI parent, Island island) {
		super(parent, "§6Île §8» §eGénérateur", true);
		this.island = island;

		OreGeneratorSettings generatorSettings = oreGenerator.fromIsland(island);
		int neededRows = Math.max((int) Math.ceil(generatorSettings.getOres().size() / 9d), 2);

		setRows(2 + neededRows);
		setItem(4, new ItemBuilder(Material.COBBLESTONE)
				.name("§eGénérateur a minerais").quickEnchant()
				.appendLore(ChatColor.GRAY, "Modifier votre générateur")
				.toItemStack());

		setItem(7, new ItemBuilder(Material.BOOK)
				.name("§eInformations")
				.appendLore(ChatColor.LIGHT_PURPLE, "Pour plus d'informations", "faîtes " + ChatColor.DARK_PURPLE + "/docs generator")
				.toItemStack());
		setItem(8, new ItemBuilder(Material.EMERALD)
				.name("§eBoutique")
				.appendLore(ChatColor.GRAY, "Améliorer le générateur", "pour ajouter / améliorer certains blocs")
				.toItemStack());

		final ItemStack hr = new ItemBuilder(Material.STAINED_GLASS_PANE)
				.name("§7§kU_u").dyeColor(DyeColor.GRAY)
				.toItemStack();

		for (int i = 0; i < 18; i++) {
			if (!getSlots().containsKey(i))
				setItem(i, hr);
		}

		ItemNamer itemNamer = LocaleManager.getItemNamer();

		int slot = 18;
		for (Ore ore : generatorSettings.getOres()) {
			setItem(slot++, new ItemBuilder(ore.getType())
					.name("§a" + itemNamer.getDisplayName(ore.getType()))
					.appendLore(ChatColor.DARK_AQUA,
							"Apparition: " + ChatColor.AQUA + (ore.getSpawningValue() / 10f) + "/10",
							"Pourcentage: " + ChatColor.AQUA + MathUtil.roundDecimal(ore.getChance(), 1) + "%")
					.toItemStack());
		}

		addActionItems(0, 1);
		addListener(this);
		current_shared.put(island.ID, this);
	}

	@Override
	public void onClose(Player player) {
		if (getBukkitInventory().getViewers().size() <= 1) {
			current_shared.remove(island.ID);
		}
	}

	public static OreGeneratorGUI getGUIInstance(@NotNull Island island) {
		String sharedIdentifier = island.ID;
		if (current_shared.containsKey(sharedIdentifier))
			return current_shared.get(sharedIdentifier);

		return new OreGeneratorGUI(IslandSettingsMenuGUI.INSTANCE, island);
	}

}
