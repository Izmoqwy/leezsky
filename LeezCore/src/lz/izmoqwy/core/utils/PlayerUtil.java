package lz.izmoqwy.core.utils;

import com.google.common.base.Preconditions;
import lz.izmoqwy.core.nms.NMS;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PlayerUtil {

	@SuppressWarnings("deprecation")
	public static OfflinePlayer getOfflinePlayer(String name) {
		Preconditions.checkNotNull(name);

		Player onlinePlayer = Bukkit.getPlayerExact(name);
		if (onlinePlayer != null)
			return onlinePlayer;

		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
		if (offlinePlayer != null && offlinePlayer.hasPlayedBefore())
			return offlinePlayer;

		return null;
	}

	/*
	Inventory
	 */
	public static int getAmountInInventory(Player player, ItemStack model) {
		return player.getInventory().all(model).keySet().stream().mapToInt(Integer::intValue).sum();
	}

	/**
	 * @deprecated Nothing to do here and InventoryUtil is useless, removing ASAP
	 */
	@Deprecated
	public static Inventory copyInventory(Inventory origin, String newName) {
		Inventory inventory = Bukkit.createInventory(origin.getHolder(), origin.getSize(), newName == null ? origin.getName() : newName);
		inventory.setContents(origin.getContents());
		return inventory;
	}

	/*
	NMS
	 */
	public static void sendTitle(Player player, String title, String subTitle, int seconds) {
		Validate.notNull(player);

		NMS.packet.sendTitle(player, title, subTitle);
		NMS.packet.sendTimings(player, seconds * 20, 5, 5);
	}

}
