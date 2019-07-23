package lz.izmoqwy.market.blackmarket;

import lz.izmoqwy.core.CorePrinter;
import lz.izmoqwy.core.helpers.PluginHelper;
import lz.izmoqwy.market.rpg.commands.FishCommand;
import lz.izmoqwy.market.rpg.commands.InventoryCommand;

public class Blackmarket {

	public static void loadRPG() {
		CorePrinter.print("Loading RPG (BlackMarket) commands...");
		PluginHelper.loadCommand("rpginventory", new InventoryCommand("rpginventory"));
		PluginHelper.loadCommand("rpgfish", new FishCommand("rpgfish"));
	}

	public static void loadAll() {
		loadRPG();
	}
}
