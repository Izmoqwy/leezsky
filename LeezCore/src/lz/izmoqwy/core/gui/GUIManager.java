package lz.izmoqwy.core.gui;

import com.google.common.collect.Maps;
import io.netty.util.internal.UnstableApi;
import lombok.Getter;
import lz.izmoqwy.core.self.LeezCore;
import lz.izmoqwy.core.api.ItemBuilder;
import lz.izmoqwy.core.nms.NMS;
import lz.izmoqwy.core.utils.ItemUtil;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Map;

/**
 * @deprecated use {@link MinecraftGUI} instead
 */
@Deprecated
public class GUIManager implements Listener {

	private static final ItemStack GO_BACK = ItemUtil.createItem(Material.ARROW, "§cRetour", Collections.singletonList("§7Retourner a la page precedente"));
	private static Map<String, GUIActions> inventories = Maps.newHashMap();

	public static void registerInventory(String inventoryName, GUIActions actions) {
		//Map<Integer, GUIFireAction> slots = Maps.newHashMap();
		//actions.getSlots().forEach(slots::put);

		inventories.put(inventoryName, actions);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	private void onClick(InventoryClickEvent event) {
		if (event.getClickedInventory() == null || event.getWhoClicked() == null) {
			return;
		}

		Player player = (Player) event.getWhoClicked();
		if (event.getClickedInventory().equals(player.getInventory())) {
			String title = player.getOpenInventory().getTitle();
			if (player.getOpenInventory() != null && inventories.containsKey(title)) {
				if (inventories.get(title).protect)
					event.setCancelled(true);
			}
			return;
		}

		final String inventoryName = event.getClickedInventory().getName();
		final int clickedSlot = event.getSlot();
		for (Map.Entry<String, GUIActions> entry : inventories.entrySet()) {
			String inventory = entry.getKey();
			GUIActions actions = entry.getValue();
			if (inventory.contains("%s") ? inventoryName.startsWith(inventory.split("%s")[0]) : inventory.equals(inventoryName)) {
				if (event.getCurrentItem() != null) {
					if (currentOpenedStates.containsKey(player) && clickedSlot == event.getInventory().getSize() - 1) {
						GUIState state = currentOpenedStates.get(player);
						if (state.getParent() != null) {
							currentOpenedStates.replace(player, state.getParent());
							player.openInventory(state.getParent().getInvoker().invoke(player));
							return;
						}
					}
					actions.slots.forEach((slot, action) -> {
						if (slot == -1 || slot == clickedSlot) {
							event.setCancelled(action.fireAction(player, new GUIClickEvent(event, actions)));
						}
					});
				}
				if (actions.protect) {
					event.setCancelled(true);
				}
				break;
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	private void onInventoryOpen(InventoryOpenEvent event) {
		if (event.getInventory() == null || event.getPlayer() == null) {
			return;
		}

		Player player = (Player) event.getPlayer();
		Inventory inv = event.getInventory();

		final String inventoryName = event.getInventory().getName();
		for (Map.Entry<String, GUIActions> entry : inventories.entrySet()) {
			String inventory = entry.getKey();
			GUIActions actions = entry.getValue();
			if (inventory.contains("%s") ? inventoryName.startsWith(inventory.split("%s")[0]) : inventory.equals(inventoryName)) {
				actions.slots.forEach((slot, action) -> {
					if (slot >= 0 && slot < inv.getSize()) {
						ItemStack item = inv.getItem(slot);
						if (item == null)
							return;

						if (action instanceof ValueChanger) {
							ValueChanger valueChanger = (ValueChanger) action;
							if (valueChanger.refactorOnOpen) {
								inv.setItem(slot, valueChanger.refactor(new ItemBuilder(item), player).toItemStack());
							}
						}
					}
				});
				if (currentOpenedStates.containsKey(player) && currentOpenedStates.get(player).getParent() != null) {
					inv.setItem(inv.getSize() - 1, GO_BACK);
				}
				break;
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onInventoryClose(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();
		if (currentOpenedStates.containsKey(player)) {
			Bukkit.getScheduler().runTaskLater(LeezCore.instance, () -> {
				if (!NMS.global.hasInventoryOpened(player))
					currentOpenedStates.remove(player);
			}, 1);
		}
	}

	@Getter
	public static class GUIActions {

		private Map<Integer, GUIFireAction> slots;
		private GUIState.GUIInvoker invoker;
		private boolean protect;

		public GUIActions(Map<Integer, GUIFireAction> slots, GUIState.GUIInvoker invoker, boolean protect) {
			this.slots = slots;
			this.invoker = invoker;
			this.protect = protect;
		}
	}

	public static class GUIActionsBuilder {

		private Map<Integer, GUIFireAction> slots = Maps.newHashMap();
		private GUIState.GUIInvoker invoker;
		private boolean protect = true;

		public GUIActionsBuilder disableProtect() {
			this.protect = false;
			return this;
		}

		public GUIActionsBuilder onSlot(int slot, GUIFireAction action) {
			slots.put(slot, action);
			return this;
		}

		public GUIActionsBuilder onSlot(int slot, GUIActions toOpen) {
			slots.put(slot, new GUIOpener(toOpen));
			return this;
		}

		public GUIActionsBuilder invoker(GUIState.GUIInvoker invoker) {
			this.invoker = invoker;
			return this;
		}

		public GUIActionsBuilder valueChanger(GUIValueChanger listener, int slot, ValueChangerFireActon playerCurrentValue, boolean refactorOnOpen, Enum... values) {
			slots.put(slot, new ValueChanger(listener, playerCurrentValue, refactorOnOpen, values));
			return this;
		}

		public GUIActions build() {
			return new GUIActions(slots, invoker, protect);
		}

		public GUIActions buildAndRegister(String guiName) {
			GUIActions guiActions = build();
			registerInventory(ChatColor.translateAlternateColorCodes('&', guiName), guiActions);
			return guiActions;
		}
	}

	private static Map<Player, GUIState> currentOpenedStates = Maps.newHashMap();

	@Getter
	private static class GUIOpener implements GUIFireAction {

		private GUIActions toOpen;

		public GUIOpener(GUIActions toOpen) {
			if (toOpen.invoker == null) {
				throw new NullPointerException("Invoker is null on a GUIActions registred to open!");
			}
			this.toOpen = toOpen;
		}

		@Override
		public boolean fireAction(Player player, GUIClickEvent event) {
			GUIState parent = currentOpenedStates.getOrDefault(player, null);
			if (parent == null) {
				if (event.getGui().getInvoker() != null) {
					parent = new GUIState(event.getGui().getInvoker(), null);
				}
			}
			currentOpenedStates.put(player, new GUIState(toOpen.invoker, parent));
			player.openInventory(toOpen.invoker.invoke(player));
			return true;
		}
	}

	private static class ValueChanger implements GUIFireAction {
		private final GUIValueChanger listener;
		private final boolean refactorOnOpen;

		private ValueChangerFireActon playerCurrentValue;
		private final Enum[] values;

		public ValueChanger(GUIValueChanger listener, ValueChangerFireActon playerCurrentValue, boolean refactorOnOpen, Enum[] values) {
			this.listener = listener;
			this.playerCurrentValue = playerCurrentValue;
			this.refactorOnOpen = refactorOnOpen;
			this.values = values;
		}

		@Override
		public boolean fireAction(Player player, GUIClickEvent event) {
			GUIManager.callValueChanger(listener, player, event, values, playerCurrentValue.fireAction(player));
			return true;
		}

		public ItemBuilder refactor(ItemBuilder itemBuilder, Player player) {
			return refactor(itemBuilder, playerCurrentValue.fireAction(player));
		}

		public static ItemBuilder refactor(ItemBuilder itemBuilder, Enum value) {
			return itemBuilder.editLoreLine(0, "§6Etat: §e" + value.toString());
		}
	}

	private static void callValueChanger(GUIValueChanger listener, Player player, InventoryClickEvent inventoryClickEvent, Enum[] values, Enum currentValue) {
		if (values.length < 2)
			return;

		ItemStack item = inventoryClickEvent.getCurrentItem();

		int index = ArrayUtils.indexOf(values, currentValue);
		if (index < values.length - 1)
			index++;
		else
			index = 0;

		Enum newValue = values[index];
		GUIValueChangedEvent
				event = new GUIValueChangedEvent(player, inventoryClickEvent.getClickedInventory().getName(), inventoryClickEvent.getSlot(), item, currentValue, newValue);
		listener.onChange(event);
		if (!event.isCancelled()) {
			if (event.isDefaultRefactoring()) {
				inventoryClickEvent.getClickedInventory().setItem(inventoryClickEvent.getSlot(), ValueChanger.refactor(new ItemBuilder(item), newValue).toItemStack());
			}
		}
	}

	public class GUIClickEvent extends InventoryClickEvent {

		@Getter
		private GUIActions gui;

		public GUIClickEvent(InventoryClickEvent event, GUIActions gui) {
			super(event.getView(), event.getSlotType(), event.getSlot(), event.getClick(), event.getAction());
			this.gui = gui;
		}
	}

	public interface GUIValueChanger {
		void onChange(GUIValueChangedEvent event);
	}

	public interface GUIFireAction {
		boolean fireAction(Player player, GUIClickEvent event);
	}

	public interface ValueChangerFireActon {
		Enum fireAction(Player player);
	}

}
