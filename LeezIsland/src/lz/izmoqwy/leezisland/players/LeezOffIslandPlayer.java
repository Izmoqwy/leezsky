package lz.izmoqwy.leezisland.players;

import lz.izmoqwy.core.api.database.exceptions.SQLActionImpossibleException;
import lz.izmoqwy.leezisland.Storage;
import lz.izmoqwy.leezisland.grid.GridManager;
import lz.izmoqwy.leezisland.island.Island;
import lz.izmoqwy.leezisland.utils.ParseUtil;
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
	public boolean hasPersonnalHome() {
		return getPersonnalHome() != null;
	}

	@Override
	public Location getPersonnalHome() {
		try {
			String loc = Storage.PLAYERS.getString("personnalHome", "player_id", base.getUniqueId().toString());
			if (loc == null) return null;
			return ParseUtil.str2locNW(loc, GridManager.getWorld().getName());
		}
		catch (SQLActionImpossibleException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void setPersonnalHome(Location loc) {
		try {
			Storage.PLAYERS.setString("personnalHome", loc == null ? null : ParseUtil.loc2strNW(loc), "player_id", base.getUniqueId().toString());
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
