package lz.izmoqwy.leezisland.commands;

import com.google.common.collect.Lists;
import lz.izmoqwy.core.utils.TextUtil;
import lz.izmoqwy.leezisland.grid.GridManager;
import lz.izmoqwy.leezisland.grid.IslandManager;
import lz.izmoqwy.leezisland.island.Island;
import lz.izmoqwy.leezisland.Locale;
import lz.izmoqwy.leezisland.players.Wrapper;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class AdminCommand implements CommandExecutor {

	public static List<UUID> BYPASSING = Lists.newArrayList();

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("isadmin")) {
			if (args.length >= 1 && args[0].equalsIgnoreCase("bypass")) {
				if (sender instanceof Player) {
					UUID uuid = ((Player) sender).getUniqueId();
					if (BYPASSING.contains(uuid)) {
						BYPASSING.remove(uuid);
						Locale.ADMIN_BYPASS_TOGGLEDOFF.send(sender);
					}
					else {
						BYPASSING.add(uuid);
						Locale.ADMIN_BYPASS_TOGGLEDON.send(sender);
					}
				}
				else
					Locale.COMMAND_PLAYERONLY.send(sender);
			}
			else if (args.length >= 1 && args[0].equalsIgnoreCase("info")) {
				if (args.length >= 2) {
					OfflinePlayer off = Bukkit.getOfflinePlayer(args[1]);
					if (off.isOnline() || off.hasPlayedBefore()) {
						Island island = Wrapper.wrapOffPlayerIsland(off);
						if (island != null) {
							sendIslandInfo(island, sender);
						}
						else
							Locale.TARGET_NOISLAND.send(sender);
					}
					else
						Locale.TARGET_NOTEXISTS.send(sender);
				}
				else {
					if (sender instanceof Player) {
						Player player = (Player) sender;
						if (player.getWorld() == GridManager.getWorld()) {
							Island island = GridManager.getIslandAt(player.getLocation());
							if (island != null) {
								sendIslandInfo(island, sender);
							}
							else
								Locale.PLAYER_NOTONISLAND.send(sender);
						}
						else
							Locale.PLAYER_NOTINWORLD.send(sender);
					}
					else
						Locale.COMMAND_PLAYERONLY.send(sender);
				}
			}
			else if (args.length >= 1 && args[0].equalsIgnoreCase("setrange")) {
				if (args.length >= 3) {
					OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
					if (player.isOnline() || player.hasPlayedBefore()) {
						Island island = Wrapper.wrapOffPlayerIsland(player);
						if (island != null) {
							int newRange;
							try {
								newRange = Integer.parseInt(args[2]);
							}
							catch (Exception ex) {
								Locale.ARGUMENT_NOTINT.send(sender);
								return true;
							}
							IslandManager.setRange(island, newRange);
							sender.sendMessage(Locale.PREFIX + "§aLe rayon de l'île §b#" + island.ID + " §aest désormais de §e" + newRange + "§a.");
						}
						else
							Locale.TARGET_NOISLAND.send(sender);
					}
					else
						Locale.TARGET_NOTEXISTS.send(sender);
				}
				else
					Locale.COMMAND_INVALID.send(sender);
			}
			else if (args.length >= 1 && (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?"))) {

				sender.sendMessage(" ");

				sender.sendMessage(Locale.PREFIX + "§3Aide de la commande /isadmin:");
				sender.sendMessage("§6/isadmin §ebypass §8- §ePrendre les permissions admin pour pouvoir intéragir les îles des joueurs");
				sender.sendMessage(" ");
				sender.sendMessage("§6/isadmin §einfo (Pseudo) §8- §eAfficher toutes les informations d'une île spécifique ou de celle où vous êtes");
				sender.sendMessage("§6/isadmin §esetrange <Pseudo> <Nouveau rayon> §8- §eDéfinir un nouveau rayon pour une île");

				sender.sendMessage(" ");

			}
			else
				Locale.COMMAND_UNKOWN.send(sender);
			return true;
		}
		return false;
	}

	protected static void sendIslandInfo(Island island, CommandSender sender) {

		sender.sendMessage(" ");

		sender.sendMessage(Locale.PREFIX + "§3Informations de l'île §b#" + island.ID + "§3:");
		if (island.getName() != null)
			sender.sendMessage(Locale.PREFIX + "§6Nom: §e" + island.getName());
		sender.sendMessage(Locale.PREFIX + "§6Propriétaire: §e" + island.getOwner().getName());
		sender.sendMessage("§8➟ §7" + island.getOwner().getUniqueId().toString());

		if (island.getMembersMap().size() > 0) {
			sender.sendMessage(Locale.PREFIX + "§6Membres §7(" + island.getMembersMap().size() + ")§6:");
			island.getMembersMap().values().forEach(member -> sender.sendMessage("§8- §e" + Bukkit.getOfflinePlayer(member.getUniqueId()).getName() + " §7(" + member.getRole().name + ")"));
		}

		int range = island.getRange() * 2 + 1;
		sender.sendMessage(Locale.PREFIX + "§6Niveau: §e" + island.getLevel());
		sender.sendMessage(Locale.PREFIX + "§6Diamètre: §e" + range + "x" + range);

		List<UUID> banneds = island.getBanneds();
		if (banneds.size() > 0) {
			sender.sendMessage(Locale.PREFIX + "§6Bannis: " + TextUtil.iterate(banneds, banned -> Bukkit.getOfflinePlayer(banned).getName(), "§c", "§6, "));
		}
		sender.sendMessage(Locale.PREFIX + "§6Fermée: §e" + (island.isLocked() ? "oui" : "non"));

		sender.sendMessage(" ");

	}

}
