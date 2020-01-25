package lz.izmoqwy.island.grid;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import lz.izmoqwy.core.utils.StoreUtil;
import lz.izmoqwy.island.island.Island;
import lz.izmoqwy.island.players.Wrapper;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CoopsManager {

	public static final CoopsManager manager = new CoopsManager();

	private CoopsManager() {
	}

	private Map<String, List<UUID>> coopMap = Maps.newHashMap();
	private Map<UUID, List<String>> reversedCoopMap = Maps.newHashMap();

	public void coop(UUID uuid, Island island, Player initier) {
		StoreUtil.addToMapList(coopMap, island.ID, uuid);
		StoreUtil.addToMapList(reversedCoopMap, uuid, island.ID);

		// Todo: Notify island's members
	}

	public void unCoop(UUID uuid, Island island, boolean notify) {
		if (StoreUtil.removeFromMapList(coopMap, island.ID, uuid) && StoreUtil.removeFromMapList(reversedCoopMap, uuid, island.ID) && notify) {
			// Todo: Notify island's members
		}
	}

	public boolean isCooped(UUID uuid, Island island) {
		return StoreUtil.isInMapList(coopMap, island.ID, uuid);
	}

	public List<UUID> getCoops(Island island) {
		return coopMap.get(island.ID);
	}

	public void unregisterIsland(Island island) {
		List<UUID> coops = coopMap.remove(island.ID);
		if (coops != null)
			coops.forEach(uuid -> unCoop(uuid, island, false));
	}

	@SneakyThrows
	public void handleDisconnect(Player player) {
		for (String islandId : reversedCoopMap.getOrDefault(player.getUniqueId(), Lists.newArrayList())) {
			unCoop(player.getUniqueId(), Wrapper.wrapIsland(islandId), true);
		}
	}

}
