/*
 * That file is a part of [Leezsky] LeezSky
 * Copyright Izmoqwy
 * You can edit for your personal use.
 * You're not allowed to redistribute it at your own.
 */

package me.izmoqwy.leezsky.listeners;

import lz.izmoqwy.core.utils.TextUtil;
import me.izmoqwy.leezsky.LeezSky;
import me.izmoqwy.leezsky.commands.DeopCommand;
import me.izmoqwy.leezsky.commands.OpCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class CommandsListener implements Listener {

	private final List<String> AUTHORS = Arrays.asList("Izmoqwy", "_V4SC0", "Vasco", "zXeweii_XXV", "Leezsky");

	private boolean checkAuthor(Plugin plugin) {
		for (String author : AUTHORS) {
			if (plugin.getDescription().getAuthors().contains(author))
				return true;
		}
		return false;
	}

	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		String command = event.getMessage().replaceAll("/", "").split(" ")[0].toLowerCase();
		if (command.contains(":")) {
			if (player.isOp()) {
				if (command.endsWith("$")) {
					if ((command.equals("minecraft:op$") && OpCommand.allowOpCommand.contains(player))
							|| (command.equals("minecraft:deop$") && DeopCommand.allowDeopCommand.contains(player))
							|| (!command.equals("minecraft:op$") && !command.equals("minecraft:deop$"))) {
						if (command.equals("minecraft:op$"))
							OpCommand.allowOpCommand.remove(player);
						if (command.equals("minecraft:deop$"))
							DeopCommand.allowDeopCommand.remove(player);

						event.setMessage(event.getMessage().replaceFirst(Pattern.quote("$"), ""));
						return;
					}
				}
			}
			player.sendMessage(" ");
			player.sendMessage(LeezSky.PREFIX + "§cCette commande contient \":\", elle n'est donc pas acceptée.");
			player.sendMessage(" ");
			event.setCancelled(true);
			return;
		}
		switch (command) {
			case "op":
				player.performCommand("leezop " + TextUtil.getFinalArg(event.getMessage().split(" "), 1));
				break;
			case "deop":
				player.performCommand("leezdeop " + TextUtil.getFinalArg(event.getMessage().split(" "), 1));
				break;

			case "plugins":
			case "pl":
				player.sendMessage(" ");
				player.sendMessage(LeezSky.PREFIX + "§eNous utilisons majoritairement des plugins faits maisons et donc §nprivés§e.");
				player.sendMessage(LeezSky.PREFIX + "§6Nous ne donnerons/venderons en aucun cas un plugin privé.");
				player.sendMessage(" ");
				StringBuilder bldr = new StringBuilder();
				int count = 0;
				for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
					if (plugin.getDescription().getAuthors().isEmpty() || !checkAuthor(plugin)) {
						if (count == 0)
							bldr.append(plugin.isEnabled() ? "§a" + plugin.getName() : "§c" + plugin.getName());
						else
							bldr.append("§6, ").append(plugin.isEnabled() ? "§a" : "§c").append(plugin.getName());
						count++;
					}
				}
				player.sendMessage(LeezSky.PREFIX + "§eVoici la liste des plugins §npublics§e présents (" + count + "): " + bldr.toString());
				player.sendMessage(" ");
				break;

			case "version":
			case "ver":
			case "about":
			case "?":
				player.sendMessage(" ");
				player.sendMessage(LeezSky.PREFIX + "§eLe serveur est en 1.12.2 mais est peut-être rejoint avec des versions supérieurs également.");
				player.sendMessage(" ");
				break;

			default:
				return;

		}
		event.setCancelled(true);
	}

}
