package lz.izmoqwy.island.players;

import lz.izmoqwy.core.api.database.exceptions.SQLActionImpossibleException;
import lz.izmoqwy.core.utils.LocationUtil;
import lz.izmoqwy.island.Storage;
import lz.izmoqwy.island.grid.GridManager;
import lz.izmoqwy.island.island.Island;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class LeezOffIslandPlayer implements OfflineSkyblockPlayer {

	private OfflinePlayer base;

	public LeezOffIslandPlayer(OfflinePlayer base) {
		this.base = base;
	}

	@Override
	public UUID getBaseId() {
		return base.getUniqueId();
	}

	@Override
	public long getLastRestart() {
		try {
			return Storage.PLAYERS.getLong("lastRestart", "player_id", base.getUniqueId().toString());
		}
		catch (SQLActionImpossibleException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public void setLastRestart(long last) {
		try {
			Storage.PLAYERS.setLong("lastRestart", last, "player_id", base.getUniqueId().toString());
		}
		catch (SQLActionImpossibleException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean hasPersonalHome() {
		return getPersonalHome() != null;
	}

	@Override
	public Location getPersonalHome() {
		try {
			String loc = Storage.PLAYERS.getString("personalHome", "player_id", base.getUniqueId().toString());
			if (loc == null) return null;
			return LocationUtil.inlineParse(loc, GridManager.getWorld());
		}
		catch (SQLActionImpossibleException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void setPersonalHome(Location loc) {
		try {
			Storage.PLAYERS.setString("personalHome", loc == null ? null : LocationUtil.inlineSerialize(loc, false, true), "player_id", base.getUniqueId().toString());
		}
		catch (SQLActionImpossibleException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean hasIsland() {
		try {
			String island = Storage.PLAYERS.getString("island_id", "player_id", base.getUniqueId().toString());
			return island != null;
		}
		catch (SQLActionImpossibleException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean isOwnerOf(Island island) {
		return island != null && island.getOwner().getUniqueId().equals(base.getUniqueId());
	}

}
