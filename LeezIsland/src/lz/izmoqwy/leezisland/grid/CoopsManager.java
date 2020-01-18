package lz.izmoqwy.leezisland.grid;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lz.izmoqwy.core.utils.StoreUtil;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CoopsManager {

	private static Map<String, List<UUID>> coops = Maps.newHashMap();
	private static Map<UUID, List<String>> coopedOn = Maps.newHashMap();

	public static void coop(UUID uuid, String islandId, Player initier) {
		StoreUtil.addToMapList(coops, islandId, uuid);
		StoreUtil.addToMapList(coopedOn, uuid, islandId);

		// Todo: Notify island's members
	}

	public static void unCoop(UUID uuid, String islandId, boolean notify) {
		if (StoreUtil.removeFromMapList(coops, islandId, uuid) && StoreUtil.removeFromMapList(coopedOn, uuid, islandId) && notify) {
			// Todo: Notify island's members
		}
	}

	public static boolean isCooped(UUID uuid, String islandId) {
		return StoreUtil.isInMapList(coops, islandId, uuid);
	}

	public static List<UUID> getCoops(String island_id) {
		if (!coops.containsKey(island_id))
			return null;

		return coops.get(island_id);
	}

	public static void unregisterIsland(String islandId) {
		List<UUID> _coops = coops.remove(islandId);
		if (_coops == null)
			return;

		_coops.forEach(uuid -> unCoop(uuid, islandId, false));
	}

	public static void handleDisconnect(Player player) {
		for (String islandId : coopedOn.getOrDefault(player.getUniqueId(), Lists.newArrayList())) {
			unCoop(player.getUniqueId(), islandId, true);
		}
	}

}
