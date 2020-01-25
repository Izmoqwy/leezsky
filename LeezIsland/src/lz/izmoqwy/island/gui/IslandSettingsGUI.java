package lz.izmoqwy.island.gui;

import com.google.common.collect.Maps;
import lz.izmoqwy.core.api.ItemBuilder;
import lz.izmoqwy.core.gui.MinecraftGUI;
import lz.izmoqwy.core.gui.MinecraftGUIListener;
import lz.izmoqwy.island.Locale;
import lz.izmoqwy.island.island.Island;
import lz.izmoqwy.island.island.IslandRole;
import lz.izmoqwy.island.island.permissions.CoopPermission;
import lz.izmoqwy.island.island.permissions.GeneralPermission;
import lz.izmoqwy.island.island.permissions.PermissionType;
import lz.izmoqwy.island.island.permissions.VisitorPermission;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class IslandSettingsGUI extends MinecraftGUI implements MinecraftGUIListener {

	private static final Map<String, IslandSettingsGUI> current_shared = Maps.newHashMap();

	private static final Map<Integer, VisitorPermission> VISITORS_SLOTMAP = new HashMap<Integer, VisitorPermission>() {{
		put(10, VisitorPermission.DOORS);
		put(11, VisitorPermission.GATES);
		put(13, VisitorPermission.VILLAGERS);
		put(15, VisitorPermission.DROP);
		put(16, VisitorPermission.PICKUP);

		put(20, VisitorPermission.REDSTONE);
		put(21, VisitorPermission.PLATES);
		put(23, VisitorPermission.BUTTONS);
		put(24, VisitorPermission.LEVERS);

		put(28, VisitorPermission.HITMOBS);
		put(29, VisitorPermission.HITGOLEMS);
		put(31, VisitorPermission.RIDING);
		put(33, VisitorPermission.HITANIMALS);
		put(34, VisitorPermission.USE_LEASH);

		put(38, VisitorPermission.USE_BOW);
		put(39, VisitorPermission.FISH);
		put(41, VisitorPermission.FLY);
		put(42, VisitorPermission.SETHOME);
	}};
	private static final Map<Integer, GeneralPermission> GENERAL_SLOTMAP = new HashMap<Integer, GeneralPermission>() {{
		put(10, GeneralPermission.SPAWNERS);
		put(11, GeneralPermission.MOB_SPAWN);
		put(15, GeneralPermission.FLUID_FLOW);
		put(16, GeneralPermission.CUSTOM_GENERATOR);
	}};
	private static final Map<Integer, CoopPermission> COOP_SLOTMAP = new HashMap<Integer, CoopPermission>() {{
		put(10, CoopPermission.PLACE);
		put(11, CoopPermission.BREAK);
		put(13, CoopPermission.CHEST);
		put(15, CoopPermission.SHULKER_BOX);
		put(16, CoopPermission.CONTAINERS);

		put(20, CoopPermission.BUCKETS);
		put(21, CoopPermission.FIRE);
		put(23, CoopPermission.REDSTONE);
		put(24, CoopPermission.ACTIVATORS);
	}};

	private Island island;
	private PermissionType permissionType;

	private IslandSettingsGUI(MinecraftGUI parent, Island island, PermissionType permissionType) {
		super(parent, "§6Île §8» §e" + permissionType.getDisplayName(), true);
		this.island = island;
		this.permissionType = permissionType;

		switch (permissionType) {
			case GENERAL:
				addActionItems(-1, 26);

				List<GeneralPermission> generalPermissions = island.getGeneralPermissions();
				GENERAL_SLOTMAP.forEach((itemSlot, permission) -> setItem(itemSlot, boolVal(
						permission.getIcon(), permission.getTitle(), permission.getDescription(), generalPermissions.contains(permission))));
				break;
			case COOP:
				addActionItems(-1, 35);

				List<CoopPermission> coopPermissions = island.getCoopPermissions();
				COOP_SLOTMAP.forEach((itemSlot, permission) -> setItem(itemSlot, boolVal(
						permission.getIcon(), permission.getTitle(), permission.getDescription(), coopPermissions.contains(permission))));
				break;
			case VISITOR:
				addActionItems(-1, 53);

				List<VisitorPermission> visitorPermissions = island.getVisitorsPermissions();
				VISITORS_SLOTMAP.forEach((itemSlot, permission) -> setItem(itemSlot, boolVal(
						permission.getIcon(), permission.getTitle(), permission.getDescription(), visitorPermissions.contains(permission))));
				break;
		}

		addListener(this);
		current_shared.put(getSharedIdentifier(), this);
	}

	@Override
	public void onClick(Player player, ItemStack clickedItem, int slot) {
		if (!island.hasRoleOrAbove(player, IslandRole.OFFICIER)) {
			Locale.PLAYER_ISLAND_RANK_TOOLOW.send(player, IslandRole.OFFICIER);
			player.closeInventory();
			return;
		}

		switch (permissionType) {
			case GENERAL:
				GeneralPermission generalPermission = GENERAL_SLOTMAP.get(slot);
				if (generalPermission == null)
					return;

				if (!island.getGeneralPermissions().remove(generalPermission)) {
					island.getGeneralPermissions().add(generalPermission);
				}
				island.savePermissions();

				setItem(slot, boolVal(generalPermission.getIcon(), generalPermission.getTitle(), generalPermission.getDescription(), island.hasGeneralPermission(generalPermission)));
				if (generalPermission == GeneralPermission.FLUID_FLOW && island.hasGeneralPermission(GeneralPermission.FLUID_FLOW)) {
					island.broadcast("§6L'écoulement des fluides à été réactivé sur votre île. les liquides déjà présents ne sont pas mis à jour automatiquement. " +
							"Si vous voulez faire coulez un liquide déjà présent, posez un bloc à côté ou casser en un pour mettre à jour le liquide.");
				}
				break;

			case COOP:
				CoopPermission coopPermission = COOP_SLOTMAP.get(slot);
				if (coopPermission == null)
					return;

				if (!island.getCoopPermissions().remove(coopPermission)) {
					island.getCoopPermissions().add(coopPermission);
				}
				island.savePermissions();

				setItem(slot, boolVal(coopPermission.getIcon(), coopPermission.getTitle(), coopPermission.getDescription(), island.hasCoopPermission(coopPermission)));
				break;

			case VISITOR:
				VisitorPermission visitorPermission = VISITORS_SLOTMAP.get(slot);
				if (visitorPermission == null)
					return;

				if (!island.getVisitorsPermissions().remove(visitorPermission)) {
					island.getVisitorsPermissions().add(visitorPermission);
				}
				island.savePermissions();

				setItem(slot, boolVal(visitorPermission.getIcon(), visitorPermission.getTitle(), visitorPermission.getDescription(), island.hasVisitorPermission(visitorPermission)));
				break;
		}
	}

	@Override
	public void onClose(Player player) {
		if (getBukkitInventory().getViewers().size() <= 1) {
			current_shared.remove(getSharedIdentifier());
		}
	}

	private String getSharedIdentifier() {
		return island.ID + permissionType.ordinal();
	}

	public static IslandSettingsGUI getGUIInstance(@NotNull Island island, @NotNull PermissionType permissionType) {
		String sharedIdentifier = island.ID + permissionType.ordinal();
		if (current_shared.containsKey(sharedIdentifier))
			return current_shared.get(sharedIdentifier);

		return new IslandSettingsGUI(IslandSettingsMenuGUI.INSTANCE, island, permissionType);
	}

	private static ItemStack boolVal(MaterialData icon, String name, String description, boolean value) {
		ItemBuilder itemBuilder = new ItemBuilder(icon)
				.name(ChatColor.YELLOW + name);

		if (description != null)
			itemBuilder.appendLore(ChatColor.GRAY, description);
		itemBuilder.appendLore("§6État: " + (value ? ChatColor.GREEN + "activé" : ChatColor.RED + "désactivé"));

		return itemBuilder.toItemStack();
	}

}
