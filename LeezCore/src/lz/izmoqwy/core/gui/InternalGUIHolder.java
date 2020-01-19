package lz.izmoqwy.core.gui;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

@Getter
@AllArgsConstructor
class InternalGUIHolder implements InventoryHolder {

	private MinecraftGUI minecraftGUI;

	@Override
	public Inventory getInventory() {
		return null;
	}

}
