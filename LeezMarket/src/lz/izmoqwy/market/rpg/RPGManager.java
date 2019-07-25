package lz.izmoqwy.market.rpg;

import com.google.common.collect.Maps;
import lz.izmoqwy.core.api.database.exceptions.SQLActionImpossibleException;
import lz.izmoqwy.market.Locale;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import static lz.izmoqwy.market.rpg.RPGItem.*;
import static lz.izmoqwy.market.rpg.RPGResource.*;

public class RPGManager {

	// static for the moment, will be per player in the future
	public static final int ENERGY_REGEN_TIME = 120;
	public static final int ENERGY_MAX = 20;

	public static Map.Entry<RPGPlayer, Boolean> loadRPGPlayer(UUID uuid, boolean allowNewAccount) throws SQLException, SQLActionImpossibleException {
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

			int res_darkmatter = rs.getInt(DARKMATTER.dbCol());
			int res_uranium = rs.getInt(URANIUM.dbCol());
			int res_titane = rs.getInt(TITANE.dbCol());
			int res_copper = rs.getInt(COPPER.dbCol());

			long last_fish = rs.getLong("last_fish");
			int fish_common = rs.getInt("fish_common");
			int fish_uncommon = rs.getInt("fish_uncommon");

			int item_pickaxe = rs.getInt(PICKAXE.dbCol());
			int item_fishrod = rs.getInt(FISHROD.dbCol());
			int item_stockage = rs.getInt(HANGAR.dbCol());

			player = new RPGPlayer(Bukkit.getOfflinePlayer(uuid), exp, points, energy, ENERGY_MAX, last_get, res_darkmatter, res_uranium, res_titane, res_copper, last_fish, fish_common, fish_uncommon, item_pickaxe, item_fishrod, item_stockage);
		}
		statement.close();

		if (player == null) {
			if (allowNewAccount) {
				PreparedStatement statement1 = RPGStorage.DB.prepare("INSERT INTO \"Players\"(\"uuid\") VALUES (?)");
				statement1.setString(1, uuid.toString());
				statement1.execute();
				statement1.close();
			}
			player = new RPGPlayer(Bukkit.getOfflinePlayer(uuid), 0, 0, ENERGY_MAX, ENERGY_MAX, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
			newPlayer = true;
		}

		return Maps.immutableEntry(player, newPlayer);
	}

	public static boolean givePoints(OfflinePlayer bukkitPlayer, int amount) {
		try {
			RPGStorage.PLAYERS.increase("points", amount, "uuid", bukkitPlayer.getUniqueId().toString());
			return true;
		}
		catch (SQLActionImpossibleException e) {
			e.printStackTrace();
			if (bukkitPlayer.isOnline())
				bukkitPlayer.getPlayer().sendMessage(Locale.PREFIX + "§4Une erreur est survenue !");
			return false;
		}
	}

	public static boolean give(OfflinePlayer bukkitPlayer, RPGResource resource, int amount) {
		try {
			RPGStorage.PLAYERS.increase(resource.dbCol(), amount, "uuid", bukkitPlayer.getUniqueId().toString());
			return true;
		}
		catch (SQLActionImpossibleException e) {
			e.printStackTrace();
			if (bukkitPlayer.isOnline())
				bukkitPlayer.getPlayer().sendMessage(Locale.PREFIX + "§4Une erreur est survenue !");
			return false;
		}
	}

	public static boolean take(OfflinePlayer bukkitPlayer, RPGResource resource, int amount) {
		try {
			RPGStorage.PLAYERS.decrease(resource.dbCol(), amount, "uuid", bukkitPlayer.getUniqueId().toString());
			return true;
		}
		catch (SQLActionImpossibleException e) {
			e.printStackTrace();
			if (bukkitPlayer.isOnline())
				bukkitPlayer.getPlayer().sendMessage(Locale.PREFIX + "§4Une erreur est survenue !");
			return false;
		}
	}
}
