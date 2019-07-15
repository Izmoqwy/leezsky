package lz.izmoqwy.core.api;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public abstract class CoreTabCompleter implements TabCompleter {

	private String name, permission;
	private boolean playerOnly;

	public CoreTabCompleter(String name, CompleteOptions options) {
		this.name = name;

		this.permission = options.permission;
		this.playerOnly = options.playerOnly;
	}

	@Override
	public List<String> onTabComplete(CommandSender commandSender, Command command, String usedCommand, String[] args) {
		if (command.getName().equalsIgnoreCase(name)) {
			if (commandSender instanceof Player) {
				if (permission != null && !commandSender.hasPermission(permission)) {
					return Lists.newArrayList();
				}
				else {
					List<String> result = get(commandSender, usedCommand, args);
					if (result == null)
						return Lists.newArrayList();

					Collections.sort(result);
					return result;
				}
			}
			else {
				if (!playerOnly) {
					List<String> result = get(commandSender, usedCommand, args);
					if (result == null)
						return Lists.newArrayList();

					Collections.sort(result);
					return result;
				}
				else
					commandSender.sendMessage("§cCommande reservée aux joueurs !");
			}
		}
		return Lists.newArrayList();
	}

	protected abstract List<String> get(CommandSender commandSender, String usedCommand, String[] args);

	protected static List<String> keepOnlyWhatMatches(List<String> list, String arg) {
		arg = arg.toLowerCase();
		List<String> toReturn = Lists.newArrayList();
		for (String str : list) {
			if (str.startsWith(arg))
				toReturn.add(str);
		}
		return toReturn;
	}

	protected static List<String> allPlayers(String arg) {
		arg = arg.toLowerCase();
		List<String> toReturn = Lists.newArrayList();
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.getName().toLowerCase().startsWith(arg))
				toReturn.add(player.getName());
		}
		return toReturn;
	}

	protected static List<String> allPlayersBut(String arg, List<String> excludes) {
		arg = arg.toLowerCase();
		List<String> toReturn = Lists.newArrayList();
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (excludes.contains(player.getName()))
				continue;
			if (player.getName().toLowerCase().startsWith(arg))
				toReturn.add(player.getName());
		}
		return toReturn;
	}

	protected static boolean c(String arg, String... possibilites) {
		for (String s : possibilites) {
			if(arg.equalsIgnoreCase(s))
				return true;
		}
		return false;
	}

}
