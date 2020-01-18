package lz.izmoqwy.market.rpg.commands;

import lz.izmoqwy.core.command.CommandOptions;
import lz.izmoqwy.core.api.database.exceptions.SQLActionImpossibleException;
import lz.izmoqwy.market.Locale;
import lz.izmoqwy.market.rpg.RPGCommand;
import lz.izmoqwy.market.rpg.RPGManager;
import lz.izmoqwy.market.rpg.RPGPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Map;

import static lz.izmoqwy.market.rpg.RPGResource.*;

public class InventoryCommand extends RPGCommand {

	public InventoryCommand(String commandName) {
		super(commandName, CommandOptions.builder()
				.playerOnly(true)
				.build());
	}

	@Override
	protected void execute(RPGPlayer player, String usedCommand, String[] args) {
		if (args.length == 1) {
			OfflinePlayer offTarget = Bukkit.getOfflinePlayer(args[0]);
			Map.Entry<RPGPlayer, Boolean> wrappedTarget;
			if (offTarget.hasPlayedBefore() || offTarget.isOnline()) {
				if (offTarget == player.getBase()) {
					execute(player, usedCommand, new String[0]);
					return;
				}

				try {
					wrappedTarget = RPGManager.loadRPGPlayer(offTarget.getUniqueId(), false);
				}
				catch (SQLException | SQLActionImpossibleException e) {
					e.printStackTrace();
					player.sendMessage(Locale.PREFIX + "§4Une erreur est survenue !");
					return;
				}
			}
			else {
				Player target = Bukkit.getPlayer(args[0]);
				if (target != null && target.isValid()) {
					if (target == player.getBase()) {
						execute(player, usedCommand, new String[0]);
						return;
					}

					try {
						wrappedTarget = RPGManager.loadRPGPlayer(target.getUniqueId(), false);
					}
					catch (SQLException | SQLActionImpossibleException e) {
						e.printStackTrace();
						player.sendMessage(Locale.PREFIX + "§4Une erreur est survenue !");
						return;
					}
				}
				else {
					player.sendMessage(Locale.PREFIX + "§cCe joueur n'éxiste pas.");
					return;
				}
			}

			if (!wrappedTarget.getValue()) {
				RPGPlayer targetPlayer = wrappedTarget.getKey();
				player.sendMessage(" ");
				player.sendMessage(Locale.RPG_PREFIX + "§9Inventaire de §b" + targetPlayer.getBase().getName());
				player.sendMessage(Locale.PREFIX + "§3Ressources:");
				player.sendMessage("§8➥ §6⚡ Énergie: §e" + targetPlayer.getEnergy() + "/" + targetPlayer.getMax_energy() + " §b[+1/" + RPGManager.ENERGY_REGEN_TIME + "s]");
				player.sendMessage("§8➥ §6✦ Points: §e" + targetPlayer.getPoints());
				player.sendMessage(" ");
				player.sendMessage("§8➥ " + DARKMATTER.getFullName() + ": §f" + readbleNumber(targetPlayer.getRes_darkmatter()));
				player.sendMessage("§8➥ " + URANIUM.getFullName() + ": §f" + readbleNumber(targetPlayer.getRes_uranium()));
				player.sendMessage("§8➥ " + TITANE.getFullName() + ": §f" + readbleNumber(targetPlayer.getRes_titane()));
				player.sendMessage("§8➥ " + COPPER.getFullName() + ": §f" + readbleNumber(targetPlayer.getRes_copper()));

				player.sendMessage(" ");
				player.sendMessage(Locale.PREFIX + "§3Poissons:");
				player.sendMessage("§8➥ §2❀ Communs: §f" + targetPlayer.getFish_common());
				player.sendMessage("§8➥ §e✮ Rares: §f" + targetPlayer.getFish_uncommon());
				player.sendMessage(" ");
			}
			else {
				player.sendMessage(Locale.RPG_PREFIX + "§cCe joueur n'a pas de compte.");
			}
		}
		else {
			player.sendMessage(" ");
			player.sendMessage(Locale.RPG_PREFIX + "§9Inventaire");
			player.sendMessage(Locale.PREFIX + "§3Ressources:");
			player.sendMessage("§8➥ §6⚡ Énergie: §e" + player.getEnergy() + "/" + player.getMax_energy() + " §b[+1/" + RPGManager.ENERGY_REGEN_TIME + "s]");
			player.sendMessage("§8➥ §6✦ Points: §e" + player.getPoints());
			player.sendMessage(" ");
			player.sendMessage("§8➥ " + DARKMATTER.getFullName() + ": §f" + readbleNumber(player.getRes_darkmatter()));
			player.sendMessage("§8➥ " + URANIUM.getFullName() + ": §f" + readbleNumber(player.getRes_uranium()));
			player.sendMessage("§8➥ " + TITANE.getFullName() + ": §f" + readbleNumber(player.getRes_titane()));
			player.sendMessage("§8➥ " + COPPER.getFullName() + ": §f" + readbleNumber(player.getRes_copper()));

			player.sendMessage(" ");
			player.sendMessage(Locale.PREFIX + "§3Poissons:");
			player.sendMessage("§8➥ §2❀ Communs: §f" + player.getFish_common());
			player.sendMessage("§8➥ §e✮ Rares: §f" + player.getFish_uncommon());
			player.sendMessage(" ");
		}
	}

}
