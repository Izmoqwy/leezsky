package me.izmoqwy.leezsky;

import com.google.common.collect.Maps;
import lz.izmoqwy.core.LeezCore;
import lz.izmoqwy.core.api.database.SQLDatabase;
import lz.izmoqwy.core.api.database.SQLite;
import lz.izmoqwy.core.helpers.PluginHelper;
import lz.izmoqwy.core.nms.NmsAPI;
import lz.izmoqwy.core.world.WorldsManager;
import me.izmoqwy.leezsky.challenges.ChallengePlugin;
import me.izmoqwy.leezsky.commands.*;
import me.izmoqwy.leezsky.listeners.CommandsListener;
import me.izmoqwy.leezsky.listeners.MotdListener;
import me.izmoqwy.leezsky.listeners.PlayersListener;
import me.izmoqwy.leezsky.listeners.SpawnListener;
import me.izmoqwy.leezsky.managers.InvestManager;
import me.izmoqwy.leezsky.tasks.AutoMessage;
import me.izmoqwy.leezsky.tasks.Rebooter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class LeezSky extends JavaPlugin {
	
	private static LeezSky instance;
	public boolean reboot = false;

	public static final boolean CLUSTER_HOST = false;
	public static final String PREFIX = LeezCore.PREFIX;
	public static final String TAB_HEADER = "§8┅⊰ §6PLAY.LEEZSKY.FR §8⊱┅\n",
		TAB_FOOTER = "\n§8┅⊰ §ediscord.gg/X78wMsE §8⊱┅";

	public static SQLDatabase DB;
	
	@Override
	public void onEnable() {
		instance = this;

		WorldsManager.registerPersistentVoidWorld("Spawn");
		World world = Bukkit.getWorld("Spawn");
		if (world != null) {
			PluginHelper.loadListener(this, new SpawnListener(world));
		}

		DB = new SQLite("LeezSky", this, new File(getDataFolder(), "storage.db"));

		try {
			DB.connect();
			DB.execute("CREATE TABLE IF NOT EXISTS `Invests` (`uuid` VARCHAR(36) UNIQUE, `invested` INTEGER(11,2),`at` INTEGER(11));");

			PreparedStatement prepared = DB.prepare("SELECT uuid, invested, at FROM Invests WHERE invested != -1");
			ResultSet rs = prepared.executeQuery();

			while(rs.next())
				InvestManager.map.put(UUID.fromString(rs.getString("uuid")), Maps.immutableEntry(rs.getLong("at"), rs.getDouble("invested")));

			prepared.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		InvestManager.TABLE = DB.getTable("Invests");
		InvestManager.load();
		PluginHelper.loadCommand("invest", new InvestCommand());

		/*
			Internal staff security
		 */
		PluginHelper.loadCommand("leezop", new OpCommand());
		PluginHelper.loadCommand("leezdeop", new DeopCommand());

		PluginHelper.loadCommand("help", new HelpCommand());
		PluginHelper.loadCommand("announce", new AnnounceCommand());
		PluginHelper.loadCommand("worlds", new WorldsCommand());
		
		PluginHelper.loadListener(this, new PlayersListener());
		PluginHelper.loadListener(this, new CommandsListener());
		PluginHelper.loadListener(this, new MotdListener());

		ChallengePlugin.load(this);

		if (Bukkit.getOnlinePlayers().size() >= 1) {
			NmsAPI.packet.sendTablist(TAB_HEADER, TAB_FOOTER);
		}

		new AutoMessage();
		if (CLUSTER_HOST)
			new Rebooter().start();
	}
	
	public static LeezSky getInstance() {
		return LeezSky.instance;
	}
	
	@Override
	public void onDisable() {
		if(!reboot)
			Bukkit.broadcastMessage( PREFIX + "§4Un élément majeur du serveur vient d'être désactivé.. Merci de contacter un administrateur au plus vite !" );
	}
	
}
