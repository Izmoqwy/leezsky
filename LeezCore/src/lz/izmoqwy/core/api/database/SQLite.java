package lz.izmoqwy.core.api.database;

import lz.izmoqwy.core.self.CorePrinter;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLite extends SQLDatabase {

	private final File file;

	public SQLite(String name, Plugin plugin, File file) {
		super(name, plugin, "SQLite");
		this.file = file;
		if (!file.exists()) {
			if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
			try {
				file.createNewFile();
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public boolean connect() {
		if (!isConnected()) {
			try {
				Class.forName("org.sqlite.JDBC");
				connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
				connection.setAutoCommit(true);
				CorePrinter.print("\"" + getName() + "\"" + "'s database connected by " + getType() + ".");
			}
			catch (SQLException ex) {
				ex.printStackTrace();
				return false;
			}
			catch (ClassNotFoundException ex2) {
				CorePrinter.err("SQLite driver not found.");
				return false;
			}
		}
		return true;
	}

}
