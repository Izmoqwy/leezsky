package lz.izmoqwy.market.rpg;

import com.google.common.collect.Maps;
import lz.izmoqwy.core.api.CommandOptions;
import lz.izmoqwy.core.api.CoreCommand;
import lz.izmoqwy.core.api.database.exceptions.SQLActionImpossibleException;
import lz.izmoqwy.market.Locale;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public abstract class RPGCommand extends CoreCommand {

	// static for the moment, will be per player in the future
	private static final int ENERGY_REGEN_TIME = 1;
	private static final int ENERGY_MAX = 500;

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
			Map.Entry<RPGPlayer, Boolean> loaded = loadRPGPlayer(((Player) commandSender).getUniqueId());
			if (loaded.getValue()) {
				commandSender.sendMessage(Locale.PREFIX + "§aNous venons de créer votre tout nouveau compte, nous vous offrons §e10⚡ §apour commencer.");
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
		if (amount < 1e3)
			return Long.toString(amount);

		String letter = null;
		long divider = 1;
		if (amount > 1e12) {
			letter = "T";
			divider = (long) 1e12;
		}
		else if (amount >= 1e9) {
			letter = "G";
			divider = (long) 1e9;
		}
		else if (amount >= 1e6) {
			letter = "M";
			divider = (long) 1e6;
		}
		else if (amount >= 1e3) {
			letter = "K";
			divider = (long) 1e3;
		}
		String str = Double.toString(Math.floor(amount / (divider / 100)) / 100);
		if (str.endsWith(".00"))
			str = str.substring(0, str.length() - 3);
		if (str.endsWith(".0"))
			str = str.substring(0, str.length() - 2);
		if (str.endsWith("0"))
			str = str.substring(0, str.length() - 1);
		return str + letter;
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
				player.sendMessage(Locale.PREFIX + "§aVous avez monté de §2" + diff + " §aniveaux !");
			}
			else {
				player.sendMessage(Locale.PREFIX + "§aVous avez monté de niveau !");
			}
			bukkit.playSound(bukkit.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, 1);
			player.sendMessage(" ");
		}
	}

	protected static Map.Entry<RPGPlayer, Boolean> loadRPGPlayer(UUID uuid) throws SQLException, SQLActionImpossibleException {
		PreparedStatement statement = RPGStorage.DB.prepare("SELECT * FROM " + RPGStorage.PLAYERS + " WHERE uuid = ?");
		statement.setString(1, uuid.toString());
		ResultSet rs = statement.executeQuery();

		RPGPlayer player = null;
		boolean newPlayer = false;
		if (rs.next()) {
			int exp = rs.getInt("exp");
			int points = rs.getInt("points");

			int energy = rs.getInt("energy");
			long last_get = rs.getLong("last_get");
			if (energy < ENERGY_MAX && last_get != 0) {
				int toRegen = (int) Math.floor((System.currentTimeMillis() - last_get) / (ENERGY_REGEN_TIME * 1000));
				if (toRegen > (ENERGY_MAX - energy))
					toRegen = ENERGY_MAX - energy;
				RPGStorage.PLAYERS.increase("energy", toRegen, "uuid", uuid.toString());
				energy += toRegen;
			}
			RPGStorage.PLAYERS.setLong("last_get", System.currentTimeMillis() - (last_get % (ENERGY_REGEN_TIME * 1000)), "uuid", uuid.toString());

			int res_darkmatter = rs.getInt("res_darkmatter");
			int res_uranium = rs.getInt("res_uranium");
			int res_titane = rs.getInt("res_titane");
			int res_copper = rs.getInt("res_copper");

			long last_fish = rs.getLong("last_fish");
			int fish_common = rs.getInt("fish_common");
			int fish_uncommon = rs.getInt("fish_uncommon");

			player = new RPGPlayer(Bukkit.getOfflinePlayer(uuid), exp, points, energy, ENERGY_MAX, last_get, res_darkmatter, res_uranium, res_titane, res_copper, last_fish, fish_common, fish_uncommon);
		}
		statement.close();

		if (player == null) {
			PreparedStatement statement1 = RPGStorage.DB.prepare("INSERT INTO \"Players\"(\"uuid\") VALUES (?)");
			statement1.setString(1, uuid.toString());
			statement1.execute();
			statement1.close();

			player = new RPGPlayer(Bukkit.getOfflinePlayer(uuid), 0, 0, ENERGY_MAX, ENERGY_MAX, 0, 0, 0, 0, 0, 0, 0, 0);
			newPlayer = true;
		}

		return Maps.immutableEntry(player, newPlayer);
	}
}
