package lz.izmoqwy.leezisland.listeners;

import lz.izmoqwy.core.utils.ItemUtil;
import lz.izmoqwy.leezisland.Locale;
import lz.izmoqwy.leezisland.grid.IslandManager;
import lz.izmoqwy.leezisland.island.*;
import lz.izmoqwy.leezisland.players.SkyblockPlayer;
import lz.izmoqwy.leezisland.players.Wrapper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.*;

public class SettingsMenuListener implements Listener {

	public static final String GUI_NAME = "§6Paramètres d'île",
			GENERAL_GUI_NAME = "§6Île §8» §eGénéral",
			VISITORS_GUI_NAME = "§6Île §8» §eVisiteurs",
			COOP_GUI_NAME = "§6Île §8» §eCoopérants";
	public static final Inventory GUI, GENERAL_GUI, VISITORS_GUI, COOP_GUI;

	static final Map<Integer, VisitorPermission> VISITORS_SLOTMAP;
	static final Map<Integer, GeneralPermission> GENERAL_SLOTMAP;
	static final Map<Integer, CoopPermission> COOP_SLOTMAP;

	private static final ItemStack BACK = ItemUtil.createItem(Material.ARROW, "§cRetour au menu", Collections.singletonList("§7Retourner à la page des paramètres"));

	static {
		GUI = Bukkit.createInventory(null, 3 * 9, GUI_NAME);
		GUI.setItem(11, ItemUtil.createItem(Material.EMPTY_MAP, "§eParamètres des visiteurs"));
		GUI.setItem(13, ItemUtil.createItem(Material.BOOK, "§6Paramètres généraux"));
		GUI.setItem(15, ItemUtil.createItem(Material.BRICK, "§eParamètres des coopérants"));

		GENERAL_GUI = Bukkit.createInventory(null, 3 * 9, GENERAL_GUI_NAME);
		GENERAL_GUI.setItem(26, BACK);

		VISITORS_GUI = Bukkit.createInventory(null, 5 * 9, VISITORS_GUI_NAME);
		VISITORS_GUI.setItem(44, BACK);

		COOP_GUI = Bukkit.createInventory(null, 4 * 9, COOP_GUI_NAME);
		COOP_GUI.setItem(35, BACK);

		VISITORS_SLOTMAP = new HashMap<Integer, VisitorPermission>() {
			{
				put(12, VisitorPermission.FLY);
				put(14, VisitorPermission.SETHOME);

				put(19, VisitorPermission.DOORS);
				put(20, VisitorPermission.GATES);

				put(22, VisitorPermission.VILLAGERS);

				put(24, VisitorPermission.DROP);
				put(25, VisitorPermission.PICKUP);

				put(29, VisitorPermission.REDSTONE);
				put(30, VisitorPermission.PLATES);

				put(32, VisitorPermission.BUTTONS);
				put(33, VisitorPermission.LEVERS);
			}
		};

		GENERAL_SLOTMAP = new HashMap<Integer, GeneralPermission>() {
			{
				put(10, GeneralPermission.SPAWNERS);
				put(11, GeneralPermission.MOBSPAWNING);

				put(15, GeneralPermission.FLUIDFLOWING);
				put(16, GeneralPermission.GENENABLED);
			}
		};
		COOP_SLOTMAP = new HashMap<Integer, CoopPermission>() {
			{
				put(10, CoopPermission.PLACE);
				put(11, CoopPermission.BREAK);

				put(13, CoopPermission.CHEST);

				put(15, CoopPermission.SHULKER_BOX);
				put(16, CoopPermission.CONTAINERS);

				put(20, CoopPermission.BUCKETS);
				put(21, CoopPermission.FIRE);

				put(23, CoopPermission.REDSTONE);
				put(24, CoopPermission.ACTIONNERS);
			}
		};
	}

	private ItemStack boolVal(MaterialData icon, String name, String description, boolean value) {
		List<String> lore;
		if (description != null)
			lore = Arrays.asList("§7" + description, "§6Etat: " + (value ? "§aactivé" : "§cdésactivé"));
		else
			lore = Collections.singletonList("§6Etat: " + (value ? "§aactivé" : "§cdésactivé"));
		return ItemUtil.createItem(icon, "§e" + name, lore);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onClick(final InventoryClickEvent event) {
		if (event.getClickedInventory() == null || event.getWhoClicked() == null)
			return;

		final String inventoryName = event.getClickedInventory().getName();
		final int slot = event.getSlot();
		switch (inventoryName) {
			case GUI_NAME: {
				event.setCancelled(true);
				if (event.getCurrentItem() == null)
					return;

				SkyblockPlayer player = Wrapper.wrapPlayer((Player) event.getWhoClicked());
				if (player == null)
					return;
				Island island = player.getIsland();
				if (island == null) {
					Locale.PLAYER_ISLAND_NONE.send(player);
					player.bukkit().closeInventory();
					return;
				}
				if (!island.hasRoleOrAbove(player, IslandRole.OFFICIER)) {
					Locale.PLAYER_ISLAND_RANK_TOOLOW.send(player, IslandRole.OFFICIER);
					player.bukkit().closeInventory();
					return;
				}

				switch (slot) {
					case 11:
						Inventory inventory = VISITORS_GUI;

						List<VisitorPermission> visitorPermissions = island.getVisitorsPermissions();
						VISITORS_SLOTMAP.forEach((itemSlot, permission) -> inventory.setItem(itemSlot, boolVal(permission.getIcon(), permission.getTitle(), permission.getDescription(), visitorPermissions.contains(permission))));

						player.bukkit().openInventory(inventory);
						break;
					case 13:
						inventory = GENERAL_GUI;

						List<GeneralPermission> generalPermissions = island.getGeneralPermissions();
						GENERAL_SLOTMAP.forEach((itemSlot, permission) -> inventory.setItem(itemSlot, boolVal(permission.getIcon(), permission.getTitle(), permission.getDescription(), generalPermissions.contains(permission))));

						player.bukkit().openInventory(inventory);
						break;
					case 15:
						inventory = COOP_GUI;

						List<CoopPermission> coopPermissions = island.getCoopPermissions();
						COOP_SLOTMAP.forEach((itemSlot, permission) -> inventory.setItem(itemSlot, boolVal(permission.getIcon(), permission.getTitle(), permission.getDescription(), coopPermissions.contains(permission))));

						player.bukkit().openInventory(inventory);
						break;
				}
				break;
			}
			case VISITORS_GUI_NAME: {
				event.setCancelled(true);
				if (event.getCurrentItem() == null)
					return;

				if (event.getCurrentItem().isSimilar(BACK)) {
					event.getWhoClicked().openInventory(GUI);
					return;
				}

				SkyblockPlayer player = Wrapper.wrapPlayer((Player) event.getWhoClicked());
				if (player == null)
					return;
				Island island = player.getIsland();
				if (island == null) {
					Locale.PLAYER_ISLAND_NONE.send(player);
					player.bukkit().closeInventory();
					return;
				}
				if (!island.hasRoleOrAbove(player, IslandRole.OFFICIER)) {
					Locale.PLAYER_ISLAND_RANK_TOOLOW.send(player, IslandRole.OFFICIER);
					player.bukkit().closeInventory();
					return;
				}

				VisitorPermission permission = VISITORS_SLOTMAP.get(slot);
				if (permission == null)
					return;

				if (island.hasVisitorPermission(permission)) {
					island.getVisitorsPermissions().remove(permission);
				}
				else {
					island.getVisitorsPermissions().add(permission);
				}
				island.updatePermissions();

				event.getClickedInventory().setItem(slot, boolVal(permission.getIcon(), permission.getTitle(), permission.getDescription(), island.hasVisitorPermission(permission)));
				break;
			}
			case GENERAL_GUI_NAME: {
				event.setCancelled(true);
				if (event.getCurrentItem() == null)
					return;

				if (event.getCurrentItem().isSimilar(BACK)) {
					event.getWhoClicked().openInventory(GUI);
					return;
				}

				SkyblockPlayer player = Wrapper.wrapPlayer((Player) event.getWhoClicked());
				if (player == null)
					return;
				Island island = player.getIsland();
				if (island == null) {
					Locale.PLAYER_ISLAND_NONE.send(player);
					player.bukkit().closeInventory();
					return;
				}
				if (!island.hasRoleOrAbove(player, IslandRole.OFFICIER)) {
					Locale.PLAYER_ISLAND_RANK_TOOLOW.send(player, IslandRole.OFFICIER);
					player.bukkit().closeInventory();
					return;
				}

				GeneralPermission permission = GENERAL_SLOTMAP.get(slot);
				if (permission == null)
					return;

				if (island.hasGeneralPermission(permission)) {
					island.getGeneralPermissions().remove(permission);
				}
				else {
					island.getGeneralPermissions().add(permission);
				}
				island.updatePermissions();

				event.getClickedInventory().setItem(slot, boolVal(permission.getIcon(), permission.getTitle(), permission.getDescription(), island.hasGeneralPermission(permission)));
				if (permission == GeneralPermission.FLUIDFLOWING) {
					if (island.hasGeneralPermission(GeneralPermission.FLUIDFLOWING))
						IslandManager.broadcast(island, "§6L'écoulement des fluides à été réactivé sur votre île. les liquides déjà présents ne sont pas mis à jour automatiquement. Si vous voulez faire coulez un liquide déjà présent, posez un bloc à côté ou casser en un pour mettre à jour le liquide.");
				}
				break;
			}
			case COOP_GUI_NAME: {
				event.setCancelled(true);
				if (event.getCurrentItem() == null)
					return;

				if (event.getCurrentItem().isSimilar(BACK)) {
					event.getWhoClicked().openInventory(GUI);
					return;
				}

				SkyblockPlayer player = Wrapper.wrapPlayer((Player) event.getWhoClicked());
				if (player == null)
					return;
				Island island = player.getIsland();
				if (island == null) {
					Locale.PLAYER_ISLAND_NONE.send(player);
					player.bukkit().closeInventory();
					return;
				}
				if (!island.hasRoleOrAbove(player, IslandRole.OFFICIER)) {
					Locale.PLAYER_ISLAND_RANK_TOOLOW.send(player, IslandRole.OFFICIER);
					player.bukkit().closeInventory();
					return;
				}

				CoopPermission permission = COOP_SLOTMAP.get(slot);
				if (permission == null)
					return;

				if (island.hasCoopPermission(permission)) {
					island.getCoopPermissions().remove(permission);
				}
				else {
					island.getCoopPermissions().add(permission);
				}
				island.updatePermissions();

				event.getClickedInventory().setItem(slot, boolVal(permission.getIcon(), permission.getTitle(), permission.getDescription(), island.hasCoopPermission(permission)));
				break;
			}
		}
	}

}
