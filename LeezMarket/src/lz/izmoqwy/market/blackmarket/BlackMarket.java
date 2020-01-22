package lz.izmoqwy.market.blackmarket;

import com.google.common.collect.Lists;
import com.sun.istack.internal.NotNull;
import lz.izmoqwy.core.PlayerDataStorage;
import lz.izmoqwy.core.self.CorePrinter;
import lz.izmoqwy.core.utils.LocationUtil;
import lz.izmoqwy.core.utils.ServerUtil;
import lz.izmoqwy.market.MarketPlugin;
import lz.izmoqwy.market.blackmarket.illegal.ForbiddenArena;
import lz.izmoqwy.market.npc.NPC_v1_12_R1;
import lz.izmoqwy.market.rpg.commands.*;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class BlackMarket implements Listener {

	protected static NPC_v1_12_R1 NPC = null;
	protected static List<ArmorStand> armorStands;
	protected static List<String> armorStandsIds;
	protected static File file = new File(MarketPlugin.getInstance().getDataFolder(), "blackmarket.yml");

	protected static YamlConfiguration config;
	protected static String NPC_NAME;

	protected static Location TELEPORT_POINT;

	public static void loadRPG() {
		CorePrinter.print("Loading RPG (BlackMarket) commands...");
		ServerUtil.registerCommand("rpgstats", new StatsCommand("rpgstats"));
		ServerUtil.registerCommand("rpginventory", new InventoryCommand("rpginventory"));
		ServerUtil.registerCommand("rpgitems", new ItemsCommand("rpgitems"));
		ServerUtil.registerCommand("rpgmine", new MineCommand("rpgmine"));
		ServerUtil.registerCommand("rpgfish", new FishCommand("rpgfish"));
	}

	public static void loadAll() {
		loadConfig();
		BMStuff.init(config.getConfigurationSection("prices"));

		ServerUtil.registerCommand("blackmarket", new BlackMarketCommand());
		ServerUtil.registerListeners(MarketPlugin.getInstance(), new BlackMarket());
		ServerUtil.registerListeners(MarketPlugin.getInstance(), new BlackMarketGUI());
		loadRPG();

		refreshForbiddenArena();
	}

	public static void refreshForbiddenArena() {
		final String path = "forbiddenarena.";

		List<Location> points = Lists.newArrayList();
		ConfigurationSection section = config.getConfigurationSection(path + "points");
		if (section != null) {
			for (String strPoint : section.getKeys(false)) {
				Location location = LocationUtil.loadFromYaml(config, path + "points." + strPoint);
				if (location != null)
					points.add(location);
			}
		}

		ForbiddenArena.load(LocationUtil.loadFromYaml(config, path + "spawn"), points);
	}

	private static boolean loadConfig() {
		if (file.exists()) {
			CorePrinter.print("Loading BM config from yaml..");
			config = YamlConfiguration.loadConfiguration(file);
			NPC_NAME = config.getString("npc.name", "§6§lMarché noir");

			Location location = LocationUtil.loadFromYaml(config, "npc");
			if (location == null)
				return false;

			String useSkin = config.getString("skin.current", "default");
			CorePrinter.print("Using skin {0} for BlackMarket NPC.", useSkin);
			NPC = new NPC_v1_12_R1(NPC_NAME, location, config.getString("skins." + useSkin + ".texture"), config.getString("skins." + useSkin + ".signature"));
			NPC.spawn();
			spawnArmorStands(location);

			TELEPORT_POINT = LocationUtil.loadFromYaml(config, "warp");
			return true;
		}
		else {
			if (!file.getParentFile().exists())
				//noinspection ResultOfMethodCallIgnored
				file.getParentFile().mkdirs();
			try {
				if (file.createNewFile())
					loadConfig();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	protected static boolean reload() {
		if (file.exists())
			return loadConfig();
		return false;
	}

	private static boolean forceLoadChunks(@NotNull Location from) {
		for (Location location : getASLocations(from)) {
			if (location.getWorld().getChunkAt(location) == null)
				return false;
		}
		return true;
	}

	protected static void forceRemoveArmorStands(@NotNull Location from) {
		if (!forceLoadChunks(from))
			return;

		for (Entity nearby : from.getWorld().getNearbyEntities(from, 3, 2, 3)) {
			if (nearby.getType() == EntityType.ARMOR_STAND) {
				ArmorStand as = (ArmorStand) nearby;
				if (!as.isVisible() && !as.isCustomNameVisible()) {
					as.remove();
				}
			}
		}
	}

	protected static void spawnArmorStands(@NotNull Location location) {
		if (location.getWorld() == null)
			return;

		Location[] locations = getASLocations(location);
		if (armorStands != null) {
			for (int i = 0; i < 4; i++) {
				armorStands.get(i).teleport(locations[i]);
			}
		}
		else {
			forceRemoveArmorStands(location);

			ArmorStand[] armorStands = new ArmorStand[4];
			String[] ids = new String[4];
			for (int i = 0; i < 4; i++) {
				location.getWorld().getChunkAt(locations[i]);
				ArmorStand armorStand = location.getWorld().spawn(locations[i], ArmorStand.class);
				armorStand.setInvulnerable(true);
				armorStand.setGravity(false);
				armorStand.setCanPickupItems(false);
				armorStand.setVisible(false);

				ids[i] = "NPC-AS(" + i + ")|" + armorStand.getEntityId();
				armorStand.setCustomName(ids[i]);

				armorStands[i] = armorStand;
			}
			BlackMarket.armorStands = Arrays.asList(armorStands);
			BlackMarket.armorStandsIds = Arrays.asList(ids);
		}
	}

	private static Location[] getASLocations(@NotNull Location middle) {
		if (middle.getWorld() == null)
			return new Location[0];

		return new Location[]{add(middle, 0.25, 0.25), add(middle, -0.25, 0.25), add(middle, 0.25, -0.25), add(middle, -0.25, -0.25)};
	}

	private static Location add(Location base, double x, double z) {
		return new Location(base.getWorld(), base.getX() + x, base.getY(), base.getZ() + z, base.getYaw(), base.getPitch());
	}

	public static void unload() {
		if (NPC != null)
			NPC.despawn();

		if (armorStands != null) {
			for (ArmorStand as : armorStands) {
				as.remove();
			}
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		if (NPC != null && NPC.getWorld() == event.getPlayer().getWorld()) {
			if (NPC.getLocation().distance(event.getPlayer().getLocation()) < 1000) {
				NPC.spawn(event.getPlayer());
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onTeleport(PlayerTeleportEvent event) {
		if (NPC != null && NPC.getWorld() == event.getTo().getWorld()) {
			if (NPC.getLocation().distance(event.getTo()) < 1000) {
				NPC.spawn(event.getPlayer());
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onInteract(PlayerInteractAtEntityEvent event) {
		if (NPC == null || armorStandsIds == null)
			return;

		if (event.getRightClicked().getType() == EntityType.ARMOR_STAND) {
			if (armorStandsIds.contains(event.getRightClicked().getCustomName())) {
				event.setCancelled(true);
				NPC.updateSkin(event.getPlayer());

				Player player = event.getPlayer();
				if (PlayerDataStorage.get(player, "blackmarket.access", false)) {
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_CHIME, SoundCategory.AMBIENT, 1, 10);
					player.openInventory(BlackMarketGUI.GUI_MENU);
				}
				else {
					BlackMarketGUI.openAccessInventory(player);
				}
			}
		}
	}

}
