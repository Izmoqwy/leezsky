package lz.izmoqwy.market.rpg.commands;

import lz.izmoqwy.core.api.CommandOptions;
import lz.izmoqwy.market.Locale;
import lz.izmoqwy.market.rpg.RPGCommand;
import lz.izmoqwy.market.rpg.RPGPlayer;

public class InventoryCommand extends RPGCommand {

	public InventoryCommand(String commandName) {
		super(commandName, new CommandOptions().playerOnly());
	}

	@Override
	protected void execute(RPGPlayer player, String usedCommand, String[] args) {
		player.sendMessage(" ");
		player.sendMessage(Locale.PREFIX + "§3Ressources:");
		player.sendMessage("§8➥ §6⚡ Énergie: §e" + player.getEnergy() + "/10");
		player.sendMessage("§8➥ §6✦ Points: §e" + player.getPoints());
		player.sendMessage(" ");
		player.sendMessage("§8➥ §8✺ Matière noire: §f" + player.getRes_darkmatter());
		player.sendMessage("§8➥ §a☢ Uranium: §f" + player.getRes_uranium());
		player.sendMessage("§8➥ §7❆ Titane: §f" + player.getRes_titane());
		player.sendMessage("§8➥ §c✻ Cuivre: §f" + player.getRes_copper());

		player.sendMessage(" ");
		player.sendMessage(Locale.PREFIX + "§3Poissons:");
		player.sendMessage("§8➥ §2❀ Communs: §f" + player.getFish_common());
		player.sendMessage("§8➥ §e✮ Rares: §f" + player.getFish_uncommon());
		player.sendMessage(" ");
	}

}
