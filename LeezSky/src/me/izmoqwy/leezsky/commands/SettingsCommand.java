package me.izmoqwy.leezsky.commands;

import lz.izmoqwy.core.command.CommandOptions;
import lz.izmoqwy.core.command.CoreCommand;
import me.izmoqwy.leezsky.managers.SettingsManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SettingsCommand extends CoreCommand {

	public SettingsCommand() {
		super("settings", CommandOptions.builder()
				.permission("leezsky.commands.settings").playerOnly(true)
				.cooldown(10)
				.build());
	}

	@Override
	protected void execute(CommandSender commandSender, String usedCommand, String[] args) {
		Player player = ((Player) commandSender);
		player.openInventory(SettingsManager.bakeInventory(player));
	}

}
