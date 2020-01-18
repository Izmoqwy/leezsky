package me.izmoqwy.leezsky.commands;

import lz.izmoqwy.core.command.CommandOptions;
import lz.izmoqwy.core.command.CoreCommand;
import me.izmoqwy.leezsky.LeezSky;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class WorldsCommand extends CoreCommand {

	public WorldsCommand() {
		super("worlds", CommandOptions.builder()
				.permission("leezsky.commands.worlds")
				.build());
	}

	@Override
	protected void execute(CommandSender commandSender, String usedCommand, String[] args) {
		commandSender.sendMessage(" ");
		commandSender.sendMessage(LeezSky.PREFIX + "§6Liste des mondes §7(" + Bukkit.getWorlds().size() + ")§6:");
		Bukkit.getWorlds().forEach(world -> commandSender.sendMessage("§8- §e" + world.getName() + " §7(" + world.getWorldType().name() + ")"));
		commandSender.sendMessage(" ");
	}

}
