package lz.izmoqwy.core.gui;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Getter
public class GUIValueChangedEvent {

	@Setter
	private boolean cancelled = false;
	@Setter
	private boolean defaultRefactoring = true;

	private Player player;

	private final String inventoryName;
	private final int clickedSlot;
	private final ItemStack clickedItem;

	private final Enum previousValue, newValue;

	public GUIValueChangedEvent(Player player, String inventoryName, int clickedSlot, ItemStack clickedItem, Enum previousValue, Enum newValue) {
		this.player = player;
		this.inventoryName = inventoryName;
		this.clickedSlot = clickedSlot;
		this.clickedItem = clickedItem;
		this.previousValue = previousValue;
		this.newValue = newValue;
	}
}
