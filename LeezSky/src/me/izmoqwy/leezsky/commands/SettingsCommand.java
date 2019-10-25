package me.izmoqwy.leezsky.commands;

import lz.izmoqwy.core.api.CommandOptions;
import lz.izmoqwy.core.api.CoreCommand;
import me.izmoqwy.leezsky.managers.SettingsManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SettingsCommand extends CoreCommand {

	public SettingsCommand() {
		super("settings", new CommandOptions().withCooldown(5).playerOnly());
	}

	@Override
	protected void execute(CommandSender commandSender, String usedCommand, String[] args) {
		Player player = ((Player) commandSender);
		player.openInventory(SettingsManager.bakeInventory(player));
	}
}
