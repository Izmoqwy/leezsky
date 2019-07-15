package lz.izmoqwy.core.utils;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

public class InventoryUtil {

	public static Inventory copy(Inventory origin, String newName) {
		Inventory inventory = Bukkit.createInventory(origin.getHolder(), origin.getSize(), newName == null ? origin.getName() : newName);
		inventory.setContents(origin.getContents());
		return inventory;
	}

}
