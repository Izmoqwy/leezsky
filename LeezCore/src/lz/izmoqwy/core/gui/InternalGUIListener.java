package lz.izmoqwy.core.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InternalGUIListener implements Listener {

	private boolean isHandledGUI(Inventory inventory) {
		return inventory != null && inventory.getHolder() instanceof InternalGUIHolder;
	}

	private MinecraftGUI getHandledGUI(InventoryEvent event) {
		if (isHandledGUI(event.getInventory()))
			return ((InternalGUIHolder) event.getInventory().getHolder()).getMinecraftGUI();
		return null;
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryOpen(InventoryOpenEvent event) {
		MinecraftGUI handledGUI = getHandledGUI(event);
		if (handledGUI == null)
			return;

		Player player = (Player) event.getPlayer();
		for (MinecraftGUIListener listener : handledGUI.getListeners()) {
			if (!listener.canOpen(player))
				event.setCancelled(true);
			listener.onOpen(player);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryClose(InventoryCloseEvent event) {
		MinecraftGUI handledGUI = getHandledGUI(event);
		if (handledGUI == null)
			return;

		Player player = (Player) event.getPlayer();
		for (MinecraftGUIListener listener : handledGUI.getListeners()) {
			listener.onClose(player);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		MinecraftGUI handledGUI = getHandledGUI(event);
		if (handledGUI == null)
			return;

		if (handledGUI.isCancelClicks())
			event.setCancelled(true);
		if (!event.getInventory().equals(event.getClickedInventory()))
			return;

		Player player = (Player) event.getWhoClicked();
		ItemStack currentItem = event.getCurrentItem();
		int slot = event.getSlot();

		boolean pointless = currentItem == null || currentItem.getType() == Material.AIR;
		for (MinecraftGUIListener listener : handledGUI.getListeners()) {
			if (!pointless)
				listener.onClick(player, currentItem, slot);
			listener.onRichClick(player, currentItem, slot, event);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryMoveItem(InventoryMoveItemEvent event) {
		if (isHandledGUI(event.getDestination()))
			event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryDrag(InventoryDragEvent event) {
		if (isHandledGUI(event.getInventory()))
			event.setCancelled(true);
	}

}
