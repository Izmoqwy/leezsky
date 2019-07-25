package lz.izmoqwy.core;

import com.google.common.collect.Maps;
import lz.izmoqwy.core.api.database.Database;
import lz.izmoqwy.core.api.nickname.NicknameAPI;
import lz.izmoqwy.core.commands.LeezCoreCommand;
import lz.izmoqwy.core.helpers.PluginHelper;
import lz.izmoqwy.core.hooks.HooksManager;
import lz.izmoqwy.core.nms.NmsAPI;
import lz.izmoqwy.core.top.TopManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class LeezCore extends JavaPlugin implements Listener {

	public static final String PREFIX = "§8» ";
	public static final Location SPAWN_LOCATION = new Location(Bukkit.getWorld("Spawn"), 0, 0, 0);
	public static JavaPlugin instance;

	private static Map<Plugin, List<Database>> databases = Maps.newHashMap();

	@Override
	public void onEnable() {
		super.onEnable();
		load(this);
	}

	public void load(JavaPlugin plugin) {
		instance = plugin;
		final long startsLoading = System.currentTimeMillis();

		NmsAPI.load();
		HooksManager.load();
		NicknameAPI.load();

		PluginHelper.loadCommand("leezcore", new LeezCoreCommand());
		PluginHelper.loadListener(this, new GUIManager());

		TopManager.load(this);
		PlayerSaveManager.load();

		long ms = System.currentTimeMillis() - startsLoading;
		CorePrinter.write("The Core took {0}ms to load.", ms > 200 ? Level.WARNING : Level.INFO, ms);
	}

	@Override
	public void onDisable() {
		super.onDisable();
		unload();
	}

	public void unload() {
		NicknameAPI.unload();

		int count = 0;
		for (List<Database> databases : databases.values()) {
			for (Database db : databases) {
				if (db.isConnected() && db.disconnect())
					count++;
			}
		}
		if (count > 0) {
			CorePrinter.warn("{0} databases were forced to disconnect due to the stop of the Core.", count);
		}
	}

	public static void registerDB(Database db) {

	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public static void onPluginDisable(PluginDisableEvent event) {
		Plugin plugin = event.getPlugin();
		if (!databases.containsKey(plugin))
			return;

		int count = 0;
		for (Database db : databases.get(plugin)) {
			if (db.isConnected() && db.disconnect())
				count++;
		}
		if (count > 0) {
			CorePrinter.warn("The plugin \"{0}\" doesn't disconnect its DBs. {1} databases were forced to disconnect.", plugin.getName(), count);
		}
	}

}
