package lz.izmoqwy.leezisland.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lz.izmoqwy.core.api.database.exceptions.SQLActionImpossibleException;
import lz.izmoqwy.core.self.CorePrinter;
import lz.izmoqwy.core.utils.LocationUtil;
import lz.izmoqwy.core.utils.ServerUtil;
import lz.izmoqwy.core.utils.TextUtil;
import lz.izmoqwy.core.world.Cuboid;
import lz.izmoqwy.leezisland.BorderAPI;
import lz.izmoqwy.leezisland.LeezIsland;
import lz.izmoqwy.leezisland.Locale;
import lz.izmoqwy.leezisland.Storage;
import lz.izmoqwy.leezisland.grid.CoopsManager;
import lz.izmoqwy.leezisland.grid.IslandManager;
import lz.izmoqwy.leezisland.grid.IslandPreset;
import lz.izmoqwy.leezisland.gui.IslandSettingsMenuGUI;
import lz.izmoqwy.leezisland.island.Island;
import lz.izmoqwy.leezisland.island.IslandMember;
import lz.izmoqwy.leezisland.island.IslandRole;
import lz.izmoqwy.leezisland.island.LevelCalculator;
import lz.izmoqwy.leezisland.players.SkyblockPlayer;
import lz.izmoqwy.leezisland.players.Wrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

public class PlayerCommand implements CommandExecutor {

	public static List<UUID> teamChat = Lists.newArrayList();

	private Map<Player, Entry<PlayerCommandAction, Player>> confirms = Maps.newHashMap();

	private void sendQueue(final Player player, final Player from, final PlayerCommandAction action, final int expires) {
		final Entry<PlayerCommandAction, Player> value = Maps.immutableEntry(action, from);
		confirms.put(player, value);
		new BukkitRunnable() {

			@Override
			public void run() {
				if (confirms.containsKey(player)) {
					if (confirms.get(player).equals(value)) {
						confirms.remove(player);
					}
				}
			}

		}.runTaskLater(LeezIsland.getInstance(), expires);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("island")) {
			if (sender instanceof Player) {
				SkyblockPlayer player = Wrapper.wrapPlayer((Player) sender);
				if (player == null) {
					sender.sendMessage(Locale.PREFIX + "§4Il semblerait qu'un problème survienne lors de l'éxécution de toute commande skyblock pour vous, contactez Izmoqwy.");
					return false;
				}

				if (args.length >= 1 && args[0].equalsIgnoreCase("confirm")) {
					if (confirms.containsKey(player.bukkit())) {
						Locale.ACTION_CONFIRM_DONE.send(player);

						Entry<PlayerCommandAction, Player> entry = confirms.get(player.bukkit());
						if (entry.getValue() == null || entry.getValue().isOnline()) {
							entry.getKey().run(entry.getValue() != null ? Wrapper.wrapPlayer(entry.getValue()) : null, player);
						}
						else
							Locale.ACTION_SENDER_DISCONNECTED.send(player);
						confirms.remove(player.bukkit());
					}
					else
						Locale.ACTION_CONFIRM_NONE.send(player);
				}
				else if (args.length >= 1 && args[0].equalsIgnoreCase("cancel")) {
					if (confirms.containsKey(player.bukkit())) {
						confirms.remove(player.bukkit());
						Locale.ACTION_CANCEL_DONE.send(player);
					}
					else
						Locale.ACTION_CANCEL_NONE.send(player);
				}

				/*
					Global
				 */
				else if (args.length >= 1 && args[0].equalsIgnoreCase("create")) {
					if (!player.hasIsland()) {
						IslandManager.createNewIsland(player.bukkit(), IslandPreset.DEFAULT, false);
					}
					else
						Locale.PLAYER_ISLAND_CREATE_ALREADYHAS.send(player);
				}

				else if (args.length >= 1 && args[0].equalsIgnoreCase("info")) {
					if (args.length >= 2) {
						OfflinePlayer off = Bukkit.getOfflinePlayer(args[1]);
						if (off.isOnline() || off.hasPlayedBefore()) {
							Island island = Wrapper.wrapOffPlayerIsland(off);
							if (island != null) {
								AdminCommand.sendIslandInfo(island, sender);
							}
							else
								Locale.TARGET_NOISLAND.send(player);
						}
						else
							Locale.TARGET_NOTEXISTS.send(player);
						return true;
					}
					if (player.hasIsland()) {
						Island island = player.getIsland();
						AdminCommand.sendIslandInfo(island, sender);
					}
					else
						Locale.PLAYER_ISLAND_NONE.send(player);
				}
				else if (args.length >= 1 && args[0].equalsIgnoreCase("level")) {
					// Todo: When really need to calc
					Island island = player.getIsland();
					final int originalLevel = island.getLevel();

					Locale.PLAYER_ISLAND_LEVEL_STARTING.send(player);
					Bukkit.getScheduler().runTask(LeezIsland.getInstance(), () -> {
						Cuboid cuboid = new Cuboid(island.getLowerNE(), island.getUpperSW());
						long neededXp = LevelCalculator.update(island, LevelCalculator.calcXP(cuboid.getChunks()));

						if (player.bukkit().isOnline()) {
							int level = island.getLevel();
							String indication = level == originalLevel ? Locale.PLAYER_ISLAND_LEVEL_INDICATION_SAME.toString() :
									level > originalLevel ? Locale.PLAYER_ISLAND_LEVEL_INDICATION_INCREASED.toString() :
											Locale.PLAYER_ISLAND_LEVEL_INDICATION_DECREASED.toString();
							Locale.PLAYER_ISLAND_LEVEL_FINISHED.send(player, level, indication, neededXp);
							// Locale.PLAYER_ISLAND_LEVEL_RANK.send(player, LevelCalculator.getPosition(island));
						}
						else
							CorePrinter.print("A player requested to calc his island level and quit before received the final message.");
					});
				}

				/*
					Island
				 */
				else if (args.length >= 1 && args[0].equalsIgnoreCase("sethome")) { // Role ✅
					if (player.hasIsland()) {
						Island island = player.getIsland();
						if (island.hasRoleOrAbove(player, IslandRole.OFFICIER)) {
							Location location = LocationUtil.getSafeLocation(player.bukkit().getLocation());
							if (location == null || !island.isInBounds(location.getBlockX(), location.getBlockZ())) {
								Locale.PLAYER_ISLAND_NOTONISLAND.send(player);
								return true;
							}
							island.setHome(location);
							player.sendMessage("§aLe home principal de l'île à été changé !");
						}
						else {
							player.sendMessage("§cVous devez être chef ou officier pour définir le home de l'île. Pour changer uniquement §nvotre §cpoint de téléporation, faîtes '/is personalhome'.");
						}
					}
					else
						Locale.PLAYER_ISLAND_NONE.send(player);
				}
				else if (args.length >= 1 && args[0].equalsIgnoreCase("personalhome")) {
					if (player.hasIsland()) {
						Island island = player.getIsland();
						Location location = LocationUtil.getSafeLocation(player.bukkit().getLocation());
						if (location == null || !island.isInBounds(location.getBlockX(), location.getBlockZ())) {
							player.sendMessage("§cVous n'êtes pas sur votre île !");
							return true;
						}
						player.setPersonalHome(location);
						player.sendMessage("§aVotre home d'île personnel vient d'être modifié.");
					}
					else
						Locale.PLAYER_ISLAND_NONE.send(player);
				}
				else if (args.length >= 1 && args[0].equalsIgnoreCase("lock")) { // Role ✅
					if (player.hasIsland()) {
						Island island = player.getIsland();
						if (island.hasRoleOrAbove(player, IslandRole.OFFICIER)) {
							island.setLocked(!island.isLocked());
							if (island.isLocked()) {
								int kicked = IslandManager.expelPlayers(island);
								if (kicked > 0)
									player.sendMessage("§aVotre île est désormais fermée aux joueurs. §7(§8" + kicked + " §7joueurs expulsés)");
								else
									player.sendMessage("§aVotre île est désormais fermée aux joueurs.");
							}
							else {
								player.sendMessage("§2Votre île n'est plus fermée aux joueurs.");
							}
						}
						else
							Locale.PLAYER_ISLAND_RANK_TOOLOW.send(player, IslandRole.OFFICIER);
					}
					else
						Locale.PLAYER_ISLAND_NONE.send(player);
				}
				else if (args.length >= 1 && args[0].equalsIgnoreCase("settings")) { // Role ✅
					if (player.hasIsland()) {
						Island island = player.getIsland();
						if (island.hasRoleOrAbove(player, IslandRole.OFFICIER)) {
							IslandSettingsMenuGUI.INSTANCE.open(player.bukkit());
						}
						else
							Locale.PLAYER_ISLAND_RANK_TOOLOW.send(player, IslandRole.OFFICIER);
					}
					else
						Locale.PLAYER_ISLAND_NONE.send(player);
				}
				else if (args.length >= 1 && args[0].equalsIgnoreCase("setname")) { // Permission & Role ✅
					if (sender.hasPermission("leezisland.command.setname")) {
						if (player.hasIsland()) {
							Island island = player.getIsland();
							if (island.hasRoleOrAbove(player, IslandRole.OFFICIER)) {
								if (args.length >= 2) {
									final String finalArg = ChatColor.translateAlternateColorCodes('&', TextUtil.getFinalArg(args, 1));
									if (finalArg.length() <= 24) {
										if (ChatColor.stripColor(finalArg).length() < 3) {
											player.sendMessage("§cLe nom de votre île doit faire au minimum 3 caractères.");
											return true;
										}

										if (!finalArg.contains(ChatColor.COLOR_CHAR + "") || sender.hasPermission("leezisland.name.color")) {
											try {
												if (island.getName() != null && island.getName().equals(finalArg)) {
													player.sendMessage("§2Votre île possède déjà ce nom");
												}
												else {
													if ((island.getName() != null && !island.getName().equalsIgnoreCase(finalArg)) && Storage.ISLANDS.hasResult("island_id", "name", finalArg)) {
														player.sendMessage("§cCe nom d'île est déjà prit !");
														return true;
													}
													IslandManager.setName(island, finalArg);
													player.sendMessage("§aLe nom de votre île est désormais §2" + island.getName() + "§a.");
												}
											}
											catch (SQLActionImpossibleException e) {
												e.printStackTrace();
												Locale.COMMAND_ERROR.send(player);
											}
										}
										else
											player.sendMessage("§cVous n'avez pas la permission de mettre des couleurs dans votre nom d'île.");
									}
									else
										player.sendMessage("§cLe nom de votre île ne peut pas dépasser 24 caractères.");
								}
								else
									Locale.COMMAND_ARGUMENTS_TOOFEW.send(player, "nom d'île");
							}
							else
								Locale.PLAYER_ISLAND_RANK_TOOLOW.send(player, IslandRole.OFFICIER);

						}
						else
							Locale.PLAYER_ISLAND_NONE.send(player);
					}
					else
						Locale.COMMAND_NOPERM.send(player);
				}

				/*
				 * TEAMS
				 */
				else if (args.length >= 1 && args[0].equalsIgnoreCase("team")) {
					if (player.hasIsland()) {
						Island island = player.getIsland();
						if (island.getMembersMap().size() > 0) {
							final Map<UUID, IslandMember> members = island.getMembersMap();
							if (!player.isOwner()) {
								members.remove(player.getBaseId());
								members.put(island.getOwner().getUniqueId(), new IslandMember(island.getOwner().getUniqueId(), IslandRole.OWNER));
							}

							sender.sendMessage(Locale.PREFIX + "§6Membres de votre île §7(" + island.getMembersMap().size() + ")§6:");
							island.getMembersMap().values().forEach(member -> sender.sendMessage("§8- §e" + Bukkit.getOfflinePlayer(member.getUniqueId()).getName() + " §7(" + member.getRole().name + ")"));
						}
						else {
							player.sendMessage("§6Vous êtes seul dans votre île. Pourquoi pas inviter quelques de vos amis à jouer avec vous ?");
						}
					}
					else
						Locale.PLAYER_ISLAND_NONE.send(player);
				}
				else if (args.length >= 1 && c(args[0], "tc", "teamchat")) {
					if (player.hasIsland()) {
						UUID uuid = player.getBaseId();
						if (teamChat.contains(uuid)) {
							teamChat.remove(uuid);
							player.sendMessage("§2Vous parlez désormais dans le chat général.");
						}
						else {
							teamChat.add(uuid);
							player.sendMessage("§aVous parlez désormais dans le chat d'île.");
						}
					}
					else
						Locale.PLAYER_ISLAND_NONE.send(player);
				}
				else if (args.length >= 1 && c(args[0], "send", "chat")) {
					if (player.hasIsland()) {
						if (args.length >= 2) {
							IslandManager.sendToTeamChat(player, TextUtil.getFinalArg(args, 1));
						}
						else
							player.sendMessage("§cVeuillez spécifier un message.");
					}
					else
						Locale.PLAYER_ISLAND_NONE.send(player);
				}
				else if (args.length >= 1 && args[0].equalsIgnoreCase("invite")) { // Role & selftarget ✅
					if (player.hasIsland()) {
						if (player.getIsland().hasRoleOrAbove(player, IslandRole.RECRUTER)) {
							if (args.length >= 2) {
								OfflinePlayer off = Bukkit.getOfflinePlayer(args[1]);
								if (!off.getUniqueId().equals(player.getBaseId())) {
									if (off.isOnline()) {
										SkyblockPlayer trgt = Wrapper.wrapPlayer(off.getPlayer());
										if (trgt == null) {
											return false;
										}
										if (!trgt.hasIsland()) {
											sendQueue(trgt.bukkit(), player.bukkit(), (plyr, target) -> {
												if (plyr == null) return;

												if (target.hasIsland()) {
													Locale.PLAYER_ISLAND_CREATE_ALREADYHAS.send(player);
													return;
												}
												if (!plyr.hasIsland()) {
													target.sendMessage("§cLe joueur vous ayant invité n'est plus dans aucune île !");
													return;
												}

												IslandManager.addMember(plyr.getIsland(), target.bukkit());
												target = Wrapper.wrapPlayer(target.bukkit());
												if (target == null)
													return;

												if (!target.hasIsland()) {
													target.sendMessage("§cUne erreur est survenue, vous n'avez pas pu rejoindre l'île.");
													return;
												}

												if (target.getIsland().getName() != null)
													Locale.TEAM_JOIN_JOINED_NAMED.send(target, target.getIsland().getName());
												else
													Locale.TEAM_JOIN_JOINED_NONAME.send(target, target.getIsland().getOwner().getName());

												plyr.sendMessage("§aLe joueur §2" + target.bukkit().getName() + " §avient de rejoindre votre île via votre invitation.");
												IslandManager.broadcast(target.getIsland(), "§6" + target.bukkit().getName() + " §ea rejoint votre île sur l'invitation de §6" + plyr.bukkit().getName());
											}, 20 * 60 * 5);
											player.sendMessage("§aInvitation envoyée à §2" + trgt.bukkit().getName() + "§a.");
											trgt.sendMessage("§aVous avez reçu une invitation de la part de §2" + player.bukkit().getName() + "§a. Faîtes '/island confirm' pour accepter.");
										}
										else
											Locale.TARGET_HASISLAND.send(player);
									}
									else
										Locale.TARGET_DISCONNECTED.send(player);
								}
								else
									Locale.TARGET_YOUSELF.send(player);
							}
							else
								Locale.ARGUMENT_PLAYER.send(player);
						}
						else
							Locale.PLAYER_ISLAND_RANK_TOOLOW.send(player, IslandRole.RECRUTER);
					}
					else
						Locale.PLAYER_ISLAND_NONE.send(player);
				}
				else if (args.length >= 1 && args[0].equalsIgnoreCase("promote")) { // Role & target-role & selftarget ✅
					if (player.hasIsland()) {
						if (player.getIsland().hasRoleOrAbove(player, IslandRole.OFFICIER)) {
							if (args.length >= 2) {
								OfflinePlayer off = Bukkit.getOfflinePlayer(args[1]);
								if (!off.getUniqueId().equals(player.getBaseId())) {
									if (off.isOnline() || off.hasPlayedBefore()) {
										Island island = player.getIsland();
										if (island.getMembersMap().containsKey(off.getUniqueId())) {
											if (island.hasRoleOrAbove(off, island.getRole(player))) {
												Locale.TEAM_TARGET_SUPERIOR.send(player);
												return true;
											}

											IslandRole originalRole = island.getRole(off.getUniqueId());
											IslandRole newRole = null;
											switch (originalRole) {
												case OFFICIER:
													player.sendMessage("§cVous ne pouvez pas promouvoir un membre chef de l'île de cette façon. Si tel était votre but, faîtes '/is makeleader " + off.getName() + "'.");
													return true;
												case RECRUTER:
													if (island.isOwner(player)) {
														// Make target OFFICIER
														newRole = IslandRole.OFFICIER;
													}
													else {
														player.sendMessage("§cVous devez être chef pour assigner une personne au rang d'Officier.");
														return true;
													}
													break;
												case MEMBER:
													// Make target RECRUTER
													newRole = IslandRole.RECRUTER;
													break;
											}
											if (newRole != null) {
												island.setRole(off, newRole);
												player.sendMessage("§aVous avez élevé §2" + off.getName() + " §aau rôle §2" + newRole + "§a.");
												if (off.isOnline())
													Bukkit.getPlayer(off.getUniqueId()).sendMessage(Locale.PREFIX + "§3Vous avez élevé au rôle §b" + island.getRole(off.getUniqueId()) + "§3.");
											}
											else
												player.sendMessage("§cAucun rôle à attribuer ! Sachez que ce message ne devrait jamais être envoyé. Si tel est le cas, veuillez contactez Izmoqwy en expliquant comment vous avez obtenu ce message.");

										}
										else if (island.getOwner().getUniqueId().equals(off.getUniqueId()))
											Locale.TEAM_TARGET_LEADER.send(player);
										else
											Locale.TEAM_TARGET_NOTMEMBER.send(player);
									}
									else
										Locale.TARGET_NOTEXISTS.send(player);
								}
								else
									Locale.TARGET_YOUSELF.send(player);
							}
							else
								Locale.ARGUMENT_PLAYER.send(player);
						}
						else
							Locale.PLAYER_ISLAND_RANK_TOOLOW.send(player, IslandRole.OFFICIER);
					}
					else
						Locale.PLAYER_ISLAND_NONE.send(player);
				}
				else if (args.length >= 1 && args[0].equalsIgnoreCase("demote")) { // Role & target-role & selftarget ✅
					if (player.hasIsland()) {
						if (player.getIsland().hasRoleOrAbove(player, IslandRole.OFFICIER)) {
							if (args.length >= 2) {
								OfflinePlayer off = Bukkit.getOfflinePlayer(args[1]);
								if (!off.getUniqueId().equals(player.getBaseId())) {
									if (off.isOnline() || off.hasPlayedBefore()) {
										Island island = player.getIsland();
										if (island.getMembersMap().containsKey(off.getUniqueId())) {
											if (island.hasRoleOrAbove(off, island.getRole(player))) {
												Locale.TEAM_TARGET_SUPERIOR.send(player);
												return true;
											}

											IslandRole originalRole = island.getRole(off.getUniqueId());
											IslandRole newRole = null;
											switch (originalRole) {
												case OFFICIER:
													if (island.isOwner(player)) {
														// Make target RECRUTER
														newRole = IslandRole.RECRUTER;
													}
													else {
														player.sendMessage("§cVous devez être chef pour rétrograder un Officier.");
														return true;
													}
													break;
												case RECRUTER:
													// Make target MEMBER
													newRole = IslandRole.MEMBER;
													break;
											}
											if (newRole != null) {
												island.setRole(off, newRole);
												player.sendMessage("§aVous avez rétrogradé §2" + off.getName() + " §aau rôle §2" + newRole + "§a.");
												if (off.isOnline())
													Bukkit.getPlayer(off.getUniqueId()).sendMessage(Locale.PREFIX + "§3Vous avez rétrogradé au rôle §b" + island.getRole(off.getUniqueId()) + "§3.");
											}
											else
												player.sendMessage("§6Ce membre est déjà au plus bas des rôles.");
										}
										else if (island.getOwner().getUniqueId().equals(off.getUniqueId()))
											Locale.TEAM_TARGET_LEADER.send(player);
										else
											Locale.TEAM_TARGET_NOTMEMBER.send(player);
									}
									else
										Locale.TARGET_NOTEXISTS.send(player);
								}
								else
									Locale.TARGET_YOUSELF.send(player);
							}
							else
								Locale.ARGUMENT_PLAYER.send(player);
						}
						else
							Locale.PLAYER_ISLAND_RANK_TOOLOW.send(player, IslandRole.OFFICIER);
					}
					else
						Locale.PLAYER_ISLAND_NONE.send(player);
				}
				else if (args.length >= 1 && args[0].equalsIgnoreCase("kick")) { // Role & target-role & selftarget ✅
					if (player.hasIsland()) {
						if (player.getIsland().hasRoleOrAbove(player, IslandRole.RECRUTER)) {
							if (args.length >= 2) {
								OfflinePlayer off = Bukkit.getOfflinePlayer(args[1]);
								if (!off.getUniqueId().equals(player.getBaseId())) {
									if (off.isOnline() || off.hasPlayedBefore()) {
										Island island = player.getIsland();
										if (island.getMembersMap().containsKey(off.getUniqueId())) {
											if (island.hasRoleOrAbove(off, island.getRole(player))) {
												Locale.TEAM_TARGET_SUPERIOR.send(player);
												return true;
											}

											IslandManager.kickMember(island, off);
											sender.sendMessage(Locale.PREFIX + "§eVous avez retiré §6" + off.getName() + " §ede votre île.");

											if (off.isOnline())
												Bukkit.getPlayer(off.getUniqueId()).sendMessage(Locale.PREFIX + "§6Vous avez été retiré de votre île par §a" + sender.getName() + "§6.");
										}
										else if (island.getOwner().getUniqueId().equals(off.getUniqueId()))
											Locale.TEAM_TARGET_LEADER.send(player);
										else
											Locale.TEAM_TARGET_NOTMEMBER.send(player);
									}
									else
										Locale.TARGET_NOTEXISTS.send(player);
								}
								else
									Locale.TARGET_YOUSELF.send(player);
							}
							else
								Locale.ARGUMENT_PLAYER.send(player);
						}
						else
							Locale.PLAYER_ISLAND_RANK_TOOLOW.send(player, IslandRole.RECRUTER);
					}
					else
						Locale.PLAYER_ISLAND_NONE.send(player);
				}
				else if (args.length >= 1 && args[0].equalsIgnoreCase("leave")) {
					if (player.hasIsland()) {
						sendQueue(player.bukkit(), null, (plyr, target) -> {
							if (target == null) return;

							if (!target.hasIsland()) {
								target.sendMessage("§cVous n'avez plus d'île !");
								return;
							}

							IslandManager.kickMember(target.getIsland(), target.bukkit());
							target.sendMessage("§aVous avez quitté votre île. Repartez sur de bonnes bases en recréant une nouvelle île ou en rejoingnant celle d'un de vos amis.");
						}, 20 * 60);
						if (player.isOwner()) {
							if (player.getIsland().getMembersMap().size() > 0) {
								player.sendMessage("§cVous êtes le chef de cette île et il y a des membres présents! Supprimez chaque membre avant de refaire cette commande de nouveau.");
								return true;
							}
							player.sendMessage("§6Attention: Vous êtes le chef de votre île, elle va donc être supprimée si vous confirmez !");
						}
						player.sendMessage("§eVous êtes sur le point de quitter votre île. Faîtes '/island confirm' pour confirmer.");
					}
					else
						Locale.PLAYER_ISLAND_NONE.send(player);
				}
				/*
					Coops
				 */
				else if (args.length >= 1 && args[0].equalsIgnoreCase("coops")) {
					if (player.hasIsland()) {
						List<UUID> coopeds = CoopsManager.getCoops(player.getIsland().ID);
						if (coopeds != null && !coopeds.isEmpty()) {
							player.sendMessage("§6Coopérants §7(" + coopeds.size() + ")§6:");
							coopeds.forEach(member -> sender.sendMessage("§8- §e" + Bukkit.getOfflinePlayer(member).getName()));
						}
						else {
							player.sendMessage("§6Vous n'avez aucun coopérants.");
						}
					}
					else
						Locale.PLAYER_ISLAND_NONE.send(player);
				}
				else if (args.length >= 1 && args[0].equalsIgnoreCase("coop")) { // Role & target-role & selftarget ✅
					if (player.hasIsland()) {
						Island island = player.getIsland();
						if (island.hasRoleOrAbove(player, IslandRole.RECRUTER)) {
							if (args.length >= 2) {
								Player target = Bukkit.getPlayer(args[1]);
								if (target == null) {
									Locale.TARGET_NEORDISCONNECT.send(player);
									return true;
								}

								if (target.getUniqueId().equals(player.getBaseId())) {
									Locale.TARGET_YOUSELF.send(player);
									return true;
								}

								if (player.getIsland().hasFullAccess(target)) {
									Locale.TEAM_TARGET_ISMEMBER.send(player);
									return true;
								}

								SkyblockPlayer trgt = Wrapper.wrapPlayer(target.getPlayer());
								if (trgt == null) {
									return true;
								}
								if (trgt.hasIsland()) {
									if (!CoopsManager.isCooped(trgt.getBaseId(), island.ID)) {
										CoopsManager.coop(trgt.getBaseId(), island.ID, player.bukkit());
										player.sendMessage("§aVous avez invité §2" + target.getName() + " §aa vous aider.");
										trgt.sendMessage("§eVous avez été invité à aider l'île " + island.getDisplayName() + "§e.");
									}
									else
										player.sendMessage("§6Ce joueur est déjà un de vos coopérants. Faîte '/is coops' pour en voir la liste.");
								}
								else
									player.sendMessage("§cCe joueur n'a pas d'île, vous ne pouvez donc pas l'inviter en temps que coopérant. Dîtes-lui d'en créer une ou de rejoindre la votre !");
							}
							else
								Locale.ARGUMENT_PLAYER.send(player);
						}
						else
							Locale.PLAYER_ISLAND_RANK_TOOLOW.send(player, IslandRole.RECRUTER);
					}
					else
						Locale.PLAYER_ISLAND_NONE.send(player);
				}
				else if (args.length >= 1 && args[0].equalsIgnoreCase("uncoop")) { // Role & target-role & selftarget ✅
					if (player.hasIsland()) {
						Island island = player.getIsland();
						if (island.hasRoleOrAbove(player, IslandRole.RECRUTER)) {
							if (args.length >= 2) {
								OfflinePlayer off = Bukkit.getOfflinePlayer(args[1]);
								if (!off.hasPlayedBefore() && !off.isOnline()) {
									Locale.TARGET_NOTEXISTS.send(player);
									return true;
								}

								if (off.getUniqueId().equals(player.getBaseId())) {
									Locale.TARGET_YOUSELF.send(player);
									return true;
								}

								if (player.getIsland().hasFullAccess(off)) {
									Locale.TEAM_TARGET_ISMEMBER.send(player);
									return true;
								}

								if (off.isOnline() && CoopsManager.isCooped(off.getUniqueId(), island.ID)) {
									CoopsManager.unCoop(off.getUniqueId(), island.ID, true);
									player.sendMessage("§2Vous avez retiré §6" + off.getName() + " §2de vos coopérants.");
									Bukkit.getPlayer(off.getUniqueId()).sendMessage(Locale.PREFIX + "§6Vous n'êtes plus coopérant de l'île " + island.getDisplayName() + "§6.");
								}
								else
									player.sendMessage("§6Ce joueur n'est pas un de vos coopérants, vous pouvez dormir sur vos deux oreilles.");
							}
							else
								Locale.ARGUMENT_PLAYER.send(player);
						}
						else
							Locale.PLAYER_ISLAND_RANK_TOOLOW.send(player, IslandRole.RECRUTER);
					}
					else
						Locale.PLAYER_ISLAND_NONE.send(player);
				}

				/*
					Visitors
				 */
				else if (args.length >= 1 && args[0].equalsIgnoreCase("banlist")) {
					if (player.hasIsland()) {
						List<UUID> bannedList = player.getIsland().getBanneds();
						if (bannedList != null && !bannedList.isEmpty()) {
							player.sendMessage("§6Bannis §7(" + bannedList.size() + ")§6:");
							bannedList.forEach(member -> sender.sendMessage("§8- §e" + Bukkit.getOfflinePlayer(member).getName()));
						}
						else {
							player.sendMessage("§6Aucun joueur n'est banni de votre île.");
						}
					}
					else
						Locale.PLAYER_ISLAND_NONE.send(player);
				}
				else if (args.length >= 1 && args[0].equalsIgnoreCase("ban")) { // Role & target-role & selftarget ✅
					if (player.hasIsland()) {
						Island island = player.getIsland();
						if (island.hasRoleOrAbove(player, IslandRole.RECRUTER)) {
							if (args.length >= 2) {
								OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
								if (!target.hasPlayedBefore() && !target.isOnline()) {
									Locale.TARGET_NOTEXISTS.send(player);
									return true;
								}

								if (target.getUniqueId().equals(player.getBaseId())) {
									Locale.TARGET_YOUSELF.send(player);
									return true;
								}

								if (target.isOnline() && Bukkit.getPlayer(target.getUniqueId()).hasPermission("leezsky.ban.exempt")) {
									// Todo: Check permission even on offline player
									Locale.BAN_TARGET_ISADMIN.send(player);
									return true;
								}

								if (player.getIsland().hasFullAccess(target)) {
									Locale.TEAM_TARGET_ISMEMBER.send(player);
									return true;
								}

								if (IslandManager.banPlayer(island, target.getUniqueId())) {
									player.sendMessage("§aVous avez banni §2" + target.getName() + " §ade votre île.");
									if (target.isOnline()) {
										Player onlineTarget = Bukkit.getPlayer(target.getUniqueId());
										onlineTarget.sendMessage(Locale.PREFIX + "§eVous avez été banni de l'île §6" + island.getDisplayName() + "§e.");
										if (!AdminCommand.BYPASSING.contains(onlineTarget.getUniqueId()) && island.isInBounds(onlineTarget.getLocation()))
											ServerUtil.performCommand("spawn " + onlineTarget.getName());
									}
								}
								else
									player.sendMessage("§6Ce joueur est déjà banni de votre île. Faîtes '/is banlist' pour voir tous les joueurx bannis de votre île.");
							}
							else
								Locale.ARGUMENT_PLAYER.send(player);
						}
						else
							Locale.PLAYER_ISLAND_RANK_TOOLOW.send(player, IslandRole.RECRUTER);
					}
					else
						Locale.PLAYER_ISLAND_NONE.send(player);
				}
				else if (args.length >= 1 && args[0].equalsIgnoreCase("unban")) { // Role & target-role & selftarget ✅
					if (player.hasIsland()) {
						Island island = player.getIsland();
						if (island.hasRoleOrAbove(player, IslandRole.RECRUTER)) {
							if (args.length >= 2) {
								OfflinePlayer off = Bukkit.getOfflinePlayer(args[1]);
								if (!off.hasPlayedBefore() && !off.isOnline()) {
									Locale.TARGET_NOTEXISTS.send(player);
									return true;
								}

								if (off.getUniqueId().equals(player.getBaseId())) {
									Locale.TARGET_YOUSELF.send(player);
									return true;
								}

								if (player.getIsland().hasFullAccess(off)) {
									Locale.TEAM_TARGET_ISMEMBER.send(player);
									return true;
								}

								if (island.getBanneds().contains(off.getUniqueId())) {
									IslandManager.unban(island, off.getUniqueId());
									player.sendMessage("§2Vous avez dé-banni §a" + off.getName() + " §2de votre île.");
									if (off.isOnline())
										Bukkit.getPlayer(off.getUniqueId()).sendMessage(Locale.PREFIX + "§eVous avez été dé-banni de l'île §6" + island.getDisplayName() + "§e.");
								}
								else
									player.sendMessage("§6Ce joueur n'est pas banni de votre île.");
							}
							else
								Locale.ARGUMENT_PLAYER.send(player);
						}
						else
							Locale.PLAYER_ISLAND_RANK_TOOLOW.send(player, IslandRole.RECRUTER);
					}
					else
						Locale.PLAYER_ISLAND_NONE.send(player);
				}
				else if (args.length >= 1 && args[0].equalsIgnoreCase("expel")) { // target-role & selftarget ✅
					if (args.length >= 2) {
						Player target = Bukkit.getPlayer(args[1]);
						if (target == null) {
							Locale.TARGET_NEORDISCONNECT.send(player);
							return true;
						}

						if (!target.getUniqueId().equals(player.getBaseId())) {
							if (player.getIsland().isInBounds(target.getLocation())) {
								if (player.getIsland().hasFullAccess(target)) {
									Locale.TEAM_TARGET_ISMEMBER.send(player);
									return true;
								}

								if (AdminCommand.BYPASSING.contains(target.getUniqueId())) {
									// Todo: Check if vanished and tell the player the admin is not connected
									Locale.TARGET_BYPASSING.send(player);
									return true;
								}

								ServerUtil.performCommand("spawn " + target.getName());

								sender.sendMessage(Locale.PREFIX + "§eVous avez expulsé §6" + target.getName() + " §ede votre île.");
								target.sendMessage(Locale.PREFIX + "§eVous avez été explusé de l'île de §e" + sender.getName() + "§e.");
							}
							else
								Locale.TARGET_NOTONMYISLAND.send(player);
						}
						else
							Locale.TARGET_YOUSELF.send(player);
					}
					else
						Locale.PLAYER_ISLAND_NONE.send(player);
				}

				/*
					Help messsage
				 */
				else if (args.length >= 1 && c(args[0], "help", "?")) {

					if (args.length >= 2 && c(args[1], "all", "tout")) {
						sender.sendMessage(" ");

						sender.sendMessage(Locale.PREFIX + "§3Aide de la commande /island:");
						sendHelp(sender, HelpGroup.GLOBAL);
						sender.sendMessage(" ");

						sendHelp(sender, HelpGroup.ISLAND);
						sender.sendMessage(" ");

						sendHelp(sender, HelpGroup.ISLAND_2);
						sender.sendMessage(" ");

						sendHelp(sender, HelpGroup.TEAM);
						sender.sendMessage(" ");

						sendHelp(sender, HelpGroup.COOPS);
						sender.sendMessage(" ");

						sendHelp(sender, HelpGroup.VISITORS);

						sender.sendMessage(" ");
					}
					else if (args.length >= 2 && c(args[1], "global", "general", "général")) {
						sender.sendMessage(" ");

						sender.sendMessage(Locale.PREFIX + "§3Aide de la commande /island §7(A propos des généralités):");
						sendHelp(sender, HelpGroup.GLOBAL);
						sender.sendMessage(" ");
					}
					else if (args.length >= 2 && c(args[1], "island", "is")) {
						sender.sendMessage(" ");

						sender.sendMessage(Locale.PREFIX + "§3Aide de la commande /island §7(A propos des îles):");
						sendHelp(sender, HelpGroup.ISLAND);
						sender.sendMessage(" ");

						sendHelp(sender, HelpGroup.ISLAND_2);

						sender.sendMessage(" ");
					}
					else if (args.length >= 2 && c(args[1], "team", "equipe")) {
						sender.sendMessage(" ");

						sender.sendMessage(Locale.PREFIX + "§3Aide de la commande /island §7(A propos des équipes):");
						sendHelp(sender, HelpGroup.TEAM);

						sender.sendMessage(" ");
					}
					else if (args.length >= 2 && c(args[1], "coop", "coops")) {
						sender.sendMessage(" ");

						sender.sendMessage(Locale.PREFIX + "§3Aide de la commande /island §7(A propos des coopérants):");
						sendHelp(sender, HelpGroup.COOPS);

						sender.sendMessage(" ");
					}
					else if (args.length >= 2 && c(args[1], "visitor", "visitors")) {
						sender.sendMessage(" ");

						sender.sendMessage(Locale.PREFIX + "§3Aide de la commande /island §7(A propos des visiteurs):");
						sendHelp(sender, HelpGroup.VISITORS);

						sender.sendMessage(" ");
					}
					else
						player.sendMessage("§6Utilisation: /island help <all|global|island|team|coops|visitors>");
				}
				else if (args.length == 0 || c(args[0], "go", "tp")) {
					if (player.hasIsland()) {
						if (player.hasPersonalHome() && !(args.length >= 2 && args[1].equalsIgnoreCase("main")))
							player.bukkit().teleport(player.getPersonalHome());
						else
							player.bukkit().teleport(player.getIsland().getHome());
						BorderAPI.setOwnBorder(player);

						Locale.PLAYER_ISLAND_TELEPORT.send(player);
					}
					else
						Locale.PLAYER_ISLAND_NONE.send(player);
				}
				else {
					// Should not be reached
					Locale.COMMAND_UNKOWN.send(player);
				}
				return true;
			}
			else
				Locale.COMMAND_PLAYERONLY.send(sender);
		}
		return false;
	}

	private void sendHelp(CommandSender sender, HelpGroup helpGroup) {
		switch (helpGroup) {
			case GLOBAL:
				/*
					Global
				 */
				sender.sendMessage("§6/island §ecreate §8- §eCréer une île"); // DONE
				sender.sendMessage("§6/island §ego [main]§8- §eSe téléporter à votre île"); // DONE
				sender.sendMessage("§6/island §elevel §8- §eCalculer le niveau de votre île"); // TODO: limit usages and when to actually calc
				sender.sendMessage("§6/island §einfo (Pseudo) §8- §eAfficher toutes les informations d'une île"); // DONE
				sender.sendMessage("§6/island §ewarp <Ile> §8- §eSe téléporter à une île");
				sender.sendMessage("§6/island §ewarps §8- §eLister tous les îles avec un point de téléportation");
				break;
			case ISLAND:
				/*
					Island
				 */
				sender.sendMessage("§6/island §esethome §8- §eChanger le home principal de l'île"); // DONE
				sender.sendMessage("§6/island §epersonalhome §8- §eChanger votre point de téléporation"); // DONE
				sender.sendMessage("§6/island §esetwarp §8- §eDéfinir le warp de votre île");
				sender.sendMessage("§6/island §edelwarp §8- §eDéfinir le warp de votre île");
				sender.sendMessage("§6/island §esetname <Nom> §8- §eChanger le nom de votre île"); // DONE
				sender.sendMessage("§6/island §elock §8- §eFermer ou ouvrir votre île au public"); // DONE
				break;
			case ISLAND_2:
				/*
					Island - 2nd
				 */
				sender.sendMessage("§6/island §esettings §8- §eConfigurer les paramètres de votre île"); // DONE
				sender.sendMessage("§6/island §eshop §8- §eOuvrir le shop d'île (améliorations, etc)");
				sender.sendMessage("§6/island §evault §8- §eOuvrir le coffre d'île");
				break;
			case TEAM:
				/*
					Team
				 */
				sender.sendMessage("§6/island §eteam §8- §eVoir les membres dans votre île"); // DONE
				sender.sendMessage("§6/island §eteamchat §8- §eActiver le chat d'île"); // DONE
				sender.sendMessage("§6/island §esend §8- §eEnvoyer un message aux membres de l'île"); // DONE
				sender.sendMessage("§6/island §einvite <Joueur> §8- §eInviter une personne dans votre île"); // DONE
				sender.sendMessage("§6/island §epromote <Joueur> §8- §ePromouvoir un membre au rôle supérieur");
				sender.sendMessage("§6/island §edemote <Joueur> §8- §eRétrograder un membre au rôle inférieur");
				sender.sendMessage("§6/island §ekick <Joueur> §8- §eExpulser un membre de votre équipe"); // DONE
				sender.sendMessage("§6/island §eleave §8- §eQuitter votre île"); // TODO: manage things with lastrestart
				break;
			case COOPS:
				/*
					Coops
				 */
				sender.sendMessage("§6/island §ecoops §8- §eVoir la liste de vos coopérants temporaires"); // DONE
				sender.sendMessage("§6/island §ecoop <Joueur> §8- §eInviter temporairement un joueur à vous aider"); // DONE
				sender.sendMessage("§6/island §euncoop <Joueur> §8- §eSupprimer un coopérant"); // DONE
				break;
			case VISITORS:
				/*
					Visistors
				 */
				sender.sendMessage("§6/island §ebanlist §8- §eVoir les joueurs bannis de votre île"); // DONE
				sender.sendMessage("§6/island §eban <Joueur> §8- §eRestreindre l'accès à un joueur de votre île"); // DONE
				sender.sendMessage("§6/island §eunban <Joueur> §8- §eDé-bannir un joueur de votre île"); // DONE
				sender.sendMessage("§6/island §eexpel <Joueur> §8- §eFaire partir une personne trainant sur votre île"); // DONE
				break;
		}
	}

	protected static boolean c(String arg, String... possibilites) {
		for (String s : possibilites) {
			if (arg.equalsIgnoreCase(s))
				return true;
		}
		return false;
	}

	private enum HelpGroup {
		GLOBAL, ISLAND, ISLAND_2, TEAM, COOPS, VISITORS
	}

	private interface PlayerCommandAction {

		void run(SkyblockPlayer sender, SkyblockPlayer executor);

	}

}
