package lz.izmoqwy.core.self;

import com.google.common.collect.Maps;
import lombok.Getter;
import lz.izmoqwy.core.PlayerSaveManager;
import lz.izmoqwy.core.api.database.Database;
import lz.izmoqwy.core.api.nickname.NicknameAPI;
import lz.izmoqwy.core.gui.GUIManager;
import lz.izmoqwy.core.gui.InternalGUIListener;
import lz.izmoqwy.core.hooks.HooksManager;
import lz.izmoqwy.core.nms.NMS;
import lz.izmoqwy.core.utils.ServerUtil;
import lz.izmoqwy.core.utils.StoreUtil;
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

	@Getter
	public static LeezCore instance;

	private static Map<Plugin, List<Database>> registeredDB = Maps.newHashMap();

	@Override
	public void onEnable() {
		instance = this;
		final long startsLoading = System.currentTimeMillis();

		NMS.load();
		HooksManager.load();
		NicknameAPI.instance.load();

		ServerUtil.registerCommand("leezcore", new LeezCoreCommand());

		ServerUtil.registerListeners(this, new InternalGUIListener());
		ServerUtil.registerListeners(this, new GUIManager());

		PlayerSaveManager.load();

		long ms = System.currentTimeMillis() - startsLoading;
		CorePrinter.write("The Core took {0}ms to load.", ms > 200 ? Level.WARNING : Level.INFO, ms);
	}

	@Override
	public void onDisable() {
		NicknameAPI.instance.unload();

		int count = 0;
		for (List<Database> databases : registeredDB.values()) {
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
		StoreUtil.addToMapList(registeredDB, db.getPlugin(), db);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public static void onPluginDisable(PluginDisableEvent event) {
		Plugin plugin = event.getPlugin();
		if (!registeredDB.containsKey(plugin))
			return;

		int count = 0;
		for (Database db : registeredDB.get(plugin)) {
			if (db.isConnected() && db.disconnect())
				count++;
		}
		if (count > 0) {
			CorePrinter.warn("The plugin \"{0}\" doesn't disconnect its DBs. {1} databases were forced to disconnect.", plugin.getName(), count);
		}
	}

}
