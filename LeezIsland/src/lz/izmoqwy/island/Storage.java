package lz.izmoqwy.island;

import lz.izmoqwy.core.api.database.SQLDatabase;
import lz.izmoqwy.core.api.database.SQLite;
import lz.izmoqwy.core.self.CorePrinter;

import java.io.File;

public class Storage {

	public static final SQLite DB;
	public static final SQLDatabase.Table ISLANDS, PLAYERS, SETTINGS, BANKS, SPAWNERS;

	static {
		DB = new SQLite("LeezIsland", LeezIsland.getInstance(), new File(LeezIsland.getInstance().getDataFolder(), "storage.db"));
		DB.connect();
		try {

			DB.execute("CREATE TABLE IF NOT EXISTS `Islands` ( `island_id` VARCHAR(12) UNIQUE, `leader` VARCHAR(36) UNIQUE, `name` VARCHAR(36) UNIQUE, `level` INTEGER(7), `settings` VARCHAR(128), `toWrap` VARCHAR(255), `members_toWrap` TEXT )");
			DB.execute("CREATE TABLE IF NOT EXISTS `Players` ( `player_id` VARCHAR(36) UNIQUE, `island_id` VARCHAR(12), `lastRestart` BIGINT(13), `personalHome` VARCHAR(128) )");
			DB.execute("CREATE TABLE IF NOT EXISTS `Settings` ( `setting_name` VARCHAR(12) UNIQUE, `valueString` VARCHAR(256), `valueInt` INTEGER(7) )");
			DB.execute("CREATE TABLE IF NOT EXISTS `Banks` ( `island_id` VARCHAR(12) UNIQUE, `solde` DOUBLE(11, 2), `valueInt` INTEGER(7) )");
			DB.execute("CREATE TABLE IF NOT EXISTS `Spawners` ( `location` VARCHAR(64) UNIQUE, `data` VARCHAR(255) )");
			CorePrinter.print("Successfully initialized LeezIsland database.");

		}
		catch (Exception ex) {
			ex.printStackTrace();
			System.err.println("[LeezIsland] Impossible d'éxécuter de préparation de commandes en SQLite, de gros problèmes peuvent survenir.");
		}

		ISLANDS = DB.getTable("Islands");
		PLAYERS = DB.getTable("Players");
		SETTINGS = DB.getTable("Settings");
		BANKS = DB.getTable("Banks");
		SPAWNERS = DB.getTable("Spawners");
	}

}
