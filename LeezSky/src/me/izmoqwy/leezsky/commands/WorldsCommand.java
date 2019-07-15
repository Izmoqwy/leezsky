package me.izmoqwy.leezsky.commands;

import lz.izmoqwy.core.api.CommandOptions;
import lz.izmoqwy.core.api.CoreCommand;
import me.izmoqwy.leezsky.LeezSky;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class WorldsCommand extends CoreCommand {

	public WorldsCommand() {
		super("worlds", new CommandOptions().withPermission("leezsky.admin.worlds"));
	}

	@Override
	protected void execute(CommandSender commandSender, String usedCommand, String[] args) {
		commandSender.sendMessage(" ");
		commandSender.sendMessage(LeezSky.PREFIX + "§6Liste des mondes §7(" + Bukkit.getWorlds().size() + ")§6:");
		Bukkit.getWorlds().forEach(world -> commandSender.sendMessage("§8- §e" + world.getName() + " §7(" + world.getWorldType().name() + ")"));
		commandSender.sendMessage(" ");
	}

}
