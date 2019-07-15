package lz.izmoqwy.invest;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import lz.izmoqwy.invest.commands.InvestCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Maps;

import lz.izmoqwy.api.api.database.SQLDatabase.Table;
import lz.izmoqwy.api.api.database.SQLite;
import net.milkbowl.vault.economy.Economy;

public class Money extends JavaPlugin {
	
	public static final String PREFIX = "�8� ";
	public static Economy economy = null;
	private static Money instance;
	
	protected static SQLite db;
	protected static Table INVESTS;
	
	 @Override
	 public void onEnable() {
		 instance = this;
		 getConfig().options().copyDefaults(true);
		 saveConfig();
		 
		 getCommand("invest").setExecutor(new InvestCommand());
		 getServer().getPluginManager().registerEvents(new Inventaire(), this);
		 setupEconomy();
		 
		 db = new SQLite("Money", this, new File(getDataFolder(), "storage.db"));
		 
		 try {
			db.connect();
			db.execute("CREATE TABLE IF NOT EXISTS `Invests` (`uuid`	VARCHAR(36) UNIQUE, `invested` INTEGER(11,2),`at` INTEGER(11));");
			 
			PreparedStatement prepared = db.prepare("SELECT uuid, invested, at FROM Invests WHERE invested != -1");
			ResultSet rs = prepared.executeQuery();
			
			while(rs.next())
				Inventaire.map.put(UUID.fromString(rs.getString("uuid")), Maps.immutableEntry(rs.getLong("at"), rs.getDouble("invested")));
			
			prepared.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		 INVESTS = db.getTable("Invests");
	 }
	 
	 
	 public boolean setupEconomy()
	    {
	        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
	        if (economyProvider != null) {
	            economy = economyProvider.getProvider();
	        }

	        return (economy != null);
	    }
	 

	public static Money getInstance() {
		return instance;
	}


}
