package lz.izmoqwy.island;

import lz.izmoqwy.core.hooks.crosshooks.CrosshooksManager;
import lz.izmoqwy.core.hooks.crosshooks.interfaces.IslandInfo;
import lz.izmoqwy.core.hooks.crosshooks.interfaces.IslandRelationship;
import lz.izmoqwy.core.hooks.crosshooks.interfaces.LeezIslandCH;
import lz.izmoqwy.core.i18n.LocaleManager;
import lz.izmoqwy.core.utils.ServerUtil;
import lz.izmoqwy.island.commands.AdminCommand;
import lz.izmoqwy.island.commands.PlayerCommand;
import lz.izmoqwy.island.grid.CoopsManager;
import lz.izmoqwy.island.grid.GridManager;
import lz.izmoqwy.island.grid.IslandManager;
import lz.izmoqwy.island.island.Island;
import lz.izmoqwy.island.island.IslandRole;
import lz.izmoqwy.island.listeners.*;
import lz.izmoqwy.island.players.SkyblockPlayer;
import lz.izmoqwy.island.players.Wrapper;
import lz.izmoqwy.island.commands.PlayerCommandTabCompleter;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.UUID;
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

		ServerUtil.registerListeners(this, this);
		ServerUtil.registerListeners(this, new GenerationListener());
		ServerUtil.registerListeners(this, new BordersListener());
		ServerUtil.registerListeners(this, new IslandGuard());

		ServerUtil.registerCommand("island", new PlayerCommand());
		ServerUtil.setCommandTabCompleter("island", new PlayerCommandTabCompleter());
		ServerUtil.registerCommand("isadmin", new AdminCommand());

		CrosshooksManager.registerHook(this, new LeezIslandCH() {
			@Override
			public IslandInfo getIslandInfo(OfflinePlayer player) {
				final Island island = Wrapper.wrapOffPlayerIsland(player);
				if (island == null)
					return null;

				return new IslandInfo() {
					@Override
					public String getName() {
						return island.getDisplayName();
					}

					@Override
					public int getLevel() {
						return island.getLevel();
					}

					@Override
					public String getRoleName(UUID player, boolean color) {
						IslandRole role = island.getRole(player);
						return color ? "§" + role.getColorChat() + role.name : role.name;
					}

					@Override
					public IslandRelationship getRelationship(UUID player) {
						if (island.getMembersMap().containsKey(player))
							return IslandRelationship.MEMBER;
						if (island.getBanneds().contains(player))
							return IslandRelationship.BANNED;
						if (CoopsManager.isCooped(player, island.ID))
							return IslandRelationship.COOP;
						return IslandRelationship.VISITOR;
					}
				};
			}

			@Override
			public String getHookName() {
				return "LeezIsland";
			}
		});
		LocaleManager.register(this, Locale.class);
	}

	@EventHandler
	public void onDisconnect(PlayerQuitEvent event) {
		CoopsManager.handleDisconnect(event.getPlayer());
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
