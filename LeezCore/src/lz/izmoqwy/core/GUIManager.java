package lz.izmoqwy.core;

import com.google.common.collect.Maps;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Map;

public class GUIManager implements Listener {

	private static Map<String, Map<Integer, GUIFireAction>> inventories = Maps.newHashMap();

	public static void registerInventory(String inventoryName, GUIActions actions) {
		Map<Integer, GUIFireAction> slots = Maps.newHashMap();
		actions.getSlots().forEach(slots::put);

		inventories.put(inventoryName, slots);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	private void onClick(InventoryClickEvent event) {
		if (event.getClickedInventory() == null || event.getWhoClicked() == null || event.getCurrentItem() == null) {
			return;
		}

		final String inventoryName = event.getClickedInventory().getName();
		final int clickedSlot = event.getSlot();
			inventories.forEach((inventory, slots) -> {
			if (inventory.contains("%s") ? inventoryName.startsWith(inventory.split("%s")[0]) : inventory.equals(inventoryName)) {
				slots.forEach((slot, action) -> {
					if (slot == -1 || slot == clickedSlot) {
						event.setCancelled(action.fireAction((Player) event.getWhoClicked(), event));
					}
				});
			}
		});
	}

	public static class GUIActions {

		@Getter
		private Map<Integer, GUIFireAction> slots;

		protected GUIActions(Map<Integer, GUIFireAction> slots) {
			this.slots = slots;
		}

	}

	public static class GUIActionsBuilder {

		private Map<Integer, GUIFireAction> slots = Maps.newHashMap();

		public GUIActionsBuilder onSlot(int slot, GUIFireAction action) {
			slots.put(slot, action);
			return this;
		}

		public GUIActions build() {
			return new GUIActions(slots);
		}

	}

	public interface GUIFireAction {
		boolean fireAction(Player player, InventoryClickEvent event);
	}

}
