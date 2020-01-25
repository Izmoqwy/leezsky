package lz.izmoqwy.leezisland.gui;

import lz.izmoqwy.core.api.ItemBuilder;
import lz.izmoqwy.core.gui.MinecraftGUI;
import lz.izmoqwy.core.gui.MinecraftGUIListener;
import lz.izmoqwy.leezisland.Locale;
import lz.izmoqwy.leezisland.island.Island;
import lz.izmoqwy.leezisland.island.IslandRole;
import lz.izmoqwy.leezisland.island.permissions.PermissionType;
import lz.izmoqwy.leezisland.players.SkyblockPlayer;
import lz.izmoqwy.leezisland.players.Wrapper;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class IslandSettingsMenuGUI extends MinecraftGUI implements MinecraftGUIListener {

	public static final IslandSettingsMenuGUI INSTANCE = new IslandSettingsMenuGUI();

	private IslandSettingsMenuGUI() {
		super(null, "§6Paramètres d'île", true);

		setRows(3);
		setItem(11, new ItemBuilder(Material.COBBLESTONE)
				.name("§6Paramètres du générateur")
				.toItemStack());
		setItem(12, new ItemBuilder(Material.BOOK)
				.name("§6Paramètres généraux")
				.toItemStack());

		setItem(14, new ItemBuilder(Material.BRICK)
				.name("§eParamètres des coopérants")
				.toItemStack());
		setItem(15, new ItemBuilder(Material.EMPTY_MAP)
				.name("§eParamètres des visiteurs")
				.toItemStack());

		addListener(this);
	}

	@Override
	public void onClick(Player player, ItemStack clickedItem, int slot) {
		SkyblockPlayer skyblockPlayer = Wrapper.wrapPlayer(player);
		if (skyblockPlayer == null)
			return;

		Island island = skyblockPlayer.getIsland();
		if (island == null) {
			skyblockPlayer.sendMessage(ChatColor.RED + "Vous n'avez plus d'île !");
			player.closeInventory();
			return;
		}

		if (!island.hasRoleOrAbove(player, IslandRole.OFFICIER)) {
			Locale.PLAYER_ISLAND_RANK_TOOLOW.send(player, IslandRole.OFFICIER);
			player.closeInventory();
			return;
		}

		switch (slot) {
			case 11:
				// todo : open generator GUI
				break;

			case 12:
				IslandSettingsGUI.getGUIInstance(island, PermissionType.GENERAL).open(player);
				break;
			case 14:
				IslandSettingsGUI.getGUIInstance(island, PermissionType.COOP).open(player);
				break;
			case 15:
				IslandSettingsGUI.getGUIInstance(island, PermissionType.VISITOR).open(player);
				break;
		}
	}

}
