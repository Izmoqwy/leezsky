package lz.izmoqwy.core.top;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lz.izmoqwy.core.LeezCore;
import lz.izmoqwy.core.helpers.PluginHelper;
import lz.izmoqwy.core.utils.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.Map;

public class TopManager implements Listener {

	private static Map<String, Top> tops = Maps.newHashMap();
	private static List<String> inventoryNames = Lists.newArrayList();

	private static Map<String, Inventory> inventories = Maps.newHashMap();

	public static void load(LeezCore from) {
		PluginHelper.loadListener(from, new TopManager());
	}

	public static void register(Top top) {
		inventoryNames.add("§3" + top.getTopName());

		if (top.getCommandName() != null) {
			tops.put(top.getCommandName(), top);
			PluginHelper.loadCommand(top.getCommandName(), new TopCommand(top.getCommandName()));
		}
		else tops.put(top.getTopName(), top);
	}

	private static void calc(Top top) {
		TopResult[] results = top.calcTop();
		Inventory inventory = Bukkit.createInventory(null, 5 * 9, "§3" + top.getTopName());

		int curr = 0;
		for (TopResult result : results) {
			if (result == null)
				break;

			int slot;
			switch (curr) {
				case 0:
					slot = 13;
					break;
				case 1:
					slot = 21;
					break;
				case 2:
					slot = 23;
					break;
				default:
					// e.g: 3 -> 28
					slot = curr + 25;
			}
			curr++;

			final String playerName = result.getPlayer().getName();
			inventory.setItem(slot, ItemUtil.skull(playerName, "§e" + playerName, result.getItemDescription()));
		}

		inventories.put(top.getCommandName(), inventory);
	}

	public static Inventory getGui(String commandName) {
		if (inventories.containsKey(commandName)) {
			return inventories.get(commandName);
		}
		else {
			calc(tops.get(commandName));
			return getGui(commandName);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onClick(final InventoryClickEvent event) {
		if (event.getClickedInventory() != null && inventoryNames.contains(event.getClickedInventory().getName()))
			event.setCancelled(true);
	}

}
