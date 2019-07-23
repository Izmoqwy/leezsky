package lz.izmoqwy.market.rpg;

import com.google.common.collect.Maps;
import lz.izmoqwy.core.api.CommandOptions;
import lz.izmoqwy.core.api.CoreCommand;
import lz.izmoqwy.market.Locale;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public abstract class RPGCommand extends CoreCommand {

	public RPGCommand(String name, CommandOptions options) {
		super(name, options);
	}

	@Override
	protected void execute(CommandSender commandSender, String usedCommand, String[] args) {
		try {
			Map.Entry<RPGPlayer, Boolean> loaded = loadRPGPlayer(((Player) commandSender).getUniqueId());
			if (loaded.getValue()) {
				commandSender.sendMessage(Locale.PREFIX + "§aNous venons de créer votre tout nouveau compte, nous vous offrons §e10⚡ §apour commencer.");
			}

			execute(loaded.getKey(), usedCommand, args);
		}
		catch (SQLException e) {
			e.printStackTrace();
			commandSender.sendMessage(Locale.PREFIX + "§4Une erreur est survenue lors de l'éxécution de cette commande RPG.");
		}
	}

	protected abstract void execute(RPGPlayer player, String usedCommand, String[] args);

	protected Map.Entry<RPGPlayer, Boolean> loadRPGPlayer(UUID uuid) throws SQLException {
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
			int res_darkmatter = rs.getInt("res_darkmatter");
			int res_uranium = rs.getInt("res_uranium");
			int res_titane = rs.getInt("res_titane");
			int res_copper = rs.getInt("res_copper");

			long last_fish = rs.getLong("last_fish");
			int fish_common = rs.getInt("fish_common");
			int fish_uncommon = rs.getInt("fish_uncommon");

			player = new RPGPlayer(Bukkit.getOfflinePlayer(uuid), exp, points, energy, last_get, res_darkmatter, res_uranium, res_titane, res_copper, last_fish, fish_common, fish_uncommon);
		}
		statement.close();

		if (player == null ) {
			PreparedStatement statement1 = RPGStorage.DB.prepare("INSERT INTO \"Players\"(\"uuid\") VALUES (?)");
			statement1.setString(1, uuid.toString());
			statement1.execute();
			statement1.close();

			player = new RPGPlayer(Bukkit.getOfflinePlayer(uuid), 0, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0);
			newPlayer = true;
		}

		return Maps.immutableEntry(player, newPlayer);
	}
}
