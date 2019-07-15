package lz.izmoqwy.leezisland.grid;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lz.izmoqwy.core.CorePrinter;
import lz.izmoqwy.core.api.database.exceptions.SQLActionImpossibleException;
import lz.izmoqwy.core.helpers.PluginHelper;
import lz.izmoqwy.core.hooks.HooksManager;
import lz.izmoqwy.core.utils.ItemUtil;
import lz.izmoqwy.core.utils.TextUtil;
import lz.izmoqwy.leezisland.BorderAPI;
import lz.izmoqwy.leezisland.LeezIsland;
import lz.izmoqwy.leezisland.Locale;
import lz.izmoqwy.leezisland.Storage;
import lz.izmoqwy.leezisland.commands.AdminCommand;
import lz.izmoqwy.leezisland.island.*;
import lz.izmoqwy.leezisland.players.LeezIslandPlayer;
import lz.izmoqwy.leezisland.players.OfflineSkyblockPlayer;
import lz.izmoqwy.leezisland.players.SkyblockPlayer;
import lz.izmoqwy.leezisland.players.Wrapper;
import org.bukkit.*;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class IslandManager {

	private static Map<IslandPreset, File> schematics;
	private static int buffer = 0;

	private static final ItemStack[] STARTING_CHEST_CONTENTS;
	static {
		Inventory inventory = Bukkit.createInventory(null, 3 * 9);

		ItemStack starting_book = ItemUtil.createItem(Material.WRITTEN_BOOK);
		BookMeta bookMeta = (BookMeta) starting_book.getItemMeta();
		bookMeta.setTitle("§eQuelques notes...");

		List<String> pages = Lists.newArrayList(), lines = Lists.newArrayList();

		/*
			Explaination (1 page)
		 */
		lines.add("Bienvenue sur cette île jeune aventurier. Ce livre vous explique les bases du skyblock et de bons moyens pour bien commencer.");
		lines.add("Si vous êtes déjà expérimenté en skyblock ou si vous voulez découvrir par vous même, je vous incite à ne pas lire ce livre.");
		lines.add("\nVous pouvez obtenir l'intégralité de ce livre en message en faisant '/help book'");
		pages.add(TextUtil.iterate(lines, String::new, "", "\n"));
		lines.clear();

		/*
			Best ways to start
		 */

		/*
			Farms
		 */
		lines.add("");
		pages.add(TextUtil.iterate(lines, String::new, "", "\n"));
		lines.clear();

		bookMeta.setPages(pages);

		bookMeta.setAuthor("LeezSky");
		starting_book.setItemMeta(bookMeta);
		inventory.setItem(10, starting_book);

		Inventory starting_box_content = Bukkit.createInventory(null, 3 * 9);
		starting_box_content.setItem(9, ItemUtil.createItem(Material.CACTUS, 32));
		starting_box_content.setItem(10, ItemUtil.createItem(Material.WOOD_STEP, 32));
		starting_box_content.setItem(11, ItemUtil.createItem(Material.FENCE, 16));

		ItemStack starting_box = ItemUtil.createItem(Material.SILVER_SHULKER_BOX, "§7Un peu d'équipement...");
		BlockStateMeta stateMeta = (BlockStateMeta) starting_box.getItemMeta();
		ShulkerBox shulkerBox = (ShulkerBox) stateMeta.getBlockState();
		shulkerBox.getInventory().setContents(starting_box_content.getContents());
		stateMeta.setBlockState(shulkerBox);
		shulkerBox.update();
		starting_box.setItemMeta(stateMeta);
		inventory.setItem(11, starting_box);

		inventory.setItem(14, ItemUtil.createItem(Material.SAPLING, 8));
		inventory.setItem(15, ItemUtil.createItem(Material.LAVA_BUCKET));
		inventory.setItem(16, ItemUtil.createItem(Material.ICE, 2));

		STARTING_CHEST_CONTENTS = inventory.getContents();
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
				CorePrinter.warn("No overriden default schematic found. Copying the default one.");
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

	public static void teleportToSpawn(Player player) {
		PluginHelper.performCommand("spawn " + player.getName());
	}

	public static int expelPlayers(Island island) {
		int count = 0;
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (island.isInBounds(player.getLocation()) && !island.hasAccess(player) && !AdminCommand.BYPASSING.contains(player.getUniqueId())) {
				teleportToSpawn(player);
				count++;
			}
		}
		return count;
	}

	private static void registerPlayerToIsland(UUID uuid, Island island) throws SQLActionImpossibleException, SQLException {
		if (Storage.PLAYERS.hasResult("player_id", "player_id", uuid.toString())) {
			Storage.PLAYERS.setString("island_id", island.ID, "player_id", uuid.toString());
		}
		else {
			Storage.DB.execute("INSERT INTO " + Storage.PLAYERS + "(player_id, island_id) VALUES (\"" + uuid.toString() + "\", \"" + island.ID + "\")");
		}
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
						createNewIsland(creator, preset,true);
				}, buffer + 10);
				return;
			}
		}
		Locale.PLAYER_ISLAND_CREATE_STARTING.send(creator);

		Map.Entry<Double, Double> coords = GridManager.next();
		double x = coords.getKey(), z = coords.getValue();
		Map.Entry<Double, Double> middle = GridManager.getMiddle(x, z);
		double mx = middle.getKey() < 0 ? middle.getKey() - 1.5 : middle.getKey() + 1.5,
				mz = middle.getValue() < 0 ? middle.getValue() - 1.5 : middle.getValue() + 1.5;

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
				GridManager.getCurrentID(), creator.getUniqueId().toString(), null, 0,
				home != null ? home : GridManager.getWorld().getHighestBlockAt(bedrock).getLocation(), mx, mz, 50, false, Lists.newArrayList(), Lists.newArrayList(),
				Lists.newArrayList(VisitorPermission.DOORS, VisitorPermission.GATES, VisitorPermission.BUTTONS, VisitorPermission.LEVERS, VisitorPermission.DROP, VisitorPermission.PICKUP,
						VisitorPermission.PLATES, VisitorPermission.VILLAGERS, VisitorPermission.FLY),
				Lists.newArrayList(GeneralPermission.SPAWNERS, GeneralPermission.MOBSPAWNING, GeneralPermission.FLUIDFLOWING, GeneralPermission.GENENABLED),
				Lists.newArrayList(CoopPermission.BREAK, CoopPermission.PLACE, CoopPermission.CONTAINERS, CoopPermission.BUCKETS, CoopPermission.ACTIONNERS, CoopPermission.REDSTONE));
		creator.teleport(island.getHome());

		creator.setFlying(false);
		creator.setAllowFlight(originalFlightState);

		try {
			PreparedStatement statement = Storage.DB.prepare("INSERT INTO " + Storage.ISLANDS + "(island_id, leader, settings, toWrap, members_toWrap) VALUES (?, ?, ?, ?, ?)");
			statement.setString(1, island.ID);
			statement.setString(2, creator.getUniqueId().toString());
			statement.setString(3, island.toString_permissions());
			statement.setString(4, island.toString());
			statement.setString(5, island.toString_members());
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

		Wrapper.getPlayers().put(creator.getUniqueId(), new LeezIslandPlayer(creator, island));
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

	public static List<Player> getPlayersOnIsland(Island island) {
		List<Player> players = Lists.newArrayList();
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.getLocation().getWorld() != GridManager.getWorld())
				return Lists.newArrayList();

			if (island.isInBounds(player.getLocation().getBlockX(), player.getLocation().getBlockZ()))
				players.add(player);
		}
		return players;
	}

	public static List<Player> getOnlinePlayers(Island island) {
		List<Player> players = Lists.newArrayList();
		for (UUID uuid : island.getMembersMap().keySet()) {
			Player player = Bukkit.getPlayer(uuid);
			if (player != null)
				players.add(player);
		}
		return players;
	}

	public static void broadcast(Island island, String message) {
		ArrayList<IslandMember> members = Lists.newArrayList(island.getMembersMap().values());
		members.add(new IslandMember(island.getOwner().getUniqueId(), IslandRole.OWNER));
		for (IslandMember member : members) {
			Player player = Bukkit.getPlayer(member.getUniqueId());
			if (player != null)
				player.sendMessage(message);
		}
	}

	public static void setRange(Island island, int range) {
		island.setRange(range);
		island.save();
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

	public static IslandMember getMemberFromPlayer(Island island, OfflinePlayer player) {
		if (island == null) {
			LeezIsland.log.warning("Cannot parse member because island is null.");
			return null;
		}
		return island.getMembersMap().get(player.getUniqueId());
	}

	public static OfflinePlayer getPlayerFromMember(Island island, IslandMember member) {
		if (island == null) {
			LeezIsland.log.warning("Cannot parse player because island is null.");
			return null;
		}
		return island.getMembersMap().containsKey(member.getUniqueId()) ? Bukkit.getOfflinePlayer(member.getUniqueId()) : null;
	}

	public static void clearPlayer(OfflineSkyblockPlayer player) {
		if (player.hasPersonnalHome()) {
			player.setPersonnalHome(null);
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
			LeezIsland.log.warning("Cannot kick member because island is null.");
			return;
		}

		OfflineSkyblockPlayer offPlayer = Wrapper.wrapOffPlayer(player);
		if (island.getMembersMap().containsKey(player.getUniqueId())) {
			island.getMembersMap().remove(player.getUniqueId());
			island.saveMembers();
			clearPlayer(offPlayer);

			// Todo: Notify island's members
		}
		else if (offPlayer.isOwnerOf(island)) {
			for (IslandMember member : island.getMembersMap().values()) {
				kickMember(island, getPlayerFromMember(island, member));
			}

			try {
				Storage.ISLANDS.delete("island_id", island.ID);
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
			GridManager.removeFromGrid(island);
			CoopsManager.unregisterIsland(island.ID);
			Wrapper.getIslands().remove(island.ID);
			clearPlayer(offPlayer);
		}
	}

	public static void addMember(Island island, OfflinePlayer player) {
		if (island == null) {
			LeezIsland.log.warning("Cannot add member because island is null.");
			return;
		}
		if (Wrapper.wrapOffPlayerIsland(player) != null) {
			LeezIsland.log.warning("Tried to add the player " + player.getName() + " to a team but he already has an island.");
			return;
		}

		// Todo: Notify island's members
		UUID uuid = player.getUniqueId();
		if (CoopsManager.isCooped(uuid, island.ID))
			CoopsManager.unCoop(uuid, island.ID, false);

		if (island.getBanneds().contains(uuid))

		try {
			registerPlayerToIsland(uuid, island);

			island.getMembersMap().put(uuid, new IslandMember(uuid, IslandRole.MEMBER));
			island.saveMembers();

			Wrapper.getPlayers().remove(uuid);
		}
		catch (SQLActionImpossibleException | SQLException e) {
			e.printStackTrace();
		}
	}

	public static void sendToTeamChat(SkyblockPlayer player, String message) {
		final IslandRole role = player.getIsland().getRole(player);
		final String displayName = "§" + role.getColorChat() + "(" + role.toString() + ") " + player.bukkit().getName();
		String coloredMessage = role.ordinal() >= IslandRole.OFFICIER.ordinal() ? message.replaceAll("&([\\da-f])", "§$1") : message;
		if (!coloredMessage.startsWith(ChatColor.COLOR_CHAR + ""))
			coloredMessage = "§d" + coloredMessage;

		IslandManager.broadcast(player.getIsland(), "§5[Team] §7" + displayName + " §8➟ " + coloredMessage);
		LeezIsland.getInstance().getLogger().info("[Team] " + player.bukkit().getName() + ": " + message);
	}

	public static boolean banPlayer(Island island, UUID target) {
		if (island.getBanneds().contains(target))
			return false;

		if (CoopsManager.isCooped(target, island.ID))
			CoopsManager.unCoop(target, island.ID, false);

		// Todo: Notify island's members
		island.getBanneds().add(target);
		island.saveMembers();
		return true;
	}

	public static void unban(Island island, UUID target) {
		island.getBanneds().remove(target);
		island.saveMembers();
	}
}
