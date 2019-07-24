package lz.izmoqwy.market.rpg;

import lz.izmoqwy.core.api.database.SQLDatabase;
import lz.izmoqwy.core.api.database.SQLite;
import lz.izmoqwy.market.MarketPlugin;

import java.io.File;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class RPGStorage {

	public static final SQLDatabase DB;
	public static final SQLDatabase.Table PLAYERS;

	static {
		File file = new File(MarketPlugin.getInstance().getDataFolder(), "rpg.db");
		DB = new SQLite("Market-RPG", MarketPlugin.getInstance(), file);

		List<String> cols = Arrays.asList("\"uuid\" VARCHAR(36) NOT NULL UNIQUE", "\"joined_pos\" INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE",
				"\"exp\" BIGINT DEFAULT 0", "\"energy\" INTEGER DEFAULT " + RPGManager.ENERGY_MAX, "\"points\" INTEGER DEFAULT 0", "\"last_get\" BIGINT(13) DEFAULT 0",
				"\"res_darkmatter\" INTEGER DEFAULT 0", "\"res_uranium\" INTEGER DEFAULT 0", "\"res_titane\" INTEGER DEFAULT 0", "\"res_copper\" INTEGER DEFAULT 0",
				"\"last_fish\" BIGINT(13) DEFAULT 0", "\"fish_common\" INTEGER(5) DEFAULT 0", "\"fish_uncommon\" INTEGER(5) DEFAULT 0",
				"\"item_pickaxe\" INTEGER(3) DEFAULT 1", "\"item_fishrod\" INTEGER(3) DEFAULT 1", "\"item_hangar\" INTEGER(3) DEFAULT 1");

		try {
			DB.connect();
			DB.execute("CREATE TABLE IF NOT EXISTS \"Players\" (  " + String.join(", ", cols) + " )");
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			PLAYERS = DB.getTable("Players");
		}
	}

}
