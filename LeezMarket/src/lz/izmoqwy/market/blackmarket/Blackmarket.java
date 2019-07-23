package lz.izmoqwy.market.blackmarket;

import lz.izmoqwy.core.CorePrinter;
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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.io.IOException;

public class BlackMarket implements Listener {

	protected static NPC_v1_12_R1 NPC = null;
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
		loadRPG();

		loadNPC();
	}
	private static void loadNPC() {

		if (file.exists()) {
			// todo
			CorePrinter.print("Loading BM NPC from file...");
			config = YamlConfiguration.loadConfiguration(file);
			NPC_NAME = config.getString("npc.name", "§6§lMarché noir");

			String[] all_paths = new String[] { "world", "x", "y", "z", "yaw", "pitch" };
			for (String path : all_paths) {
				if(!config.isSet("npc." + path))
					return;
			}

			double x = config.getDouble("npc.x"), y = config.getDouble("npc.y"), z = config.getDouble("npc.z");
			double yaw = config.getDouble("npc.yaw"), pitch = config.getDouble("npc.pitch");

			Location location = new Location(Bukkit.getWorld(config.getString("npc.world")), x, y, z, (float) yaw, (float) pitch);
			NPC = new NPC_v1_12_R1(NPC_NAME, location, config.getString("skin.texture"), config.getString("skin.signature"));
			NPC.spawn();
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

	public static void unload() {
		if (NPC != null)
			NPC.despawn();
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		if (NPC != null)
			NPC.spawn(event.getPlayer());
	}
}
