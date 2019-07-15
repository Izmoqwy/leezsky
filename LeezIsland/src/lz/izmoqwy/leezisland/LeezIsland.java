package lz.izmoqwy.leezisland;

import lz.izmoqwy.core.crosshooks.CrosshooksManager;
import lz.izmoqwy.core.crosshooks.interfaces.LeezIslandCH;
import lz.izmoqwy.core.helpers.PluginHelper;
import lz.izmoqwy.core.i18n.LocaleManager;
import lz.izmoqwy.leezisland.commands.AdminCommand;
import lz.izmoqwy.leezisland.commands.PlayerCommand;
import lz.izmoqwy.leezisland.grid.CoopsManager;
import lz.izmoqwy.leezisland.grid.GridManager;
import lz.izmoqwy.leezisland.grid.IslandManager;
import lz.izmoqwy.leezisland.island.Island;
import lz.izmoqwy.leezisland.listeners.*;
import lz.izmoqwy.leezisland.players.SkyblockPlayer;
import lz.izmoqwy.leezisland.players.Wrapper;
import lz.izmoqwy.leezisland.tabcompleters.PlayerCommandTabCompleter;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.logging.Logger;

public class LeezIsland extends JavaPlugin implements Listener {

	public static Logger log;
	public static final String WORLD_NAME = "LeezIsland";

	private static LeezIsland instance;

	@Override
	public void onEnable() {
		instance = this;
		log = getLogger();

		GridManager.load();

		try {
			GridManager.loadAllIslands();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}

		PluginHelper.loadListener(this, this);
		PluginHelper.loadListener(this, new GenerationListener());
		PluginHelper.loadListener(this, new BordersListener());
		PluginHelper.loadListener(this, new IslandGuard());
		PluginHelper.loadListener(this, new SettingsMenuListener());
		PluginHelper.loadListener(this, new CobbleGeneratorListener());

		PluginHelper.loadCommand("island", new PlayerCommand());
		PluginHelper.setTabCompleter("island", new PlayerCommandTabCompleter());
		PluginHelper.loadCommand("isadmin", new AdminCommand());

		CrosshooksManager.registerHook(this, new LeezIslandCH() {
			@Override
			public int getIslandLevel(OfflinePlayer player) {
				Island island = Wrapper.wrapOffPlayerIsland(player);
				return island != null ? island.getLevel() : -1;
			}

			@Override
			public String getHookName() {
				return "LeezIsland";
			}
		});
		LocaleManager.register(this, Locale.class);
	}

	@EventHandler
	public void onDisconnnect(final PlayerQuitEvent event) {
		CoopsManager.handleDisconnect(event.getPlayer());
		Wrapper.getPlayers().remove(event.getPlayer().getUniqueId());
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onAsyncChat(final AsyncPlayerChatEvent event) {
		if (PlayerCommand.teamChat.contains(event.getPlayer().getUniqueId())) {
			event.setCancelled(true);

			SkyblockPlayer player = Wrapper.wrapPlayer(event.getPlayer());
			if (player == null) {
				event.setCancelled(false);
				return;
			}

			IslandManager.sendToTeamChat(player, event.getMessage());
		}
	}

	@Override
	public void onDisable() {
		Storage.DB.disconnect();
	}

	public static LeezIsland getInstance() {
		return LeezIsland.instance;
	}
}
