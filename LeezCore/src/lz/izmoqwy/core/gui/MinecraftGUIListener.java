package lz.izmoqwy.core.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public interface MinecraftGUIListener {

	default boolean canOpen(Player player) {
		return true;
	}

	default void onClick(Player player, ItemStack clickedItem, int slot) {
	}

	default void onRichClick(Player player, ItemStack clickedItem, int slot, InventoryClickEvent event) {
	}

	default void onClose(Player player) {
	}

}
