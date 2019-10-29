package lz.izmoqwy.core.gui;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

@Getter
public class GUIState {

	private GUIInvoker invoker;
	private GUIState parent;

	public GUIState(GUIInvoker invoker, GUIState parent) {
		this.invoker = invoker;
		this.parent = parent;
	}

	public interface GUIInvoker {
		Inventory invoke(Player player);
	}

}
