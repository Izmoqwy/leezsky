package lz.izmoqwy.core.api.database;

import java.sql.DriverManager;
import java.sql.SQLException;

import lz.izmoqwy.core.self.CorePrinter;
import org.bukkit.plugin.Plugin;

public class MySQL extends SQLDatabase {

	private final String host, database, user, pass;

	public MySQL(String name, Plugin plugin, String host, String database, String user, String pass) {
		super(name, plugin, "MySQL");
		this.host = host;
		this.database = database;
		this.user = user;
		this.pass = pass;
	}

	@Override
	public boolean connect() {
		if (!isConnected()) {
			try {
				Class.forName("com.mysql.jdbc.Driver");
				connection = DriverManager.getConnection("jdbc:mysql://" + host + "/" + database + "?autoReconnect=true&useUnicode=yes", user, pass);
				connection.setAutoCommit(true);
				CorePrinter.print("\"" + getName() + "\"" + "' database connected by " + getType() + ".");
			}
			catch (SQLException ex) {
				ex.printStackTrace();
				return false;
			}
			catch (ClassNotFoundException ex2) {
				CorePrinter.err("MySQL driver not found.");
				return false;
			}
		}
		return true;
	}

}
