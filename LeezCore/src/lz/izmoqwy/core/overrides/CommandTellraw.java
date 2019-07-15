package lz.izmoqwy.core.overrides;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import lz.izmoqwy.core.nms.NmsAPI;

public class CommandTellraw implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("tellraw")) {
			if (args.length < 1) return false;

			String json = getString(args, 1);
			Player target = Bukkit.getPlayer(args[0]);
			if (target != null) {
				NmsAPI.packet.sendJson(target, json);
			}
			else sender.sendMessage("§cInvalid player!");
			return false;
		}
		return false;

	}

	private String getString(String[] args, int start) {
		StringBuilder builder = new StringBuilder();

		for (int i = start; i < args.length; i++) {
			if (i != start) builder.append(" ");
			builder.append(args[i]);
		}
		return builder.toString();

	}

}
