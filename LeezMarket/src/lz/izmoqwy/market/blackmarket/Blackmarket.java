package lz.izmoqwy.market.blackmarket;

import lz.izmoqwy.core.CorePrinter;
import lz.izmoqwy.core.PlayerDataStorage;
import lz.izmoqwy.core.helpers.PluginHelper;
import lz.izmoqwy.market.MarketPlugin;
import lz.izmoqwy.market.npc.NPC_v1_12_R1;
import lz.izmoqwy.market.rpg.commands.FishCommand;
import lz.izmoqwy.market.rpg.commands.InventoryCommand;
import lz.izmoqwy.market.rpg.commands.MineCommand;
import lz.izmoqwy.market.rpg.commands.StatsCommand;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class BlackMarket implements Listener {

	protected static NPC_v1_12_R1 NPC = null;
	protected static List<ArmorStand> armorStands;
	protected static List<Integer> armorStandsIds;
	protected static File file = new File(MarketPlugin.getInstance().getDataFolder(), "blackmarket.yml");

	protected static YamlConfiguration config;
	protected static String NPC_NAME;

	public static void loadRPG() {
		CorePrinter.print("Loading RPG (BlackMarket) commands...");
		PluginHelper.loadCommand("rpgstats", new StatsCommand("rpgstats"));
		PluginHelper.loadCommand("rpginventory", new InventoryCommand("rpginventory"));
		PluginHelper.loadCommand("rpgmine", new MineCommand("rpgmine"));
		PluginHelper.loadCommand("rpgfish", new FishCommand("rpgfish"));
	}

	public static void loadAll() {
		PluginHelper.loadCommand("blackmarket", new BlackMarketCommand());
		PluginHelper.loadListener(MarketPlugin.getInstance(), new BlackMarket());
		PluginHelper.loadListener(MarketPlugin.getInstance(), new BlackMarketGUI());
		loadRPG();

		loadNPC();
	}

	private static void loadNPC() {
		if (file.exists()) {
			// todo
			CorePrinter.print("Loading BM NPC from file...");
			config = YamlConfiguration.loadConfiguration(file);
			NPC_NAME = config.getString("npc.name", "§6§lMarché noir");

			String[] all_paths = new String[]{"world", "x", "y", "z", "yaw", "pitch"};
			for (String path : all_paths) {
				if (!config.isSet("npc." + path))
					return;
			}

			double x = config.getDouble("npc.x"), y = config.getDouble("npc.y"), z = config.getDouble("npc.z");
			double yaw = config.getDouble("npc.yaw"), pitch = config.getDouble("npc.pitch");

			Location location = new Location(Bukkit.getWorld(config.getString("npc.world")), x, y, z, (float) yaw, (float) pitch);
			NPC = new NPC_v1_12_R1(NPC_NAME, location, config.getString("skin.texture"), config.getString("skin.signature"));
			NPC.spawn();

			spawnArmorStand(location);
		}
		else {
			if (!file.getParentFile().exists())
				//noinspection ResultOfMethodCallIgnored
				file.getParentFile().mkdirs();
			try {
				if (file.createNewFile())
					loadNPC();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected static void spawnArmorStand(Location location) {
		Location[] locations = spawnArmorStands(location);
		if (armorStands != null) {
			for (int i = 0; i < 4; i++) {
				armorStands.get(i).teleport(locations[i]);
			}
		}
		else {
			ArmorStand[] armorStands = new ArmorStand[4];
			Integer[] ids = new Integer[4];
			for (int i = 0; i < 4; i++) {
				ArmorStand armorStand = location.getWorld().spawn(locations[i], ArmorStand.class);
				armorStand.setInvulnerable(true);
				armorStand.setGravity(false);
				armorStand.setCanPickupItems(false);
				armorStand.setVisible(false);

				armorStands[i] = armorStand;
				ids[i] = armorStand.getEntityId();
			}
			BlackMarket.armorStands = Arrays.asList(armorStands);
			BlackMarket.armorStandsIds = Arrays.asList(ids);
		}
	}

	private static Location[] spawnArmorStands(Location middle) {
		return new Location[]{add(middle, 0.25, 0.25), add(middle, -0.25, 0.25), add(middle, 0.25, -0.25), add(middle, -0.25, -0.25)};
				//add(middle, 0.25, 0), add(middle, -0.25, 0), add(middle, 0, 0.25), add(middle, 0, -0.25)};
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
		if (NPC != null)
			NPC.spawn(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onInteract(PlayerInteractAtEntityEvent event) {
		if (NPC == null || armorStandsIds == null)
			return;

		if (event.getRightClicked().getType() == EntityType.ARMOR_STAND) {
			if (armorStandsIds.contains(event.getRightClicked().getEntityId())) {
				event.setCancelled(true);
				NPC.updateSkin(event.getPlayer());

				Player player = event.getPlayer();
				if (PlayerDataStorage.get(player, "blackmarket.access", false)) {
					player.openInventory(BlackMarketGUI.GUI_MENU);
				}
				else {
					BlackMarketGUI.openAccessInventory(player);
				}
			}
		}
	}
}
