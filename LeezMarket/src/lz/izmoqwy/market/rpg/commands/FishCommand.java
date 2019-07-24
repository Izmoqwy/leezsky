package lz.izmoqwy.market.rpg.commands;

import lz.izmoqwy.core.CorePrinter;
import lz.izmoqwy.core.api.CommandOptions;
import lz.izmoqwy.core.api.database.exceptions.SQLActionImpossibleException;
import lz.izmoqwy.market.Locale;
import lz.izmoqwy.market.MarketPlugin;
import lz.izmoqwy.market.rpg.RPGCommand;
import lz.izmoqwy.market.rpg.RPGPlayer;
import org.bukkit.Bukkit;

import java.util.Random;

import static lz.izmoqwy.market.rpg.RPGStorage.PLAYERS;

public class FishCommand extends RPGCommand {

	private static final Random random = new Random();
	private static final int cooldown = 120 * 1000;

	public FishCommand(String commandName) {
		super(commandName, new CommandOptions().playerOnly(), true);
	}

	@Override
	protected void execute(RPGPlayer player, String usedCommand, String[] args) {
		long elasped = System.currentTimeMillis() - player.getLast_fish();
		if (player.getLast_fish() == 0 || elasped >= cooldown) {
			final String uuid = player.getBase().getUniqueId().toString();
			try {
				PLAYERS.decrease("energy", 1, "uuid", uuid);
				PLAYERS.setLong("last_fish", System.currentTimeMillis(), "uuid", uuid);
			}
			catch (SQLActionImpossibleException e) {
				e.printStackTrace();
				player.sendMessage(Locale.PREFIX + "§4Une erreur est survenue ! §cImpossible de pêcher dans ces conditions.");
				return;
			}

			player.sendMessage(Locale.RPG_PREFIX + "§aUn poisson est dans les environs, attendez qu'il morde..");
			Bukkit.getScheduler().runTaskLater(MarketPlugin.getInstance(), () -> {
				int amount = 1;
				FishType type = FishType.WASTE;
				switch (random.nextInt(7)) {
					case 0:
					case 1:
					case 2:
					case 3:
						// Catch (a) common fish
						type = FishType.COMMON;
						if (random.nextInt(4) == 3) {
							amount = 2;
						}
						break;
					case 4:
					case 5:
						// Catch an old shoe
						type = FishType.WASTE;
						break;
					case 6:
						type = FishType.UNCOMMON;
						// Catch an uncommon fish
						break;
				}

				boolean online = player.getBase().isOnline();
				if (!online)
					CorePrinter.print("[RPG] Player {0} disconnected before catching a fish! Giving it to him anyway.", player.getBase().getName());

				switch (type) {
					case COMMON:
						try {
							PLAYERS.increase("fish_common", amount, "uuid", uuid);
							PLAYERS.increase("exp", 15 * (online ? 2 : 1) * amount, "uuid", uuid);
						}
						catch (SQLActionImpossibleException e) {
							e.printStackTrace();
						}
						if (online) {
							player.sendMessage(Locale.RPG_PREFIX + "§bVous avez attrapé " + (amount == 2 ? "deux poissons" : "un poisson") + " de rareté §2❀ Commune§b.");
							calcLevelUp(player, 30 * amount);
						}
						break;
					case UNCOMMON:
						try {
							PLAYERS.increase("fish_uncommon", amount, "uuid", uuid);
							PLAYERS.increase("energy", 2, "uuid", uuid);
							PLAYERS.increase("exp", online ? 100 : 50, "uuid", uuid);
						}
						catch (SQLActionImpossibleException e) {
							e.printStackTrace();
						}
						if (online) {
							player.sendMessage(Locale.RPG_PREFIX + "§bVous avez attrapé un poisson de rareté §e✮ Rare§b, vous regagnez également §e2⚡§b.");
							calcLevelUp(player, 100);
						}
						break;
					case WASTE:
						try {
							PLAYERS.increase("exp", online ? 15 : 5, "uuid", uuid);
						}
						catch (SQLActionImpossibleException e) {
							e.printStackTrace();
						}
						if (online) {
							player.sendMessage(Locale.RPG_PREFIX + "§bVous avez pêché un déchet, vous l'avez jeté à la poubelle, vous n'avez donc rien gagné.");
							calcLevelUp(player, 15);
						}
						break;
				}
			}, (random.nextInt(15) + 5) * 20);
		}
		else {
			player.sendMessage(Locale.RPG_PREFIX + "§6Votre canne à pêche ne sera prête que dans §e" + (Math.floor((cooldown - elasped) / 100) / 10) + " secondes§6.");
		}
	}

	enum FishType {
		COMMON, UNCOMMON, WASTE
	}

}
