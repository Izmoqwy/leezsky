/*
 * That file is a part of [Leezsky] LeezSky
 * Copyright Izmoqwy
 * You can edit for your personal use.
 * You're not allowed to redistribute it at your own.
 */

package me.izmoqwy.leezsky.listeners;

import lz.izmoqwy.core.utils.TextUtil;
import me.izmoqwy.leezsky.LeezSky;
import me.izmoqwy.leezsky.commands.management.DeopCommand;
import me.izmoqwy.leezsky.commands.management.OpCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandsListener implements Listener {

	private final List<String> PLUGIN_AUTHORS = Arrays.asList("Izmoqwy", "_V4SC0", "Vasco", "Leezsky");

	private boolean isPublic(Plugin plugin) {
		for (String author : PLUGIN_AUTHORS) {
			if (plugin.getDescription().getAuthors().contains(author))
				return false;
		}
		return true;
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
				player.sendMessage(LeezSky.PREFIX + "§eNous utilisons principalement des plugins §nprivés§e mais nous utilisons également des plugins publics qui conviennent à " +
						"nos besoins.");
				player.sendMessage(LeezSky.PREFIX + "§6Nous ne donnerons/vendrons en aucun cas un plugin privé.");
				player.sendMessage(" ");

				Stream<String> publicPlugins = Arrays.stream(Bukkit.getPluginManager().getPlugins())
						.filter(plugin -> plugin.getDescription().getAuthors().isEmpty() || isPublic(plugin))
						.map(plugin -> (plugin.isEnabled() ? ChatColor.GREEN : ChatColor.RED) + plugin.getName());
				player.sendMessage(LeezSky.PREFIX + "§eVoici la liste des plugins §npublics§e présents (" + publicPlugins.count() + "): " +
						publicPlugins.collect(Collectors.joining("§6, ")));

				player.sendMessage(" ");
				break;

			case "version":
			case "ver":
			case "about":
			case "?":
				player.sendMessage(" ");
				player.sendMessage(LeezSky.PREFIX + "§eLe serveur est en 1.12.2 mais est peut-être rejoint avec des versions supérieures également.");
				player.sendMessage(" ");
				break;

			default:
				return;

		}
		event.setCancelled(true);
	}

}
