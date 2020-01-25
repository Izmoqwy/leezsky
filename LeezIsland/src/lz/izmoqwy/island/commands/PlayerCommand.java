package lz.izmoqwy.island.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lz.izmoqwy.core.api.database.exceptions.SQLActionImpossibleException;
import lz.izmoqwy.core.command.CommandOptions;
import lz.izmoqwy.core.command.CoreCommand;
import lz.izmoqwy.core.self.CorePrinter;
import lz.izmoqwy.core.utils.LocationUtil;
import lz.izmoqwy.core.utils.ServerUtil;
import lz.izmoqwy.core.utils.TextUtil;
import lz.izmoqwy.core.world.Cuboid;
import lz.izmoqwy.island.BorderAPI;
import lz.izmoqwy.island.LeezIsland;
import lz.izmoqwy.island.Locale;
import lz.izmoqwy.island.Storage;
import lz.izmoqwy.island.grid.CoopsManager;
import lz.izmoqwy.island.grid.IslandManager;
import lz.izmoqwy.island.grid.IslandPreset;
import lz.izmoqwy.island.gui.IslandSettingsMenuGUI;
import lz.izmoqwy.island.island.Island;
import lz.izmoqwy.island.island.IslandMember;
import lz.izmoqwy.island.island.IslandRole;
import lz.izmoqwy.island.island.LevelCalculator;
import lz.izmoqwy.island.players.SkyblockPlayer;
import lz.izmoqwy.island.players.Wrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

public class PlayerCommand extends CoreCommand {

	public static List<UUID> teamChat = Lists.newArrayList();

	private Map<Player, Entry<PlayerCommandAction, Player>> confirms = Maps.newHashMap();

	public PlayerCommand() {
		super("island", CommandOptions.builder()
				.playerOnly(true)
				.build());
	}

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
	protected void execute(CommandSender sender, String usedCommand, String[] args) {
		SkyblockPlayer player = Wrapper.wrapPlayer((Player) sender);
		if (player == null) {
			sender.sendMessage(Locale.PREFIX + "§4Il semblerait qu'un problème survienne lors de l'éxécution de toute commande skyblock pour vous, contactez Izmoqwy.");
			return;
		}

		if (match(args, 0, "confirm")) {
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
		else if (match(args, 0, "cancel")) {
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
		else if (match(args, 0, "create")) {
			if (!player.hasIsland()) {
				IslandManager.createNewIsland(player.bukkit(), IslandPreset.DEFAULT, false);
			}
			else
				Locale.PLAYER_ISLAND_CREATE_ALREADYHAS.send(player);
		}
		else if (match(args, 0, "info")) {
			if (args.length >= 2) {
				OfflinePlayer off = Bukkit.getOfflinePlayer(args[1]);
				if (off.isOnline() || off.hasPlayedBefore()) {
					Island island = Wrapper.getOfflinePlayerIsland(off);
					if (island != null) {
						AdminCommand.sendIslandInfo(island, sender);
					}
					else
						Locale.TARGET_NOISLAND.send(player);
				}
				else
					Locale.TARGET_NOTEXISTS.send(player);
				return;
			}
			if (player.hasIsland()) {
				Island island = player.getIsland();
				AdminCommand.sendIslandInfo(island, sender);
			}
			else
				Locale.PLAYER_ISLAND_NONE.send(player);
		}
		else if (match(args, 0, "level")) {
			// Todo: When really need to calc
			Island island = player.getIsland();
			final int originalLevel = island.getLevel();

			Locale.PLAYER_ISLAND_LEVEL_STARTING.send(player);
			Bukkit.getScheduler().runTask(LeezIsland.getInstance(), () -> {
				Cuboid cuboid = new Cuboid(island.getLowerNE(), island.getUpperSW());
				long neededXp = LevelCalculator.update(island, LevelCalculator.calculateExperience(cuboid.getChunks()));

				// todo : broadcast
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
		else if (match(args, 0, "sethome")) {
			if (player.hasIsland()) {
				Island island = player.getIsland();
				if (island.hasRoleOrAbove(player, IslandRole.OFFICIER)) {
					Location location = LocationUtil.getSafeLocation(player.bukkit().getLocation());
					if (location == null || !island.isInBounds(location.getBlockX(), location.getBlockZ())) {
						Locale.PLAYER_ISLAND_NOTONISLAND.send(player);
						return;
					}
					island.setHomeLocation(location);
					player.sendMessage("§aLe home principal de l'île à été changé !");
				}
				else {
					player.sendMessage("§cVous devez être chef ou officier pour définir le home de l'île. Pour changer uniquement §nvotre §cpoint de téléportation, faîtes '/is personalhome'.");
				}
			}
			else
				Locale.PLAYER_ISLAND_NONE.send(player);
		}
		else if (match(args, 0, "personalhome")) {
			if (player.hasIsland()) {
				Island island = player.getIsland();
				Location location = LocationUtil.getSafeLocation(player.bukkit().getLocation());
				if (location == null || !island.isInBounds(location.getBlockX(), location.getBlockZ())) {
					player.sendMessage("§cVous n'êtes pas sur votre île !");
					return;
				}
				player.setPersonalHome(location);
				player.sendMessage("§aVotre home d'île personnel vient d'être modifié.");
			}
			else
				Locale.PLAYER_ISLAND_NONE.send(player);
		}
		else if (match(args, 0, "lock")) {
			if (player.hasIsland()) {
				Island island = player.getIsland();
				if (island.hasRoleOrAbove(player, IslandRole.OFFICIER)) {
					island.setLocked(!island.isLocked());
					if (island.isLocked()) {
						int kicked = IslandManager.expelEveryone(island);
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
		else if (match(args, 0, "settings")) {
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
		else if (match(args, 0, "setname")) {
			if (!sender.hasPermission("leezisland.command.setname")) {
				Locale.COMMAND_NOPERM.send(player);
				return;
			}

			if (!player.hasIsland()) {
				Locale.PLAYER_ISLAND_NONE.send(player);
				return;
			}

			Island island = player.getIsland();
			if (!island.hasRoleOrAbove(player, IslandRole.OFFICIER)) {
				Locale.PLAYER_ISLAND_RANK_TOOLOW.send(player, IslandRole.OFFICIER);
				return;
			}

			if (args.length < 2) {
				Locale.COMMAND_ARGUMENTS_TOOFEW.send(player, "nom d'île");
				return;
			}

			final String finalArg = ChatColor.translateAlternateColorCodes('&', TextUtil.getFinalArg(args, 1));
			if (finalArg.length() <= 24) {
				if (ChatColor.stripColor(finalArg).length() < 3) {
					player.sendMessage("§cLe nom de votre île doit faire au minimum 3 caractères.");
					return;
				}

				if (!finalArg.contains(Character.toString(ChatColor.COLOR_CHAR)) || sender.hasPermission("leezisland.name.color")) {
					try {
						if (island.getName() != null && island.getName().equals(finalArg)) {
							player.sendMessage("§2Votre île possède déjà ce nom");
						}
						else {
							if (island.getName() != null && !island.getName().equalsIgnoreCase(finalArg) && Storage.ISLANDS.hasResult("island_id", "name", finalArg)) {
								player.sendMessage("§cCe nom d'île est déjà prit !");
								return;
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

		/*
		 TEAMS
		 */
		else if (match(args, 0, "team")) {
			if (!player.hasIsland()) {
				Locale.PLAYER_ISLAND_NONE.send(player);
				return;
			}

			Island island = player.getIsland();
			if (island.getMembersMap().size() > 0) {
				final Map<UUID, IslandMember> members = island.getMembersMap();
				if (!player.isOwner()) {
					members.remove(player.getBaseId());
					members.put(island.getOwnerId(), new IslandMember(island.getOwnerId(), IslandRole.OWNER));
				}

				sender.sendMessage(Locale.PREFIX + "§6Membres de votre île §7(" + island.getMembersMap().size() + ")§6:");
				island.getMembersMap().values().forEach(member ->
						sender.sendMessage("§8- §e" + Bukkit.getOfflinePlayer(member.getPlayerId()).getName() + " §7(" + member.getRole().getName() + ")"));
			}
			else {
				player.sendMessage("§6Vous êtes seul dans votre île. Pourquoi pas inviter quelques de vos amis à jouer avec vous ?");
			}
		}
		else if (match(args, 0, "tc", "teamchat")) {
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
		else if (match(args, 0, "send", "chat")) {
			if (player.hasIsland()) {
				if (args.length >= 2) {
					player.getIsland().sendToTeam(player, TextUtil.getFinalArg(args, 1));
				}
				else
					player.sendMessage("§cVeuillez spécifier un message.");
			}
			else
				Locale.PLAYER_ISLAND_NONE.send(player);
		}
		else if (match(args, 0, "invite")) {
			if (!player.hasIsland()) {
				Locale.PLAYER_ISLAND_NONE.send(player);
				return;
			}

			Island island = player.getIsland();
			if (!island.hasRoleOrAbove(player, IslandRole.RECRUTER)) {
				Locale.PLAYER_ISLAND_RANK_TOOLOW.send(player, IslandRole.RECRUTER);
				return;
			}

			if (args.length < 2) {
				Locale.ARGUMENT_PLAYER.send(player);
				return;
			}

			OfflinePlayer off = Bukkit.getOfflinePlayer(args[1]);
			if (!off.getUniqueId().equals(player.getBaseId())) {
				if (off.isOnline()) {
					SkyblockPlayer trgt = Wrapper.wrapPlayer(off.getPlayer());
					if (trgt == null) {
						return;
					}
					if (!trgt.hasIsland()) {
						sendQueue(trgt.bukkit(), player.bukkit(), (plyr, target) -> {
							if (plyr == null) return;

							if (target.hasIsland()) {
								Locale.PLAYER_ISLAND_CREATE_ALREADYHAS.send(target);
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
							target.getIsland().broadcast("§6" + target.bukkit().getName() + " §ea rejoint votre île sur l'invitation de §6" + plyr.bukkit().getName());
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
		else if (match(args, 0, "promote")) {
			if (!player.hasIsland()) {
				Locale.PLAYER_ISLAND_NONE.send(player);
				return;
			}

			Island island = player.getIsland();
			if (!island.hasRoleOrAbove(player, IslandRole.OFFICIER)) {
				Locale.PLAYER_ISLAND_RANK_TOOLOW.send(player, IslandRole.OFFICIER);
				return;
			}

			if (args.length < 2) {
				Locale.ARGUMENT_PLAYER.send(player);
				return;
			}

			OfflinePlayer off = Bukkit.getOfflinePlayer(args[1]);
			if (!off.getUniqueId().equals(player.getBaseId())) {
				if (off.isOnline() || off.hasPlayedBefore()) {
					if (island.getMembersMap().containsKey(off.getUniqueId())) {
						if (island.hasRoleOrAbove(off, island.getRole(player))) {
							Locale.TEAM_TARGET_SUPERIOR.send(player);
							return;
						}

						IslandRole originalRole = island.getRole(off.getUniqueId());
						IslandRole newRole = null;
						switch (originalRole) {
							case OFFICIER:
								player.sendMessage("§cVous ne pouvez pas promouvoir un membre chef de l'île de cette façon. Si tel était votre but, faîtes '/is makeleader " + off.getName() + "'.");
								return;
							case RECRUTER:
								if (island.isOwner(player)) {
									newRole = IslandRole.OFFICIER;
								}
								else {
									player.sendMessage("§cVous devez être chef pour assigner une personne au rang d'Officier.");
									return;
								}
								break;
							case MEMBER:
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
					else if (island.isOwner(off.getUniqueId()))
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
		else if (match(args, 0, "demote")) {
			if (!player.hasIsland()) {
				Locale.PLAYER_ISLAND_NONE.send(player);
				return;
			}

			Island island = player.getIsland();
			if (!island.hasRoleOrAbove(player, IslandRole.OFFICIER)) {
				Locale.PLAYER_ISLAND_RANK_TOOLOW.send(player, IslandRole.OFFICIER);
				return;
			}

			if (args.length < 2) {
				Locale.ARGUMENT_PLAYER.send(player);
				return;
			}

			OfflinePlayer off = Bukkit.getOfflinePlayer(args[1]);
			if (!off.getUniqueId().equals(player.getBaseId())) {
				if (off.isOnline() || off.hasPlayedBefore()) {
					if (island.getMembersMap().containsKey(off.getUniqueId())) {
						if (island.hasRoleOrAbove(off, island.getRole(player))) {
							Locale.TEAM_TARGET_SUPERIOR.send(player);
							return;
						}

						IslandRole originalRole = island.getRole(off.getUniqueId());
						IslandRole newRole = null;
						switch (originalRole) {
							case OFFICIER:
								if (island.isOwner(player)) {
									newRole = IslandRole.RECRUTER;
								}
								else {
									player.sendMessage("§cVous devez être chef pour rétrograder un Officier.");
									return;
								}
								break;
							case RECRUTER:
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
					else if (island.isOwner(off.getUniqueId()))
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
		else if (match(args, 0, "kick")) {
			if (!player.hasIsland()) {
				Locale.PLAYER_ISLAND_NONE.send(player);
				return;
			}

			Island island = player.getIsland();
			if (!island.hasRoleOrAbove(player, IslandRole.RECRUTER)) {
				Locale.PLAYER_ISLAND_RANK_TOOLOW.send(player, IslandRole.RECRUTER);
				return;
			}

			if (args.length < 2) {
				Locale.ARGUMENT_PLAYER.send(player);
				return;
			}

			OfflinePlayer off = Bukkit.getOfflinePlayer(args[1]);
			if (!off.getUniqueId().equals(player.getBaseId())) {
				if (off.isOnline() || off.hasPlayedBefore()) {
					if (island.getMembersMap().containsKey(off.getUniqueId())) {
						if (island.hasRoleOrAbove(off, island.getRole(player))) {
							Locale.TEAM_TARGET_SUPERIOR.send(player);
							return;
						}

						IslandManager.kickMember(island, off);
						sender.sendMessage(Locale.PREFIX + "§eVous avez retiré §6" + off.getName() + " §ede votre île.");

						if (off.isOnline())
							Bukkit.getPlayer(off.getUniqueId()).sendMessage(Locale.PREFIX + "§6Vous avez été retiré de votre île par §a" + sender.getName() + "§6.");
					}
					else if (island.isOwner(off.getUniqueId()))
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
		else if (match(args, 0, "leave")) {
			if (player.hasIsland()) {
				sendQueue(player.bukkit(), null, (plyr, target) -> {
					if (target == null) return;

					if (!target.hasIsland()) {
						target.sendMessage("§cVous n'avez plus d'île !");
						return;
					}

					IslandManager.kickMember(target.getIsland(), target.bukkit());
					target.sendMessage("§aVous avez quitté votre île. Repartez sur de bonnes bases en recréant une nouvelle île ou en rejoignant celle d'un de vos amis.");
				}, 20 * 60);
				if (player.isOwner()) {
					if (player.getIsland().getMembersMap().size() > 0) {
						player.sendMessage("§cVous êtes le chef de cette île et il y a des membres présents! Supprimez chaque membre avant de refaire cette commande de nouveau.");
						return;
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
		else if (match(args, 0, "coops", "cooplist")) {
			if (player.hasIsland()) {
				List<UUID> coopList = player.getIsland().getCoops();
				if (coopList != null && !coopList.isEmpty()) {
					player.sendMessage("§6Coopérants §7(" + coopList.size() + ")§6:");
					coopList.forEach(member -> sender.sendMessage("§8- §e" + Bukkit.getOfflinePlayer(member).getName()));
				}
				else {
					player.sendMessage("§6Vous n'avez aucun coopérants.");
				}
			}
			else
				Locale.PLAYER_ISLAND_NONE.send(player);
		}
		else if (match(args, 0, "coop")) {
			if (!player.hasIsland()) {
				Locale.PLAYER_ISLAND_NONE.send(player);
				return;
			}

			Island island = player.getIsland();
			if (!island.hasRoleOrAbove(player, IslandRole.RECRUTER)) {
				Locale.PLAYER_ISLAND_RANK_TOOLOW.send(player, IslandRole.RECRUTER);
				return;
			}

			if (args.length < 2) {
				Locale.ARGUMENT_PLAYER.send(player);
				return;
			}

			Player target = Bukkit.getPlayer(args[1]);
			if (target == null) {
				Locale.TARGET_NEORDISCONNECT.send(player);
				return;
			}

			if (target.getUniqueId().equals(player.getBaseId())) {
				Locale.TARGET_YOUSELF.send(player);
				return;
			}

			if (player.getIsland().hasFullAccess(target)) {
				Locale.TEAM_TARGET_ISMEMBER.send(player);
				return;
			}

			SkyblockPlayer trgt = Wrapper.wrapPlayer(target.getPlayer());
			if (trgt == null) {
				return;
			}
			if (trgt.hasIsland()) {
				if (!island.isCooped(trgt.bukkit())) {
					CoopsManager.manager.coop(trgt.getBaseId(), island, player.bukkit());
					player.sendMessage("§aVous avez invité §2" + target.getName() + " §aa vous aider.");
					trgt.sendMessage("§eVous avez été invité à aider l'île " + island.getDisplayName() + "§e.");
				}
				else
					player.sendMessage("§6Ce joueur est déjà un de vos coopérants. Faîte '/is coops' pour en voir la liste.");
			}
			else
				player.sendMessage("§cCe joueur n'a pas d'île, vous ne pouvez donc pas l'inviter en temps que coopérant. Dîtes-lui d'en créer une ou de rejoindre la votre !");
		}
		else if (match(args, 0, "uncoop")) {
			if (!player.hasIsland()) {
				Locale.PLAYER_ISLAND_NONE.send(player);
				return;
			}

			Island island = player.getIsland();
			if (!island.hasRoleOrAbove(player, IslandRole.RECRUTER)) {
				Locale.PLAYER_ISLAND_RANK_TOOLOW.send(player, IslandRole.RECRUTER);
				return;
			}

			if (args.length < 2) {
				Locale.ARGUMENT_PLAYER.send(player);
				return;
			}

			OfflinePlayer off = Bukkit.getOfflinePlayer(args[1]);
			if (!off.hasPlayedBefore() && !off.isOnline()) {
				Locale.TARGET_NOTEXISTS.send(player);
				return;
			}

			if (off.getUniqueId().equals(player.getBaseId())) {
				Locale.TARGET_YOUSELF.send(player);
				return;
			}

			if (player.getIsland().hasFullAccess(off)) {
				Locale.TEAM_TARGET_ISMEMBER.send(player);
				return;
			}

			if (off.isOnline() && island.isCooped(off)) {
				CoopsManager.manager.unCoop(off.getUniqueId(), island, true);
				player.sendMessage("§2Vous avez retiré §6" + off.getName() + " §2de vos coopérants.");
				Bukkit.getPlayer(off.getUniqueId()).sendMessage(Locale.PREFIX + "§6Vous n'êtes plus coopérant de l'île " + island.getDisplayName() + "§6.");
			}
			else
				player.sendMessage("§6Ce joueur n'est pas un de vos coopérants, vous pouvez dormir sur vos deux oreilles.");
		}

		/*
		Visitors
		 */
		else if (match(args, 0, "bans", "banlist")) {
			if (player.hasIsland()) {
				List<UUID> bannedList = player.getIsland().getBanList();
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
		else if (match(args, 0, "ban")) {
			if (!player.hasIsland()) {
				Locale.PLAYER_ISLAND_NONE.send(player);
				return;
			}

			Island island = player.getIsland();
			if (!island.hasRoleOrAbove(player, IslandRole.RECRUTER)) {
				Locale.PLAYER_ISLAND_RANK_TOOLOW.send(player, IslandRole.RECRUTER);
				return;
			}

			if (args.length < 2) {
				Locale.ARGUMENT_PLAYER.send(player);
				return;
			}

			OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
			if (!target.hasPlayedBefore() && !target.isOnline()) {
				Locale.TARGET_NOTEXISTS.send(player);
				return;
			}

			if (target.getUniqueId().equals(player.getBaseId())) {
				Locale.TARGET_YOUSELF.send(player);
				return;
			}

			if ((target.isOnline() && Bukkit.getPlayer(target.getUniqueId()).hasPermission("leezsky.ban.exempt")) || target.isOp()) {
				Locale.BAN_TARGET_ISADMIN.send(player);
				return;
			}

			if (player.getIsland().hasFullAccess(target)) {
				Locale.TEAM_TARGET_ISMEMBER.send(player);
				return;
			}

			if (island.banPlayer(target)) {
				player.sendMessage("§aVous avez banni §2" + target.getName() + " §ade votre île.");
				if (target.isOnline()) {
					Player onlineTarget = Bukkit.getPlayer(target.getUniqueId());
					onlineTarget.sendMessage(Locale.PREFIX + "§eVous avez été banni de l'île §6" + island.getDisplayName() + "§e.");
					if (!AdminCommand.BYPASSING.contains(onlineTarget.getUniqueId()) && island.isInBounds(onlineTarget.getLocation()))
						ServerUtil.performCommand("spawn " + onlineTarget.getName());
				}
			}
			else
				player.sendMessage("§6Ce joueur est déjà banni de votre île. Faîtes '/is banlist' pour voir tous les joueur bannis de votre île.");
		}
		else if (match(args, 0, "unban", "pardon")) {
			if (!player.hasIsland()) {
				Locale.PLAYER_ISLAND_NONE.send(player);
				return;
			}

			Island island = player.getIsland();
			if (!island.hasRoleOrAbove(player, IslandRole.RECRUTER)) {
				Locale.PLAYER_ISLAND_RANK_TOOLOW.send(player, IslandRole.RECRUTER);
				return;
			}

			if (args.length < 2) {
				Locale.ARGUMENT_PLAYER.send(player);
				return;
			}

			OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
			if (!target.hasPlayedBefore() && !target.isOnline()) {
				Locale.TARGET_NOTEXISTS.send(player);
				return;
			}

			if (target.getUniqueId().equals(player.getBaseId())) {
				Locale.TARGET_YOUSELF.send(player);
				return;
			}

			if (player.getIsland().hasFullAccess(target)) {
				Locale.TEAM_TARGET_ISMEMBER.send(player);
				return;
			}

			if (island.getBanList().contains(target.getUniqueId())) {
				island.pardonPlayer(target);
				player.sendMessage("§2Vous avez dé-banni §a" + target.getName() + " §2de votre île.");
				if (target.isOnline())
					target.getPlayer().sendMessage(Locale.PREFIX + "§eVous avez été dé-banni de l'île §6" + island.getDisplayName() + "§e.");
			}
			else
				player.sendMessage("§6Ce joueur n'est pas banni de votre île.");
		}
		else if (match(args, 0, "expel")) {
			if (args.length >= 2) {
				Player target = Bukkit.getPlayer(args[1]);
				if (target == null || !player.bukkit().canSee(target)) {
					Locale.TARGET_NEORDISCONNECT.send(player);
					return;
				}

				if (!target.getUniqueId().equals(player.getBaseId())) {
					if (player.getIsland().isInBounds(target.getLocation())) {
						if (player.getIsland().hasFullAccess(target)) {
							Locale.TEAM_TARGET_ISMEMBER.send(player);
							return;
						}

						if (AdminCommand.BYPASSING.contains(target.getUniqueId())) {
							Locale.TARGET_BYPASSING.send(player);
							return;
						}

						ServerUtil.performCommand("spawn " + target.getName());
						sender.sendMessage(Locale.PREFIX + "§eVous avez expulsé §6" + target.getName() + " §ede votre île.");
						target.sendMessage(Locale.PREFIX + "§eVous avez été expulsé de l'île de §e" + sender.getName() + "§e.");
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
		Help messages
		 */
		else if (match(args, 0, "help", "?")) {
			if (match(args, 1, "all", "tout")) {
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
			else if (match(args, 1, "global", "general", "général")) {
				sender.sendMessage(" ");

				sender.sendMessage(Locale.PREFIX + "§3Aide de la commande /island §7(A propos des généralités):");
				sendHelp(sender, HelpGroup.GLOBAL);
				sender.sendMessage(" ");
			}
			else if (match(args, 1, "island", "is")) {
				sender.sendMessage(" ");

				sender.sendMessage(Locale.PREFIX + "§3Aide de la commande /island §7(A propos des îles):");
				sendHelp(sender, HelpGroup.ISLAND);
				sender.sendMessage(" ");

				sendHelp(sender, HelpGroup.ISLAND_2);

				sender.sendMessage(" ");
			}
			else if (match(args, 1, "team", "equipe")) {
				sender.sendMessage(" ");

				sender.sendMessage(Locale.PREFIX + "§3Aide de la commande /island §7(A propos des équipes):");
				sendHelp(sender, HelpGroup.TEAM);

				sender.sendMessage(" ");
			}
			else if (match(args, 1, "coop", "coops")) {
				sender.sendMessage(" ");

				sender.sendMessage(Locale.PREFIX + "§3Aide de la commande /island §7(A propos des coopérants):");
				sendHelp(sender, HelpGroup.COOPS);

				sender.sendMessage(" ");
			}
			else if (match(args, 1, "visitor", "visitors")) {
				sender.sendMessage(" ");

				sender.sendMessage(Locale.PREFIX + "§3Aide de la commande /island §7(A propos des visiteurs):");
				sendHelp(sender, HelpGroup.VISITORS);

				sender.sendMessage(" ");
			}
			else
				player.sendMessage("§6Utilisation: /island help <all|global|island|team|coops|visitors>");
		}
		else if (args.length == 0 || match(args, 0, "go", "tp")) {
			if (player.hasIsland()) {
				if (player.hasPersonalHome() && !match(args, 1, "main"))
					player.bukkit().teleport(player.getPersonalHome());
				else
					player.bukkit().teleport(player.getIsland().getHomeLocation());

				BorderAPI.setOwnBorder(player);
				Locale.PLAYER_ISLAND_TELEPORT.send(player);
			}
			else
				Locale.PLAYER_ISLAND_NONE.send(player);
		}
		else {
			Locale.COMMAND_UNKOWN.send(player);
		}
	}

	private void sendHelp(CommandSender sender, HelpGroup helpGroup) {
		switch (helpGroup) {
			case GLOBAL:
				sender.sendMessage("§6/island §ecreate §8- §eCréer une île"); // DONE
				sender.sendMessage("§6/island §ego [main]§8- §eSe téléporter à votre île"); // DONE
				sender.sendMessage("§6/island §elevel §8- §eCalculer le niveau de votre île"); // TODO: limit usages and when to actually calc
				sender.sendMessage("§6/island §einfo (Pseudo) §8- §eAfficher toutes les informations d'une île"); // DONE
				sender.sendMessage("§6/island §ewarp <Ile> §8- §eSe téléporter à une île");
				sender.sendMessage("§6/island §ewarps §8- §eLister tous les îles avec un point de téléportation");
				break;
			case ISLAND:
				sender.sendMessage("§6/island §esethome §8- §eChanger le home principal de l'île"); // DONE
				sender.sendMessage("§6/island §epersonalhome §8- §eChanger votre point de téléportation"); // DONE
				sender.sendMessage("§6/island §esetwarp §8- §eDéfinir le warp de votre île");
				sender.sendMessage("§6/island §edelwarp §8- §eDéfinir le warp de votre île");
				sender.sendMessage("§6/island §esetname <Nom> §8- §eChanger le nom de votre île"); // DONE
				sender.sendMessage("§6/island §elock §8- §eFermer ou ouvrir votre île au public"); // DONE
				break;
			case ISLAND_2:
				sender.sendMessage("§6/island §esettings §8- §eConfigurer les paramètres de votre île"); // DONE
				sender.sendMessage("§6/island §eshop §8- §eOuvrir le shop d'île (améliorations, etc)");
				sender.sendMessage("§6/island §evault §8- §eOuvrir le coffre d'île");
				break;
			case TEAM:
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
				sender.sendMessage("§6/island §ecoops §8- §eVoir la liste de vos coopérants temporaires"); // DONE
				sender.sendMessage("§6/island §ecoop <Joueur> §8- §eInviter temporairement un joueur à vous aider"); // DONE
				sender.sendMessage("§6/island §euncoop <Joueur> §8- §eSupprimer un coopérant"); // DONE
				break;
			case VISITORS:
				sender.sendMessage("§6/island §ebanlist §8- §eVoir les joueurs bannis de votre île"); // DONE
				sender.sendMessage("§6/island §eban <Joueur> §8- §eRestreindre l'accès à un joueur de votre île"); // DONE
				sender.sendMessage("§6/island §eunban <Joueur> §8- §eDé-bannir un joueur de votre île"); // DONE
				sender.sendMessage("§6/island §eexpel <Joueur> §8- §eRenvoyer au spawn une personne présente sur votre île"); // DONE
				break;
		}
	}

	private enum HelpGroup {
		GLOBAL, ISLAND, ISLAND_2, TEAM, COOPS, VISITORS
	}

	private interface PlayerCommandAction {

		void run(SkyblockPlayer sender, SkyblockPlayer executor);

	}

}
