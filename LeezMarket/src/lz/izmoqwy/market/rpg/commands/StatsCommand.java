package lz.izmoqwy.market.rpg.commands;

import lz.izmoqwy.core.api.CommandOptions;
import lz.izmoqwy.market.Locale;
import lz.izmoqwy.market.rpg.RPGCommand;
import lz.izmoqwy.market.rpg.RPGPlayer;

public class StatsCommand extends RPGCommand {

	public StatsCommand(String commandName) {
		super(commandName, new CommandOptions().playerOnly());
	}

	@Override
	protected void execute(RPGPlayer player, String usedCommand, String[] args) {
		player.sendMessage(" ");

		player.sendMessage(Locale.RPG_PREFIX + "§3Statistiques:");
		player.sendMessage("§8➥ §6Niveau: §e" + player.calcLevel());
		player.sendMessage("§8➥ §6Éxpérience: §e" + readbleNumber(player.getExp()));

		player.sendMessage(" ");
	}
}
