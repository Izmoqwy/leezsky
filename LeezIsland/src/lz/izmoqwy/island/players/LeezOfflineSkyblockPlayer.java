package lz.izmoqwy.island.players;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lz.izmoqwy.core.api.database.exceptions.SQLActionImpossibleException;
import lz.izmoqwy.core.utils.LocationUtil;
import lz.izmoqwy.island.Storage;
import lz.izmoqwy.island.grid.GridManager;
import lz.izmoqwy.island.island.Island;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class LeezOfflineSkyblockPlayer implements OfflineSkyblockPlayer {

	private OfflinePlayer base;

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
	public void setLastRestart(long lastRestart) {
		try {
			Storage.PLAYERS.setLong("lastRestart", lastRestart, "player_id", base.getUniqueId().toString());
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
			String personalHome = Storage.PLAYERS.getString("personalHome", "player_id", base.getUniqueId().toString());
			if (personalHome == null)
				return null;
			return LocationUtil.inlineParse(personalHome, GridManager.getWorld());
		}
		catch (SQLActionImpossibleException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void setPersonalHome(Location personalHome) {
		try {
			Storage.PLAYERS.setString("personalHome",
					personalHome == null ? null : LocationUtil.inlineSerialize(personalHome, false, true), "player_id", base.getUniqueId().toString());
		}
		catch (SQLActionImpossibleException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean hasIsland() {
		try {
			return Storage.PLAYERS.getString("island_id", "player_id", base.getUniqueId().toString()) != null;
		}
		catch (SQLActionImpossibleException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean isOwnerOf(Island island) {
		return island != null && island.isOwner(base.getUniqueId());
	}

}
