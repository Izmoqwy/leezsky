package lz.izmoqwy.island;

import lz.izmoqwy.core.hooks.crosshooks.interfaces.IslandInfo;
import lz.izmoqwy.core.hooks.crosshooks.interfaces.IslandRelationship;
import lz.izmoqwy.core.hooks.crosshooks.interfaces.LeezIslandCH;
import lz.izmoqwy.island.grid.CoopsManager;
import lz.izmoqwy.island.island.Island;
import lz.izmoqwy.island.island.IslandRole;
import lz.izmoqwy.island.players.Wrapper;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class LeezIslandCrossHook implements LeezIslandCH {

	@Override
	public IslandInfo getIslandInfo(OfflinePlayer player) {
		final Island island = Wrapper.getOfflinePlayerIsland(player);
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
				return color ? "§" + role.getColorChat() + role.getName() : role.getName();
			}

			@Override
			public IslandRelationship getRelationship(UUID player) {
				if (island.getMembersMap().containsKey(player))
					return IslandRelationship.MEMBER;

				if (island.getBanList().contains(player))
					return IslandRelationship.BANNED;

				if (CoopsManager.manager.isCooped(player, island))
					return IslandRelationship.COOP;

				return IslandRelationship.VISITOR;
			}
		};
	}

	@Override
	public String getHookName() {
		return "LeezIsland";
	}

}
