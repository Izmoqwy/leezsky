package lz.izmoqwy.island.commands;

import com.google.common.collect.Lists;
import lz.izmoqwy.core.command.CoreTabCompleter;
import lz.izmoqwy.island.grid.CoopsManager;
import lz.izmoqwy.island.island.IslandMember;
import lz.izmoqwy.island.island.IslandRole;
import lz.izmoqwy.island.players.SkyblockPlayer;
import lz.izmoqwy.island.players.Wrapper;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerCommandTabCompleter extends CoreTabCompleter {

	private static List<String> NO_ISLAND = Arrays.asList("create", "info", "help");

	private static List<String> ISLAND_DEFAULT = Arrays.asList("go", "info", "help", "personalhome", "level", "team", "coops", "banlist", "expel", "teamchat", "send", "vault"),
			ISLAND_OFFICIER, ISLAND_RECRUTER;

	static {
		ISLAND_RECRUTER = Lists.newArrayList("invite", "kick", "coop", "uncoop");
		ISLAND_RECRUTER.addAll(ISLAND_DEFAULT);

		ISLAND_OFFICIER = Lists.newArrayList("sethome", "setwarp", "promote", "demote");
		ISLAND_OFFICIER.addAll(ISLAND_RECRUTER);
	}

	public PlayerCommandTabCompleter() {
		super("island", true, null);
	}

	@Override
	public List<String> get(CommandSender commandSender, String usedCommand, String[] args) {
		if (args.length == 1) {
			SkyblockPlayer player = Wrapper.wrapPlayer((Player) commandSender);
			if (player == null)
				return null;

			if (!player.hasIsland()) {
				return keepOnlyWhatMatches(NO_ISLAND, args[0]);
			}
			else {
				if (player.getIsland().hasRoleOrAbove(player, IslandRole.OFFICIER)) {
					return keepOnlyWhatMatches(ISLAND_OFFICIER, args[0]);
				}
				else if (player.getIsland().hasRoleOrAbove(player, IslandRole.RECRUTER)) {
					return keepOnlyWhatMatches(ISLAND_RECRUTER, args[0]);
				}
				else {
					return keepOnlyWhatMatches(ISLAND_DEFAULT, args[0]);
				}
			}
		}
		else if (args.length == 2) {
			SkyblockPlayer player = Wrapper.wrapPlayer((Player) commandSender);
			if (player == null || !player.hasIsland())
				return null;

			switch (args[0].toLowerCase()) {
				case "info":
					return allPlayers(args[1]);
				case "expel":
				case "invite":
				case "coop":
				case "ban":
					if (match(args, 1, "invite", "coop", "ban") && !player.getIsland().hasRoleOrAbove(player, IslandRole.RECRUTER)) {
						return null;
					}

					List<String> members = player.getIsland().getMembersMap().keySet().stream().map(uuid -> Bukkit.getOfflinePlayer(uuid).getName()).collect(Collectors.toList());
					members.add(player.getIsland().getOwner().getName());
					return allPlayersBut(args[1], members);
				case "kick":
					if (!player.getIsland().hasRoleOrAbove(player, IslandRole.RECRUTER)) {
						return null;
					}

					members = Lists.newArrayList();
					IslandRole role = player.getIsland().getRole(player);
					for (IslandMember member : player.getIsland().getMembersMap().values()) {
						if (member.getRole().ordinal() < role.ordinal())
							members.add(Bukkit.getOfflinePlayer(member.getUniqueId()).getName());
					}
					return keepOnlyWhatMatches(members, args[1]);
				case "uncoop":
					if (!player.getIsland().hasRoleOrAbove(player, IslandRole.RECRUTER)) {
						return null;
					}

					List<UUID> coops = CoopsManager.getCoops(player.getIsland().ID);
					if (coops == null)
						return null;
					return keepOnlyWhatMatches(coops.stream().map(uuid -> Bukkit.getOfflinePlayer(uuid).getName()).collect(Collectors.toList()), args[1]);
				case "promote":
				case "demote":
					if (!player.getIsland().hasRoleOrAbove(player, IslandRole.OFFICIER)) {
						return null;
					}

					members = Lists.newArrayList();
					role = player.getIsland().getRole(player);
					for (IslandMember member : player.getIsland().getMembersMap().values()) {
						if (member.getRole().ordinal() < role.ordinal())
							members.add(Bukkit.getOfflinePlayer(member.getUniqueId()).getName());
					}
					return keepOnlyWhatMatches(members, args[1]);
				case "unban":
					if (!player.getIsland().hasRoleOrAbove(player, IslandRole.RECRUTER)) {
						return null;
					}
					return keepOnlyWhatMatches(player.getIsland().getBanneds().stream().map(uuid -> Bukkit.getOfflinePlayer(uuid).getName()).collect(Collectors.toList()), args[1]);
			}

		}
		return null;
	}

}
