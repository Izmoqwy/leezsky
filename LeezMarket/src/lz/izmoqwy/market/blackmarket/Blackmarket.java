package lz.izmoqwy.market.blackmarket;

import lz.izmoqwy.core.CorePrinter;
import lz.izmoqwy.core.helpers.PluginHelper;
import lz.izmoqwy.market.rpg.commands.FishCommand;
import lz.izmoqwy.market.rpg.commands.InventoryCommand;
import lz.izmoqwy.market.rpg.commands.MineCommand;
import lz.izmoqwy.market.rpg.commands.StatsCommand;

public class Blackmarket {

	public static void loadRPG() {
		CorePrinter.print("Loading RPG (BlackMarket) commands...");
		PluginHelper.loadCommand("rpgstats", new StatsCommand("rpgstats"));
		PluginHelper.loadCommand("rpginventory", new InventoryCommand("rpginventory"));
		PluginHelper.loadCommand("rpgmine", new MineCommand("rpgmine"));
		PluginHelper.loadCommand("rpgfish", new FishCommand("rpgfish"));
	}

	public static void loadAll() {
		loadRPG();
	}
}
