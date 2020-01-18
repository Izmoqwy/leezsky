package lz.izmoqwy.core.api.database;

import lombok.Getter;
import lz.izmoqwy.core.api.database.exceptions.SQLActionImpossibleException;
import lz.izmoqwy.core.api.database.exceptions.SQLActionImpossibleException.ImpossibleExceptionType;
import lz.izmoqwy.core.self.CorePrinter;
import lz.izmoqwy.core.self.LeezCore;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Getter
public abstract class SQLDatabase implements Database {

	protected Connection connection = null;

	private final String name, type;
	private final Plugin plugin;

	protected SQLDatabase(String name, Plugin plugin, String type) {
		this.name = name;
		this.plugin = plugin;
		this.type = type;

		LeezCore.registerDB(this);
	}

	public abstract boolean connect();

	public boolean disconnect() {
		if (isConnected()) {
			try {
				connection.close();
				connection = null;
				CorePrinter.print("\"" + getName() + "\"" + " database disconnected from " + type + ".");
			}
			catch (SQLException ex) {
				ex.printStackTrace();
				return false;
			}
		}
		return true;
	}

	public boolean isConnected() {
		return connection != null;
	}

	public Table getTable(String name) {
		return new Table(name);
	}

	public PreparedStatement prepare(String sql) throws SQLException {
		return connection.prepareStatement(sql);
	}

	public void execute(String sql) throws SQLException {
		PreparedStatement prepared = prepare(sql);
		prepared.execute();
		prepared.close();
	}

	public class Table {

		private String name;

		public Table(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

		public String getString(String entry, String key, String value, String def)
				throws SQLActionImpossibleException {
			try {
				PreparedStatement executor = prepare("SELECT " + entry + " FROM " + name + " WHERE " + key + " = ?");
				executor.setString(1, value);

				ResultSet rs = executor.executeQuery();
				if (rs.next())
					def = rs.getString(entry);
				executor.close();
			}
			catch (SQLException ex) {
				throw new SQLActionImpossibleException(ex.getMessage(), ImpossibleExceptionType.SQLERROR);
			}
			return def;
		}

		public String getString(String entry, String key, String value)
				throws SQLActionImpossibleException {
			return getString(entry, key, value, "None");
		}

		public void setString(String key, String value, String where, String whereequals)
				throws SQLActionImpossibleException {
			if (key == null)
				throw new SQLActionImpossibleException("Key cannot be null", ImpossibleExceptionType.KEYISNULL);

			try {
				PreparedStatement executor = prepare("UPDATE " + name + " SET " + key + "=\"" + value + "\" WHERE " + where + " = ?");
				executor.setString(1, whereequals);
				executor.executeUpdate();
				executor.close();
			}
			catch (SQLException ex) {
				throw new SQLActionImpossibleException(ex.getMessage(), ImpossibleExceptionType.SQLERROR);
			}
		}

		public int getInt(String entry, String key, String value, int def)
				throws SQLActionImpossibleException {

			try {
				PreparedStatement executor = prepare("SELECT " + entry + " FROM " + name + " WHERE `" + key + "` = \"" + value + "\"");
				ResultSet rs = executor.executeQuery();
				if (rs.next())
					def = rs.getInt(entry);
				executor.close();
			}
			catch (SQLException ex) {
				throw new SQLActionImpossibleException(ex.getMessage(), ImpossibleExceptionType.SQLERROR);
			}
			return def;
		}

		public int getInt(String entry, String key, String value)
				throws SQLActionImpossibleException {
			return getInt(entry, key, value, -1);
		}

		public void setInt(String key, int value, String where, String whereequals)
				throws SQLActionImpossibleException {
			if (key == null)
				throw new SQLActionImpossibleException("Key cannot be null", ImpossibleExceptionType.KEYISNULL);

			try {
				PreparedStatement executor = prepare("UPDATE " + name + " SET " + key + "=" + value + " WHERE " + where + " = ?");
				executor.setString(1, whereequals);
				executor.executeUpdate();
				executor.close();
			}
			catch (SQLException ex) {
				throw new SQLActionImpossibleException(ex.getMessage(), ImpossibleExceptionType.SQLERROR);
			}
		}

		public double getDouble(String entry, String key, String value, double def)
				throws SQLActionImpossibleException {

			try {
				PreparedStatement executor = prepare("SELECT " + entry + " FROM " + name + " WHERE `" + key + "` = \"" + value + "\"");
				ResultSet rs = executor.executeQuery();
				if (rs.next())
					def = rs.getDouble(entry);
				executor.close();
			}
			catch (SQLException ex) {
				throw new SQLActionImpossibleException(ex.getMessage(), ImpossibleExceptionType.SQLERROR);
			}
			return def;
		}

		public double getDouble(String entry, String key, String value)
				throws SQLActionImpossibleException {
			return getDouble(entry, key, value, -1);
		}

		public void setDouble(String key, double value, String where, String whereequals)
				throws SQLActionImpossibleException {

			if (key == null)
				throw new SQLActionImpossibleException("Key cannot be null", ImpossibleExceptionType.KEYISNULL);

			try {
				PreparedStatement executor = prepare("UPDATE " + name + " SET " + key + "=" + value + " WHERE " + where + " = ?");
				executor.setString(1, whereequals);
				executor.executeUpdate();
				executor.close();

			}
			catch (SQLException ex) {
				throw new SQLActionImpossibleException(ex.getMessage(), ImpossibleExceptionType.SQLERROR);
			}
		}

		public long getLong(String entry, String key, String value, long def)
				throws SQLActionImpossibleException {

			try {
				PreparedStatement executor = prepare("SELECT " + entry + " FROM " + name + " WHERE `" + key + "` = \"" + value + "\"");
				ResultSet rs = executor.executeQuery();
				if (rs.next())
					def = rs.getInt(entry);
				executor.close();
			}
			catch (SQLException ex) {
				throw new SQLActionImpossibleException(ex.getMessage(), ImpossibleExceptionType.SQLERROR);
			}
			return def;
		}

		public long getLong(String entry, String key, String value)
				throws SQLActionImpossibleException {
			return getInt(entry, key, value, -1);
		}

		public void setLong(String key, long value, String where, String whereequals)
				throws SQLActionImpossibleException {

			if (key == null)
				throw new SQLActionImpossibleException("Key cannot be null", ImpossibleExceptionType.KEYISNULL);

			try {
				PreparedStatement executor = prepare("UPDATE " + name + " SET " + key + "=" + value + " WHERE " + where + " = ?");
				executor.setString(1, whereequals);
				executor.executeUpdate();
				executor.close();
			}
			catch (SQLException ex) {
				throw new SQLActionImpossibleException(ex.getMessage(), ImpossibleExceptionType.SQLERROR);
			}
		}

		public void increase(String key, int i, String where, String whereequals)
				throws SQLActionImpossibleException {

			if (key == null)
				throw new SQLActionImpossibleException("Key cannot be null", ImpossibleExceptionType.KEYISNULL);

			try {
				PreparedStatement executor = prepare("UPDATE " + name + " SET " + key + "=" + key + "+? WHERE " + where + " = ?");
				executor.setInt(1, i);
				executor.setString(2, whereequals);
				executor.executeUpdate();
				executor.close();
			}
			catch (SQLException ex) {
				throw new SQLActionImpossibleException(ex.getMessage(), ImpossibleExceptionType.SQLERROR);
			}
		}

		public void decrease(String key, int i, String where, String whereequals)
				throws SQLActionImpossibleException {

			if (key == null)
				throw new SQLActionImpossibleException("Key cannot be null", ImpossibleExceptionType.KEYISNULL);

			try {
				PreparedStatement executor = prepare("UPDATE " + name + " SET " + key + "=" + key + "-? WHERE " + where + " = ?");
				executor.setInt(1, i);
				executor.setString(2, whereequals);
				executor.executeUpdate();
				executor.close();
			}
			catch (SQLException ex) {
				throw new SQLActionImpossibleException(ex.getMessage(), ImpossibleExceptionType.SQLERROR);
			}
		}

		public void delete(String where, String whereequals) throws SQLException {
			PreparedStatement executor = prepare("DELETE FROM " + name + " WHERE " + where + " = ?");
			executor.setString(1, whereequals);
			executor.executeUpdate();
			executor.close();
		}

		public boolean hasResult(String key, String where, String whereequals)
				throws SQLActionImpossibleException {

			if (key == null)
				throw new SQLActionImpossibleException("Key cannot be null", ImpossibleExceptionType.KEYISNULL);

			boolean def = false;
			try {
				String bonus = type.equalsIgnoreCase("SQLite") ? " COLLATE NOCASE" : "";
				PreparedStatement executor = prepare("SELECT " + key + " FROM " + name + " WHERE " + where + " = \"" + whereequals + "\"" + bonus);
				ResultSet rs = executor.executeQuery();
				def = rs.next();
				executor.close();
			}
			catch (SQLException ex) {
				throw new SQLActionImpossibleException(ex.getMessage(), ImpossibleExceptionType.SQLERROR);
			}
			return def;
		}

		public boolean hasResult(String key)
				throws SQLActionImpossibleException {

			if (key == null)
				throw new SQLActionImpossibleException("Key cannot be null", ImpossibleExceptionType.KEYISNULL);

			boolean def = false;
			try {
				String bonus = type.equalsIgnoreCase("SQLite") ? " COLLATE NOCASE" : "";
				PreparedStatement executor = prepare("SELECT " + key + " FROM " + name + bonus);
				ResultSet rs = executor.executeQuery();
				def = rs.next();
				executor.close();
			}
			catch (SQLException ex) {
				throw new SQLActionImpossibleException(ex.getMessage(), ImpossibleExceptionType.SQLERROR);
			}
			return def;
		}

	}

}
