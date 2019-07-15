package me.izmoqwy.hardpermissions.commands;

import lz.izmoqwy.core.LeezCore;
import lz.izmoqwy.core.api.CommandOptions;
import lz.izmoqwy.core.api.CoreCommand;
import lz.izmoqwy.core.utils.TextUtil;
import me.izmoqwy.hardpermissions.Configs;
import me.izmoqwy.hardpermissions.Group;
import me.izmoqwy.hardpermissions.LeezPermissions;
import me.izmoqwy.hardpermissions.Locale;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class MainCommand extends CoreCommand {

	public MainCommand() {
		super("leezpermissions", new CommandOptions().withPermission("leezpermissions.command"));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void execute(CommandSender commandSender, String usedCommand, String[] args) {
		if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
			if (hasPerm(commandSender, "reload")) {
				LeezPermissions.reload();
				Locale.RELOADED.send(commandSender, LeezPermissions.getInstance().getDescription().getVersion());
			}
			else
				Locale.NOPERM_SUBCOMMAND.send(commandSender);
			LeezPermissions.reload();
		}
		else if (args.length >= 2) {
			if (c(args[0], "help", "?")) {
				displayHelp(commandSender);
			}
			else if ("group".equalsIgnoreCase(args[0])) {
				if (hasPerm(commandSender, "groups")) {
					if ("list".equalsIgnoreCase(args[1])) {
						if (!Configs.getAllGroups().isEmpty()) {
							Locale.GROUPS_LIST.send(commandSender);
							for (Group grp : Configs.getAllGroups()) {
								commandSender.sendMessage("§7- §b" + grp.getName());
							}
						}
						else
							Locale.GROUPS_NOGROUP.send(commandSender);
					}
					else if (args.length >= 3 && c(args[1], "perms", "permissions")) {
						final Group group = Configs.getGroup(args[2]);
						if (group != null) {
							if (!group.getBasePermissions().isEmpty()) {
								Locale.GROUP_LISTPERMS_HEADER.send(commandSender, group.getName());
								commandSender.sendMessage("§8➟ " + TextUtil.iterate(group.getBasePermissions(), perm -> perm, "§a", "§7, "));
							}
							else
								Locale.GROUP_LISTPERMS_NONE.send(commandSender, group.getName());

							if (group.hasInheritances())
								Locale.GROUP_LISTPERMS_INHERITANCES_LIST.send(commandSender, TextUtil.iterate(group.getInheritances(), Group::getName, Locale.GROUP_LISTPERMS_INHERITANCES_COLOR.toString(), "§7, "));
						}
						else
							Locale.GROUP_DOESNOTEXISTS.send(commandSender, args[2]);
					}
					else if (args.length >= 3 && args[1].equalsIgnoreCase("remove")) {
						final Group group = Configs.getGroup(args[2]);
						if (group != null) {
							Configs.removeGroup(group.getName());
							Locale.GROUP_DELETED.send(commandSender, group.getName());
						}
						else
							Locale.GROUP_DOESNOTEXISTS.send(commandSender, args[2]);
					}
					else if (args.length >= 3 && args[1].equalsIgnoreCase("add")) {
						final Group group = Configs.getGroup(args[2]);
						if (group == null) {
							Configs.addGroup(args[2]);
							Locale.GROUP_CREATED.send(commandSender, args[2]);
						}
						else
							Locale.GROUP_ALRDEXISTS.send(commandSender, group.getName());
					}
					else if (args.length >= 4 && args[1].equalsIgnoreCase("edit")) {
						if (c(args[3], "prefix", "suffix", "addperm", "delperm") && args.length >= 5) {
							final Group group = Configs.getGroup(args[2]);
							if (group != null) {
								final String name = group.getName();
								if (args[3].equalsIgnoreCase("prefix")) {
									group.setPrefix(TextUtil.getFinalArg(args, 4));
									Configs.editGroup(name, group);

									Locale.GROUP_EDITED.send(commandSender, name, "prefix", group.getPrefix());
								}
								else if (args[3].equalsIgnoreCase("suffix")) {
									group.setSuffix(TextUtil.getFinalArg(args, 4));
									Configs.editGroup(name, group);

									Locale.GROUP_EDITED.send(commandSender, name, "suffix", group.getSuffix());
								}
								else if (args[3].equalsIgnoreCase("addperm")) {
									final String perm = args[4].toLowerCase();
									if (!group.getPermissions().contains(perm)) {
										List<String> perms = group.getPermissions();
										perms.add(perm);
										group.setPermissions(perms);
										Configs.editGroup(name, group);

										Locale.GROUP_PERMADDED.send(commandSender, name, perm);
									}
									else
										Locale.GROUP_HASPERM.send(commandSender, group.getName(), perm);
								}
								else if (args[3].equalsIgnoreCase("delperm")) {
									final String perm = args[4].toLowerCase();
									if (group.getPermissions().contains(perm)) {
										List<String> perms = group.getPermissions();
										perms.remove(perm);
										group.setPermissions(perms);
										Configs.editGroup(name, group);

										Locale.GROUP_PERMREMOVED.send(commandSender, name, perm);
									}
									else
										Locale.GROUP_NOPERM.send(commandSender, group.getName(), perm);
								}
								LeezPermissions.reload();
							}
							else
								Locale.GROUP_DOESNOTEXISTS.send(commandSender, args[2]);

						}
						else
							commandSender.sendMessage("§4/lperm §cgroup edit <Grade> <prefix|suffix|addperm|delperm> <nouvelle valeur>");

					}
					else
						displayHelp_Group(commandSender);
				}
				else
					Locale.NOPERM_SUBCOMMAND.send(commandSender);
			}
			else if (args[0].equalsIgnoreCase("player")) {
				if (hasPerm(commandSender, "players")) {
					if (args.length >= 3 && args[1].equalsIgnoreCase("get")) {
						OfflinePlayer o_target = Bukkit.getOfflinePlayer(args[2]);
						if (o_target.hasPlayedBefore() || o_target.isOnline()) {
							final Group grp = Configs.getPlayerGroup(Bukkit.getPlayer(o_target.getUniqueId()));
							if (grp != null)
								Locale.PLAYER_HASGROUP.send(commandSender, o_target.getName(), grp.getName());
							else
								Locale.PLAYER_NOGROUP.send(commandSender, o_target.getName());
						}
						else
							Locale.PLAYER_NEVERPLAYED.send(commandSender, args[2]);
					}
					else if (args.length >= 4 && args[1].equalsIgnoreCase("set")) {
						OfflinePlayer o_target = Bukkit.getOfflinePlayer(args[2]);
						final Group from = Configs.getOfflinePlayerGroupOrDefault(o_target);
						if (o_target.hasPlayedBefore() || o_target.isOnline()) {
							final Group group = Configs.getGroup(args[3]);
							if (group != null) {
								if (commandSender instanceof Player) {
									final int playerPower = Configs.getOfflinePlayerGroupOrDefault((Player) commandSender).getPower();
									if (from.getPower() < playerPower) {
										if (group.getPower() < playerPower) {
											if (!from.getName().equals(group.getName())) {
												Configs.setGroupFromCS(o_target, group, commandSender);
												Locale.PLAYER_CHANGEDGROUP.send(commandSender, o_target.getName(), group.getName());
											}
											else
												Locale.PLAYER_ALRDHAS.send(commandSender, o_target.getName(), group.getName());
										}
										else {
											if (group.getPower() == playerPower)
												Locale.GROUP_SAMEPOWER.send(commandSender, group.getName());
											else
												Locale.GROUP_GREATERPOWER.send(commandSender, group.getName());
										}
									}
									else
										Locale.PLAYER_CANTCHANGE.send(commandSender, o_target.getName());
								}
								else {
									if (!from.getName().equals(group.getName())) {
										Configs.setGroupFromCS(o_target, group, commandSender);
										Locale.PLAYER_CHANGEDGROUP.send(commandSender, o_target.getName(), group.getName());
									}
									else
										Locale.PLAYER_ALRDHAS.send(commandSender, o_target.getName(), group.getName());
								}
							}
							else
								Locale.GROUP_DOESNOTEXISTS.send(commandSender, args[3]);
						}
						else
							Locale.PLAYER_NEVERPLAYED.send(commandSender, args[2]);
					}
					else
						displayHelp_Player(commandSender);
				}
				else
					Locale.NOPERM_SUBCOMMAND.send(commandSender);
			}
			else
				displayHelp(commandSender);

		}
		else
			displayHelp(commandSender);
	}

	private void displayHelp(CommandSender target) {
		target.sendMessage(LeezCore.PREFIX + "§3Aide pour la commande /leezperm§3:");

		displayHelp_Server(target);
		displayHelp_Group(target);
		displayHelp_Player(target);
	}

	private void displayHelp_Server(CommandSender target) {
		target.sendMessage("§9Commandes serveur:");
		target.sendMessage("§4/lperm §creload §8- §cRecharger la configuration");
	}

	private void displayHelp_Group(CommandSender target) {
		target.sendMessage("§9Commandes liées aux grades:");
		target.sendMessage("§4/lperm §cgroup list §8- §cVoir la liste des grades");
		target.sendMessage("§4/lperm §cgroup <add|del> <Grade> §8- §cCréer ou supprimer un grade en jeu");
		target.sendMessage("§4/lperm §cgroup edit <Grade> <prefix|suffix|addperm|delperm> <nouvelle valeur> §8- §cModifier un grade en jeu");
		target.sendMessage("§4/lperm §cgroup permissions <Grade> §8- §cVoir les permissions d'un grade");
	}

	private void displayHelp_Player(CommandSender target) {
		target.sendMessage("§9Commandes liées aux joueurs:");
		target.sendMessage("§4/lperm §cplayer set <Joueur> <Grade> §8- §cDéfinir le grade d'un joueur");
		target.sendMessage("§4/lperm §cplayer get <Joueur> §8- §cVoir quel grade possède un joueur");
	}

	private boolean hasPerm(CommandSender sender, String perm) {
		if (!(sender instanceof Player))
			return true;
		String plus = perm == null ? "" : "." + perm;
		return sender.hasPermission("leezpermissions.command" + plus);
	}

}
