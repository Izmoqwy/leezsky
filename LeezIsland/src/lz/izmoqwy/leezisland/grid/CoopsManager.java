package lz.izmoqwy.leezisland.grid;

import com.google.common.collect.Maps;
import lz.izmoqwy.core.utils.StoreUtil;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CoopsManager {

	private static Map<String, List<UUID>> coops = Maps.newHashMap();
	private static Map<UUID, List<String>> coopedOn = Maps.newHashMap();

	/*
		Coop methods
	 */

	public static void coop(UUID uuid, String island_id, Player initier) {
		StoreUtil.addToMapList(coops, island_id, uuid);
		StoreUtil.addToMapList(coopedOn, uuid, island_id);

		// Todo: Notify island's members
	}

	public static void unCoop(UUID uuid, String island_id, boolean notify) {
		if (StoreUtil.removeFromMapList(coops, island_id, uuid)) {
			StoreUtil.removeFromMapList(coopedOn, uuid, island_id);
			if (notify) {
				// Todo: Notify island's members
			}
		}
	}

	public static boolean isCooped(UUID uuid, String island_id) {
		return StoreUtil.isInMapList(coops, island_id, uuid);
	}

	public static List<UUID> getCoops(String island_id) {
		if (!coops.containsKey(island_id))
			return null;

		return coops.get(island_id);
	}

	/*
		Extra methods
	 */
	public static void unregisterIsland(final String island_id) {
		if (!coops.containsKey(island_id))
			return;

		final UUID[] coopeds = coops.get(island_id).toArray(new UUID[0]);
		for (UUID cooped : coopeds)
			unCoop(cooped, island_id, false);

		coops.remove(island_id);
	}

	public static void handleDisconnect(Player player) {
		UUID uuid = player.getUniqueId();
		if (coopedOn.containsKey(uuid)) {
			final String[] islands = coopedOn.get(uuid).toArray(new String[0]);
			for (String island_id : islands)
				unCoop(uuid, island_id, true);
		}
	}

}
