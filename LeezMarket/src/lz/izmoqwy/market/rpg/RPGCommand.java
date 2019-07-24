package lz.izmoqwy.market.rpg;

import lz.izmoqwy.core.api.CommandOptions;
import lz.izmoqwy.core.api.CoreCommand;
import lz.izmoqwy.core.api.database.exceptions.SQLActionImpossibleException;
import lz.izmoqwy.core.utils.TextUtil;
import lz.izmoqwy.market.Locale;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Map;

import static lz.izmoqwy.market.rpg.RPGManager.loadRPGPlayer;

public abstract class RPGCommand extends CoreCommand {

	private final boolean needsEnergy;

	public RPGCommand(String name, CommandOptions options, boolean needsEnergy) {
		super(name, options);
		this.needsEnergy = needsEnergy;
	}

	public RPGCommand(String name, CommandOptions options) {
		this(name, options, false);
	}

	@Override
	protected void execute(CommandSender commandSender, String usedCommand, String[] args) {
		try {
			Map.Entry<RPGPlayer, Boolean> loaded = loadRPGPlayer(((Player) commandSender).getUniqueId(), true);
			if (loaded.getValue()) {
				commandSender.sendMessage(Locale.RPG_PREFIX + "§aNous venons de créer votre tout nouveau compte, nous vous offrons §e10⚡ §apour commencer.");
			}

			RPGPlayer player = loaded.getKey();
			if (needsEnergy) {
				if (player.getEnergy() <= 0) {
					Locale.RPG_NO_ENERGY.send(commandSender);
					return;
				}
			}

			execute(loaded.getKey(), usedCommand, args);
		}
		catch (SQLException | SQLActionImpossibleException e) {
			e.printStackTrace();
			commandSender.sendMessage(Locale.PREFIX + "§4Une erreur est survenue lors de l'éxécution de cette commande RPG.");
		}
	}

	protected abstract void execute(RPGPlayer player, String usedCommand, String[] args);

	protected static String readbleNumber(long amount) {
		return TextUtil.readbleNumber(amount);
	}

	protected static void calcLevelUp(RPGPlayer player, int amount) {
		Player bukkit = player.getBase().getPlayer();
		if (bukkit == null)
			return;


		int level = player.calcLevel();
		player.setExp(player.getExp() + amount >= 0 ? amount : -amount);
		int newLevel = player.calcLevel();
		if (level < newLevel) {
			player.sendMessage(" ");

			int diff = newLevel - level;
			if (diff > 1) {
				player.sendMessage(Locale.RPG_PREFIX + "§aVous avez monté de §2" + diff + " §aniveaux !");
			}
			else {
				player.sendMessage(Locale.RPG_PREFIX + "§aVous avez monté de niveau !");
			}
			bukkit.playSound(bukkit.getLocation(), Sound.BLOCK_NOTE_BELL, 1, 15);
			player.sendMessage(" ");
		}
	}

}
