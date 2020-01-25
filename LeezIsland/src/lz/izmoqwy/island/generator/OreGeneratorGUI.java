package lz.izmoqwy.island.generator;

import lz.izmoqwy.core.api.ItemBuilder;
import lz.izmoqwy.core.gui.GUIManager;
import lz.izmoqwy.core.i18n.ItemNamer;
import lz.izmoqwy.core.i18n.LocaleManager;
import lz.izmoqwy.island.island.Island;
import lz.izmoqwy.island.players.SkyblockPlayer;
import lz.izmoqwy.island.players.Wrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class OreGeneratorGUI {

	private OreGenerator oreGenerator;

	public OreGeneratorGUI() {
		this.oreGenerator = new OreGenerator();

		GUIManager.registerInventory("§3Generateur", new GUIManager.GUIActionsBuilder().build());
	}

	public Inventory bakeInventory(Player player) {
		SkyblockPlayer skyblockPlayer = Wrapper.wrapPlayer(player);
		if (skyblockPlayer == null)
			return null;

		Island island = skyblockPlayer.getIsland();
		if (island == null) {
			skyblockPlayer.sendMessage("§cUne erreur est survenue lors de la génération de l'inventaire du générateur modifié, vous n'êtes sur aucune ile!");
			return null;
		}

		OreGeneratorSettings generatorSettings = oreGenerator.fromIsland(island);
		int neededRows = Math.max((int) Math.ceil(generatorSettings.getOres().size() / 9d), 2);
		if (neededRows > 4)
			return null;

		Inventory inventory = Bukkit.createInventory(null, 2 * 9 + neededRows * 9, "§3Générateur");

		inventory.setItem(0, new ItemBuilder(Material.DARK_OAK_DOOR_ITEM).name("§cQuitter").appendLore(ChatColor.GRAY, "Fermer cette interface").toItemStack());
		inventory.setItem(1, new ItemBuilder(Material.ARROW).name("§cRetour").appendLore(ChatColor.GRAY, "Retourner en arrière").toItemStack());
		inventory.setItem(4, new ItemBuilder(Material.COBBLESTONE).name("§eGenerateur a minerais").appendLore(ChatColor.GRAY, "Peppa Pig!").quickEnchant().toItemStack());
		inventory.setItem(7, new ItemBuilder(Material.BOOK).name("§eInformations").appendLore(ChatColor.GRAY, "Obtenir le lien du wiki", "pour en savoir plus sur le generateur").toItemStack());
		inventory.setItem(8, new ItemBuilder(Material.EMERALD).name("§eBoutique").appendLore(ChatColor.GRAY, "Ameliorer le generateur", "pour ajouter/ameliorer certains blocs").toItemStack());

		final ItemStack hr = new ItemBuilder(Material.STAINED_GLASS_PANE).dyeColor(DyeColor.GRAY).name("§7§kU_u").toItemStack();
		for (int i = 0; i < 18; i++) {
			if (inventory.getItem(i) == null)
				inventory.setItem(i, hr);
		}

		ItemNamer itemNamer = LocaleManager.getItemNamer();

		int slot = 18;
		for (Material ore : generatorSettings.getOres().keySet()) {
			inventory.setItem(slot++, new ItemBuilder(ore).name("§a" + itemNamer.getDisplayName(ore)).toItemStack());
		}

		return inventory;
	}

}
