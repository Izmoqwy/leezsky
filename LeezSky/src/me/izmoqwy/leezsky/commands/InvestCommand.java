package me.izmoqwy.leezsky.commands;

import lz.izmoqwy.core.api.CommandOptions;
import lz.izmoqwy.core.api.CoreCommand;
import lz.izmoqwy.core.utils.ItemUtil;
import me.izmoqwy.leezsky.managers.InvestManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class InvestCommand extends CoreCommand {

	public static final Inventory INVENTORY = Bukkit.createInventory(null, 9 * 3, InvestManager.GUI_NAME);
	static {
		INVENTORY.setItem(11, ItemUtil.createItem(Material.GOLD_NUGGET, "§ePlacer de l'argent"));
		INVENTORY.setItem(15, ItemUtil.createItem(Material.GOLD_INGOT, "§eRetirer de l'argent"));
	}

	public InvestCommand() {
		super("invest", new CommandOptions().withPermission("leezsky.invest").withCooldown(10).playerOnly());
	}

	@Override
	protected void execute(CommandSender commandSender, String usedCommand, String[] args) {
		((Player) commandSender).openInventory(INVENTORY);
	}
}
