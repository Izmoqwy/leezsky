package lz.izmoqwy.island;

import lombok.Getter;
import lz.izmoqwy.core.hooks.crosshooks.CrosshooksManager;
import lz.izmoqwy.core.i18n.LocaleManager;
import lz.izmoqwy.core.utils.ServerUtil;
import lz.izmoqwy.island.commands.AdminCommand;
import lz.izmoqwy.island.commands.PlayerCommand;
import lz.izmoqwy.island.commands.PlayerCommandTabCompleter;
import lz.izmoqwy.island.grid.CoopsManager;
import lz.izmoqwy.island.grid.GridManager;
import lz.izmoqwy.island.island.Island;
import lz.izmoqwy.island.listeners.BordersListener;
import lz.izmoqwy.island.listeners.ChunkGenerationListener;
import lz.izmoqwy.island.listeners.IslandGuard;
import lz.izmoqwy.island.players.SkyblockPlayer;
import lz.izmoqwy.island.players.Wrapper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.logging.Logger;

public class LeezIsland extends JavaPlugin implements Listener {

	public static Logger logger;
	public static final String WORLD_NAME = "LeezIsland";

	@Getter
	private static LeezIsland instance;

	@Override
	public void onEnable() {
		instance = this;
		logger = getLogger();

		GridManager.load();
		try {
			GridManager.loadAllIslands();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}

		ServerUtil.registerListeners(this, this,
				new ChunkGenerationListener(), new BordersListener(),
				new IslandGuard());

		ServerUtil.registerCommand("island", new PlayerCommand());
		ServerUtil.setCommandTabCompleter("island", new PlayerCommandTabCompleter());

		ServerUtil.registerCommand("isadmin", new AdminCommand());

		CrosshooksManager.registerHook(this, new LeezIslandCrossHook());
		LocaleManager.register(this, Locale.class);
	}

	@EventHandler
	public void onDisconnect(PlayerQuitEvent event) {
		CoopsManager.manager.handleDisconnect(event.getPlayer());
		Wrapper.getPlayers().remove(event.getPlayer().getUniqueId());
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onAsyncChat(AsyncPlayerChatEvent event) {
		if (PlayerCommand.teamChat.contains(event.getPlayer().getUniqueId())) {
			event.setCancelled(true);

			SkyblockPlayer player = Wrapper.wrapPlayer(event.getPlayer());
			if (player == null) {
				event.setCancelled(false);
				return;
			}

			Island island = player.getIsland();
			if (island != null)
				island.sendToTeam(player, event.getMessage());
		}
	}

	@Override
	public void onDisable() {
		Storage.DB.disconnect();
	}

}
