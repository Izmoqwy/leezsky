package lz.izmoqwy.island.grid;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lz.izmoqwy.core.api.ItemBuilder;
import lz.izmoqwy.core.api.database.exceptions.SQLActionImpossibleException;
import lz.izmoqwy.core.hooks.HooksManager;
import lz.izmoqwy.core.self.CorePrinter;
import lz.izmoqwy.core.utils.ItemUtil;
import lz.izmoqwy.core.utils.ServerUtil;
import lz.izmoqwy.core.utils.TextUtil;
import lz.izmoqwy.island.BorderAPI;
import lz.izmoqwy.island.LeezIsland;
import lz.izmoqwy.island.Locale;
import lz.izmoqwy.island.Storage;
import lz.izmoqwy.island.commands.AdminCommand;
import lz.izmoqwy.island.island.Island;
import lz.izmoqwy.island.island.IslandMember;
import lz.izmoqwy.island.island.IslandRole;
import lz.izmoqwy.island.island.permissions.CoopPermission;
import lz.izmoqwy.island.island.permissions.GeneralPermission;
import lz.izmoqwy.island.island.permissions.VisitorPermission;
import lz.izmoqwy.island.players.LeezSkyblockPlayer;
import lz.izmoqwy.island.players.OfflineSkyblockPlayer;
import lz.izmoqwy.island.players.SkyblockPlayer;
import lz.izmoqwy.island.players.Wrapper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IslandManager {

	private static Map<IslandPreset, File> schematics;
	private static int buffer = 0;

	private static final ItemStack[] STARTING_CHEST_CONTENTS;

	static {
		Inventory chestContent = Bukkit.createInventory(null, 3 * 9);

		/*
		Starting book
		 */
		ItemStack startingBook = ItemUtil.quickItem(Material.WRITTEN_BOOK);
		BookMeta bookMeta = (BookMeta) startingBook.getItemMeta();
		bookMeta.setTitle("§eQuelques notes...");

		List<String> pages = Lists.newArrayList(), lines = Lists.newArrayList();

		lines.add("Bienvenue sur cette île jeune aventurier. Ce livre vous explique les bases du skyblock et de bons moyens pour bien commencer.");
		lines.add("Si vous êtes déjà expérimenté en skyblock ou si vous voulez découvrir par vous même, je vous incite à ne pas lire ce livre.");
		lines.add("\nVous pouvez obtenir l'intégralité de ce livre en message en faisant '/help book'");
		pages.add(TextUtil.iterate(lines, String::new, "", "\n"));
		lines.clear();

		lines.add("");
		pages.add(TextUtil.iterate(lines, String::new, "", "\n"));
		lines.clear();

		bookMeta.setPages(pages);
		bookMeta.setAuthor("Leezsky");
		startingBook.setItemMeta(bookMeta);
		chestContent.setItem(10, startingBook);

		/*
		Starting box
		 */
		Inventory shulkerContent = Bukkit.createInventory(null, 3 * 9);

		shulkerContent.setItem(9, ItemUtil.quickItem(Material.CACTUS, 32));
		shulkerContent.setItem(10, ItemUtil.quickItem(Material.WOOD_STEP, 32));
		shulkerContent.setItem(11, ItemUtil.quickItem(Material.FENCE, 16));

		ItemStack startingBox = new ItemBuilder(Material.SILVER_SHULKER_BOX)
				.name("§7Un peu d'équipement...")
				.toItemStack();
		BlockStateMeta stateMeta = (BlockStateMeta) startingBox.getItemMeta();
		ShulkerBox shulkerBox = (ShulkerBox) stateMeta.getBlockState();
		shulkerBox.getInventory().setContents(shulkerContent.getContents());
		stateMeta.setBlockState(shulkerBox);
		shulkerBox.update();
		startingBox.setItemMeta(stateMeta);

		/*
		Chest content
		 */
		chestContent.setItem(11, startingBox);

		chestContent.setItem(14, ItemUtil.quickItem(Material.SAPLING, 8));
		chestContent.setItem(15, ItemUtil.quickItem(Material.LAVA_BUCKET));
		chestContent.setItem(16, ItemUtil.quickItem(Material.ICE, 2));

		STARTING_CHEST_CONTENTS = chestContent.getContents();
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	protected static void load() {
		final File dir = new File(LeezIsland.getInstance().getDataFolder(), "schematics/");
		if (!dir.exists())
			dir.mkdirs();

		schematics = Maps.newHashMap();
		for (IslandPreset preset : IslandPreset.values()) {
			File file = new File(dir, preset.getSchematicName() + ".schematic");
			if (file.exists())
				schematics.put(preset, file);
			else if (preset == IslandPreset.DEFAULT) {
				CorePrinter.warn("No overridden default schematic found. Copying the default one.");
				LeezIsland.getInstance().saveResource("schematics/default.schematic", false);
				if (!file.exists()) {
					CorePrinter.err("The default schematic hasn't been copied. There is no default schematic for the default preset!");
				}
				else
					schematics.put(preset, file);
			}
			else
				CorePrinter.warn("Unable to find the file \"{0}\" in the schematics/ folder. Associated preset will be ignored", file.getName());
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				if (buffer > 0) {
					buffer -= 20;
					if (buffer < 0)
						buffer = 0;
				}
			}
		}.runTaskTimerAsynchronously(LeezIsland.getInstance(), 20 * 5, 20);
	}

	public static void createNewIsland(Player creator, IslandPreset preset, boolean force) {
		if (!creator.isOnline())
			return;

		if (!force) {
			buffer += 60;
			if (buffer > 60) {
				Locale.PLAYER_ISLAND_CREATE_WAITING.send(creator);
				Bukkit.getScheduler().runTaskLater(LeezIsland.getInstance(), () -> {
					if (creator.isOnline())
						createNewIsland(creator, preset, true);
				}, buffer + 10);
				return;
			}
		}
		Locale.PLAYER_ISLAND_CREATE_STARTING.send(creator);

		Map.Entry<Integer, Integer> coords = GridManager.next();
		int x = coords.getKey(), z = coords.getValue();

		Map.Entry<Integer, Integer> middle = GridManager.getMiddle(x, z);
		int mx = middle.getKey() < 0 ? middle.getKey() : middle.getKey(),
				mz = middle.getValue() < 0 ? middle.getValue() : middle.getValue();

		final Location bedrock = new Location(GridManager.getWorld(), mx, 100, mz);
		final boolean originalFlightState = creator.getAllowFlight();
		if (!originalFlightState)
			creator.setAllowFlight(true);
		creator.setFlying(true);
		creator.teleport(new Location(bedrock.getWorld(), bedrock.getX(), 150, bedrock.getZ()));

		bedrock.getBlock().setType(Material.BEDROCK);
		if (!pasteIslandSchematic(bedrock, preset)) {
			Locale.PLAYER_ISLAND_CREATE_NOSCHEMATIC.send(creator);
		}

		bedrock.setY(115);
		final int raduis = 8, high = 8;

		Location home = null;
		Chest startingChest = null;

		loop:
		for (int x_ = raduis; x_ >= -raduis; x_--) {
			for (int y_ = high; y_ >= -high; y_--) {
				for (int z_ = raduis; z_ >= -raduis; z_--) {
					Block target = bedrock.getBlock().getRelative(x_, y_, z_);
					if (target.getType() == Material.SEA_LANTERN) {
						home = target.getLocation();
						home.setX(home.getX() + 0.5);
						home.setY(target.getLocation().getY() + 1);
						home.setZ(home.getZ() + 0.5);
						home.setYaw(145F);
						home.setPitch(0F);

						if (startingChest != null)
							break loop;
					}
					else if (target.getType() == Material.CHEST) {
						Locale.PLAYER_ISLAND_CREATE_FILLINGCHEST.send(creator);

						startingChest = (Chest) target.getState();
						startingChest.getInventory().setContents(STARTING_CHEST_CONTENTS);

						if (home != null)
							break loop;
					}
				}
			}
		}

		Island island = new Island(
				GridManager.getCurrentID(), creator.getUniqueId(), null, 0,
				home != null ? home : GridManager.getWorld().getHighestBlockAt(bedrock).getLocation(), mx, mz, (short) 50, false, Lists.newArrayList(), Lists.newArrayList(),
				Lists.newArrayList(VisitorPermission.DOORS, VisitorPermission.GATES, VisitorPermission.BUTTONS, VisitorPermission.LEVERS, VisitorPermission.DROP, VisitorPermission.PICKUP,
						VisitorPermission.PLATES, VisitorPermission.VILLAGERS, VisitorPermission.FLY),
				Lists.newArrayList(GeneralPermission.SPAWNERS, GeneralPermission.MOB_SPAWN, GeneralPermission.FLUID_FLOW, GeneralPermission.CUSTOM_GENERATOR),
				Lists.newArrayList(CoopPermission.BREAK, CoopPermission.PLACE, CoopPermission.CONTAINERS, CoopPermission.BUCKETS, CoopPermission.ACTIVATORS, CoopPermission.REDSTONE));
		creator.teleport(island.getHomeLocation());

		creator.setFlying(false);
		creator.setAllowFlight(originalFlightState);

		try {
			PreparedStatement statement = Storage.DB.prepare("INSERT INTO " + Storage.ISLANDS + "(island_id, leader, settings, general, members) VALUES (?, ?, ?, ?, ?)");
			statement.setString(1, island.ID);
			statement.setString(2, creator.getUniqueId().toString());
			statement.setString(3, island.serializePermissions());
			statement.setString(4, island.serializeData());
			statement.setString(5, island.serializeMembers());
			statement.execute();

			registerPlayerToIsland(creator.getUniqueId(), island);
			GridManager.saveGrid();
		}
		catch (SQLException | SQLActionImpossibleException e) {
			System.err.println("[LeezIsland] Erreur pendant la sauvegarde d'une nouvelle île, ID: " + island.ID);
			e.printStackTrace();

			Locale.COMMAND_ERROR.send(creator);
			return;
		}

		Wrapper.getPlayers().remove(creator.getUniqueId());
		GridManager.addToGrid(island);

		SkyblockPlayer wrappedPlayer = Wrapper.wrapPlayer(creator);
		if (wrappedPlayer != null) {
			BorderAPI.setOwnBorder(wrappedPlayer);
		}
		Locale.PLAYER_ISLAND_CREATE_FINISHED.send(creator);
	}

	private static boolean pasteIslandSchematic(Location bedrock, IslandPreset preset) {
		bedrock.setY(bedrock.getY() - 1);
		if (schematics.containsKey(preset))
			return HooksManager.worldedit().loadSchematic(schematics.get(preset), bedrock);
		else
			return false;
	}

	private static void registerPlayerToIsland(UUID uuid, Island island) throws SQLActionImpossibleException, SQLException {
		if (Storage.PLAYERS.hasResult("player_id", "player_id", uuid.toString())) {
			Storage.PLAYERS.setString("island_id", island.ID, "player_id", uuid.toString());
		}
		else {
			Storage.DB.execute("INSERT INTO " + Storage.PLAYERS + "(player_id, island_id) VALUES (\"" + uuid.toString() + "\", \"" + island.ID + "\")");
		}
	}

	public static List<Player> getPlayersOnIsland(Island island) {
		return Bukkit.getOnlinePlayers().stream().filter(player -> island.isInBounds(player.getLocation())).collect(Collectors.toList());
	}

	public static int expelEveryone(Island island) {
		Stream<? extends Player> toTeleport =
				Bukkit.getOnlinePlayers().stream().filter(player -> island.isInBounds(player.getLocation()) && !AdminCommand.BYPASSING.contains(player.getUniqueId()));

		toTeleport.forEach(player -> ServerUtil.performCommand("spawn " + player.getName()));
		return (int) toTeleport.count();
	}

	public static void setRange(Island island, short range) {
		island.setRange(range);
		island.saveGeneral();

		Wrapper.getIslands().replace(island.ID, island);
		for (Player player : getPlayersOnIsland(island)) {
			BorderAPI.setBorder(player, island);
			SkyblockPlayer skyblockPlayer = Wrapper.wrapPlayer(player);
			if (skyblockPlayer != null)
				skyblockPlayer.sendMessage("§a~~~Wow~~~, le rayon de l'île où vous êtes vient d'augmenter !");
		}
	}

	public static void setName(Island island, String name) {
		island.setName(name);
		try {
			Storage.ISLANDS.setString("name", name, "island_id", island.ID);
		}
		catch (SQLActionImpossibleException e) {
			e.printStackTrace();
		}
	}

	public static void clearPlayer(OfflineSkyblockPlayer player) {
		if (player.hasPersonalHome()) {
			player.setPersonalHome(null);
		}

		try {
			Storage.PLAYERS.setString("island_id", null, "player_id", player.getBaseId().toString());
		}
		catch (SQLActionImpossibleException e) {
			e.printStackTrace();
		}
		Wrapper.getPlayers().remove(player.getBaseId());
	}

	public static void kickMember(Island island, OfflinePlayer player) {
		if (island == null) {
			LeezIsland.logger.warning("Cannot kick member because island is null.");
			return;
		}

		OfflineSkyblockPlayer skyblockPlayer = Wrapper.getOfflinePlayer(player);
		if (island.getMembersMap().remove(player.getUniqueId()) != null) {
			island.saveMembers();
			clearPlayer(skyblockPlayer);
		}
		else if (skyblockPlayer.isOwnerOf(island)) {
			island.getMembersMap().values().forEach(member -> kickMember(island, member.toPlayer(island)));

			try {
				Storage.ISLANDS.delete("island_id", island.ID);
			}
			catch (SQLException e) {
				e.printStackTrace();
			}

			GridManager.removeFromGrid(island);
			CoopsManager.manager.unregisterIsland(island);
			Wrapper.getIslands().remove(island.ID);
			clearPlayer(skyblockPlayer);
		}

		// Todo: Notify island's members
	}

	public static void addMember(Island island, OfflinePlayer player) {
		if (island == null) {
			LeezIsland.logger.warning("Cannot add member because island is null.");
			return;
		}
		if (Wrapper.getOfflinePlayerIsland(player) != null) {
			LeezIsland.logger.warning("Tried to add the player " + player.getName() + " to a team but he already has an island.");
			return;
		}

		UUID playerId = player.getUniqueId();
		if (island.isCooped(player))
			CoopsManager.manager.unCoop(playerId, island, false);

		island.getBanList().remove(playerId);

		try {
			registerPlayerToIsland(playerId, island);

			island.getMembersMap().put(playerId, new IslandMember(playerId, IslandRole.MEMBER));
			island.saveMembers();

			Wrapper.getPlayers().remove(playerId);
		}
		catch (SQLActionImpossibleException | SQLException e) {
			e.printStackTrace();
		}

		// Todo: Notify island's members
	}

}
