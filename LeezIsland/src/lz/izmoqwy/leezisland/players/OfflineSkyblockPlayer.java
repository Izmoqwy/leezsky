package lz.izmoqwy.leezisland.players;

import lz.izmoqwy.leezisland.island.Island;
import org.bukkit.Location;

import java.util.UUID;

public interface OfflineSkyblockPlayer {

	UUID getBaseId();

	boolean hasIsland();

	boolean isOwnerOf(Island island);

	long getLastRestart();

	void setLastRestart(long last);

	boolean hasPersonalHome();

	Location getPersonalHome();

	void setPersonalHome(Location loc);

}
