package lz.izmoqwy.market.blackmarket;

import lz.izmoqwy.core.PlayerDataStorage;
import lz.izmoqwy.core.api.database.exceptions.SQLActionImpossibleException;
import lz.izmoqwy.core.utils.ItemUtil;
import lz.izmoqwy.core.utils.TextUtil;
import lz.izmoqwy.market.Locale;
import lz.izmoqwy.market.blackmarket.stuff.StuffBase;
import lz.izmoqwy.market.rpg.RPGManager;
import lz.izmoqwy.market.rpg.RPGPlayer;
import lz.izmoqwy.market.rpg.RPGResource;
import lz.izmoqwy.market.rpg.RPGStorage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@SuppressWarnings("deprecation")
public class BlackMarketGUI implements Listener {

	protected static final String GUI_ACCESS_NAME = "§6§lMN §8» §aAcheter l'accès", GUI_CONFIRM_NAME = "§6§lMN §8» §eAchat", GUI_MENU_NAME = "§6§lMarché noir",
			GUI_ILLEGAL_NAME = "§6§lMN §8» §c§kIllégal", GUI_FORGE_NAME = "§6§lMN §8» §7Forge en titane";
	protected static final List<String> GUIS = Arrays.asList(GUI_ACCESS_NAME, GUI_CONFIRM_NAME, GUI_MENU_NAME, GUI_ILLEGAL_NAME, GUI_FORGE_NAME);

	protected static final Inventory GUI_MENU, GUI_ILLEGAL, GUI_FORGE;
	protected static final ItemStack ITEM_BACK = ItemUtil.createItem(Material.ARROW, "§cRetour", Collections.singletonList("§7Retourner au menu"));

	static {
		GUI_MENU = Bukkit.createInventory(null, 3 * 9, GUI_MENU_NAME);
		GUI_MENU.setItem(10, ItemUtil.createItem(Material.DRAGON_EGG, "§3Hyper Générateur", Collections.singletonList("§eUtilisez la " + RPGResource.DARKMATTER.getFullName() + " §epour obtenir des objets")));
		GUI_MENU.setItem(11, ItemUtil.createItem(Material.SUGAR, "§c§kIllégal", Collections.singletonList("§cNe vous faîtes pas repérer en trainant ici !")));

		GUI_MENU.setItem(14, ItemUtil.createItem(Material.ANVIL, "§7Forge en titane", Collections.singletonList("§eUtilisez le " + RPGResource.TITANE.getFullName() + " §epour obtenir des équipements")));
		GUI_MENU.setItem(15, ItemUtil.createItem(Material.LIME_GLAZED_TERRACOTTA, "§aRéacteur nucléaire", Collections.singletonList("§aCet endroit semble dangereux")));
		GUI_MENU.setItem(16, ItemUtil.createItem(Material.REDSTONE_LAMP_OFF, "§cCentrale éléctrique", Collections.singletonList("§eVendez le " + RPGResource.COPPER.getFullName() + " §epour obtenir de l'argent")));

		GUI_ILLEGAL = Bukkit.createInventory(null, 3 * 9, GUI_ILLEGAL_NAME);

		GUI_ILLEGAL.setItem(13, ItemUtil.createItem(Material.GOLD_SWORD, "§cArène interdite",
				Arrays.asList("§7Voulez-vous faire un combat", "§7dans l'arène interdite ?", " ",
						"§2Il vous sera demandé de confirmer", "§3Prix: " + RPGResource.DARKMATTER.getFullName(TextUtil.readbleNumber(1000)))));

		GUI_ILLEGAL.setItem(3 * 9 - 1, ITEM_BACK);

		GUI_FORGE = Bukkit.createInventory(null, 4 * 9, GUI_FORGE_NAME);

		// Titane stuff
		GUI_FORGE.setItem(10, BMStuff.TITANE_HELMET.getPresentationItem());
		GUI_FORGE.setItem(11, BMStuff.TITANE_CHESTPLATE.getPresentationItem());
		GUI_FORGE.setItem(12, BMStuff.TITANE_LEGGINGS.getPresentationItem());
		GUI_FORGE.setItem(13, BMStuff.TITANE_BOOTS.getPresentationItem());

		GUI_FORGE.setItem(15, BMStuff.TITANE_SWORD.getPresentationItem());
		GUI_FORGE.setItem(16, BMStuff.TITANE_SHIELD.getPresentationItem());

		GUI_FORGE.setItem(21, BMStuff.TITANE_PICKAXE.getPresentationItem());
		GUI_FORGE.setItem(22, BMStuff.TITANE_AXE.getPresentationItem());
		GUI_FORGE.setItem(23, BMStuff.TITANE_SHOVEL.getPresentationItem());

		GUI_FORGE.setItem(4 * 9 - 1, ITEM_BACK);
	}

	public static void openAccessInventory(Player player) {
		Inventory inventory = Bukkit.createInventory(null, 3 * 9, GUI_ACCESS_NAME);

		try {
			RPGPlayer rpgPlayer = RPGManager.loadRPGPlayer(player.getUniqueId(), false).getKey();
			if (rpgPlayer.getRes_darkmatter() >= 250) {
				inventory.setItem(11, ItemUtil.createItem(new MaterialData(Material.WOOL, (byte) 5), "§aAcheter", Arrays.asList("§2Cela débloque le marché noir", "§2de manière permanente")));
				inventory.setItem(13, ItemUtil.createItem(Material.BOOK, "§eInformations",
						Arrays.asList("§aVoulez-vous débloquer l'accès au marché noir ?", "§ePrix: " + RPGResource.DARKMATTER.getFullName(Integer.toString(80)))));
				inventory.setItem(15, ItemUtil.createItem(new MaterialData(Material.WOOL, (byte) 14), "§cAnnuler"));
			}
			else {
				inventory.setItem(13, ItemUtil.createItem(Material.BARRIER, "§cRessources inssufisantes",
						Arrays.asList("§CVous n'avez pas de quoi débloquer le marché noir !", "§7Revenez quand vous aurez " + RPGResource.DARKMATTER.getFullName(Integer.toString(250)) + "§7.")));
			}
		}
		catch (SQLException | SQLActionImpossibleException e) {
			e.printStackTrace();
			player.closeInventory();
			player.sendMessage(Locale.PREFIX + "§4Une erreur est survenue !");
		}

		player.openInventory(inventory);
	}

	private static Map<HumanEntity, StuffBase> awaitConfirm = new HashMap<>();

	public static void openConfirmGUI(Player player, StuffBase stuff) {
		Inventory inventory = Bukkit.createInventory(null, 3 * 9, GUI_CONFIRM_NAME);
		inventory.setItem(11, ItemUtil.createItem(new MaterialData(Material.WOOL, (byte) 5), "§aAcheter",
				Collections.singletonList("§9» §3Prix: §r" + stuff.getCostRes().getFullName(TextUtil.readbleNumber(stuff.getCost())))));

		ItemStack informations = stuff.getPresentationItem().clone();
		ItemMeta informationsMeta = informations.getItemMeta();

		informationsMeta.setDisplayName("§eConfirmation d'achat");
		informationsMeta.setLore(Arrays.asList("§6Voulez-vous acheter l'objet suivant:", "§6» " + stuff.getPresentationItem().getItemMeta().getDisplayName() + "§6 ?"));

		informations.setItemMeta(informationsMeta);
		inventory.setItem(13, informations);

		inventory.setItem(15, ItemUtil.createItem(new MaterialData(Material.WOOL, (byte) 14), "§cAnnuler"));
		player.openInventory(inventory);
		awaitConfirm.put(player, stuff);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onClick(final InventoryClickEvent event) {
		if (event.getClickedInventory() == null || event.getWhoClicked() == null)
			return;

		if (event.getClickedInventory() == event.getWhoClicked().getInventory()) {
			HumanEntity human = event.getWhoClicked();
			if (human.getOpenInventory() != null && GUIS.contains(human.getOpenInventory().getTitle()))
				return;
		}

		final String inventoryName = event.getClickedInventory().getName();
		if (GUIS.contains(inventoryName)) {
			event.setCancelled(true);
			if (event.getCurrentItem() == null)
				return;

			Player player = (Player) event.getWhoClicked();
			if (event.getCurrentItem().isSimilar(ITEM_BACK)) {
				player.openInventory(GUI_MENU);
				return;
			}

			switch(inventoryName) {
				case GUI_ACCESS_NAME:
					switch (event.getSlot()) {
						case 11:
							try {
								RPGStorage.PLAYERS.decrease(RPGResource.DARKMATTER.dbCol(), 250, "uuid", player.getUniqueId().toString());
								PlayerDataStorage.set(player, "blackmarket.access", true);
								PlayerDataStorage.save(player);
								player.sendMessage(Locale.RPG_PREFIX + "§aVous avez débloqué l'accès du PNJ du marché noir. Attention à ne pas vous faire remarquer dans le coin !");
							}
							catch (SQLActionImpossibleException | IOException e) {
								e.printStackTrace();
								player.sendMessage(Locale.PREFIX + "§4Une erreur est survenue !");
							}
							player.closeInventory();
							break;
						case 15:
							player.closeInventory();
							break;
					}
					break;
				case GUI_CONFIRM_NAME:
					if (awaitConfirm.containsKey(player)) {
						switch (event.getSlot()) {
							case 11:
								// Todo: Check if still has resources, remove ress and give item
								ItemUtil.giveItems(player, awaitConfirm.get(player).getItem());
								awaitConfirm.remove(player);
								player.closeInventory();
								break;
							case 15:
								// Todo: Go back to actual previous menu
								awaitConfirm.remove(player);
								player.openInventory(GUI_MENU);
								break;
						}
					}
					break;
				case GUI_MENU_NAME:
					switch (event.getSlot()) {
						case 11:
							player.openInventory(GUI_ILLEGAL);
							break;
						case 14:
							player.openInventory(GUI_FORGE);
							break;
					}
					break;
				case GUI_FORGE_NAME:
					switch (event.getSlot()) {
						case 10:
							openConfirmGUI(player, BMStuff.TITANE_HELMET);
							break;
						case 11:
							openConfirmGUI(player, BMStuff.TITANE_CHESTPLATE);
							break;
						case 12:
							openConfirmGUI(player, BMStuff.TITANE_LEGGINGS);
							break;
						case 13:
							openConfirmGUI(player, BMStuff.TITANE_BOOTS);
							break;

						case 15:
							openConfirmGUI(player, BMStuff.TITANE_SWORD);
							break;
						case 16:
							openConfirmGUI(player, BMStuff.TITANE_SHIELD);
							break;

						case 21:
							openConfirmGUI(player, BMStuff.TITANE_PICKAXE);
							break;
						case 22:
							openConfirmGUI(player, BMStuff.TITANE_AXE);
							break;
						case 23:
							openConfirmGUI(player, BMStuff.TITANE_SHOVEL);
							break;
					}
					break;
			}
		}
	}

	@EventHandler
	public void onClose(InventoryCloseEvent event) {
		awaitConfirm.remove(event.getPlayer());
	}

}
