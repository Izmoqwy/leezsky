package lz.izmoqwy.core.utils;

import lz.izmoqwy.core.nms.NmsAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class BukkitUtil {

	public static Player loadPlayer(OfflinePlayer player) {
		if (player.isOnline()) return Bukkit.getPlayer(player.getUniqueId());

		Player target = NmsAPI.packet.loadPlayer(player);
		if (target != null) target.loadData();
		return target;
	}

}
