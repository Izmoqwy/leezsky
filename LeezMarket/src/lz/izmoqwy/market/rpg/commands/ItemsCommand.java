package lz.izmoqwy.market.rpg.commands;

import lz.izmoqwy.core.command.CommandOptions;
import lz.izmoqwy.market.Locale;
import lz.izmoqwy.market.rpg.RPGCommand;
import lz.izmoqwy.market.rpg.RPGItem;
import lz.izmoqwy.market.rpg.RPGPlayer;

public class ItemsCommand extends RPGCommand {

	public ItemsCommand(String name) {
		super(name, CommandOptions.builder()
				.playerOnly(true)
				.build(), false);
	}

	@Override
	protected void execute(RPGPlayer player, String usedCommand, String[] args) {
		player.sendMessage(" ");

		player.sendMessage(Locale.RPG_PREFIX + "§3Objets: ");
		player.sendMessage("§8➥ §b" + RPGItem.HANGAR.getDisplayName() + ": §7Niveau " + player.getItem_storage());
		player.sendMessage("§8➥ §b" + RPGItem.PICKAXE.getDisplayName() + ": §7Niveau " + player.getItem_pickaxe());
		player.sendMessage("§8➥ §b" + RPGItem.FISHROD.getDisplayName() + ": §7Niveau " + player.getItem_fishrod());

		player.sendMessage(" ");
	}

}
