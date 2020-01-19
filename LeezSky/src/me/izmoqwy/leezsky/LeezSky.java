package me.izmoqwy.leezsky;

import com.google.common.collect.Maps;
import lombok.Getter;
import lz.izmoqwy.core.api.database.SQLDatabase;
import lz.izmoqwy.core.api.database.SQLite;
import lz.izmoqwy.core.nms.NMS;
import lz.izmoqwy.core.self.LeezCore;
import lz.izmoqwy.core.utils.ServerUtil;
import me.izmoqwy.leezsky.challenges.ChallengePlugin;
import me.izmoqwy.leezsky.commands.HelpCommand;
import me.izmoqwy.leezsky.commands.InvestCommand;
import me.izmoqwy.leezsky.commands.ObjectiveCommand;
import me.izmoqwy.leezsky.commands.SettingsCommand;
import me.izmoqwy.leezsky.commands.management.AnnounceCommand;
import me.izmoqwy.leezsky.commands.management.DeopCommand;
import me.izmoqwy.leezsky.commands.management.OpCommand;
import me.izmoqwy.leezsky.commands.management.WorldsCommand;
import me.izmoqwy.leezsky.listeners.CommandsListener;
import me.izmoqwy.leezsky.listeners.MotdListener;
import me.izmoqwy.leezsky.listeners.PlayersListener;
import me.izmoqwy.leezsky.listeners.SpawnListener;
import me.izmoqwy.leezsky.managers.InvestManager;
import me.izmoqwy.leezsky.managers.ScoreboardManager;
import me.izmoqwy.leezsky.objectives.ObjectiveManager;
import me.izmoqwy.leezsky.tasks.AutoMessage;
import me.izmoqwy.leezsky.tasks.Rebooter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class LeezSky extends JavaPlugin {

	@Getter
	private static LeezSky instance;
	public boolean rebooting = false;

	public static final boolean CLUSTER_HOST = false;
	public static final String PREFIX = LeezCore.PREFIX;
	public static final String
			TAB_HEADER = "§8┅⊰ §6PLAY.LEEZSKY.FR §8⊱┅\n",
			TAB_FOOTER = "\n§8┅⊰ §ediscord.gg/X78wMsE §8⊱┅";

	public static SQLDatabase DB;

	@Override
	public void onEnable() {
		instance = this;

		final String initMessage =
				"  _                                  _            \n" +
						" | |                                | |           \n" +
						" | |        ___    ___   ____  ___  | | __  _   _ \n" +
						" | |       / _ \\  / _ \\ |_  / / __| | |/ / | | | |\n" +
						" | |____  |  __/ |  __/  / /  \\__ \\ |   <  | |_| |\n" +
						" |______|  \\___|  \\___| /___| |___/ |_|\\_\\  \\__, |\n" +
						"                                             __/ |\n" +
						"                                            |___/ ";
		final String[] lines = initMessage.split("\n");
		for (String line : lines) {
			Bukkit.getConsoleSender().sendMessage("§6» " + line);
		}

		ServerUtil.registerPersistentVoidWorld("Spawn");
		World world = Bukkit.getWorld("Spawn");
		if (world != null) {
			ServerUtil.registerListeners(this, new SpawnListener(world));
		}

		DB = new SQLite("LeezSky", this, new File(getDataFolder(), "storage.db"));

		try {
			DB.connect();
			DB.execute("CREATE TABLE IF NOT EXISTS `Invests` (`uuid` VARCHAR(36) UNIQUE, `invested` INTEGER(11,2),`at` INTEGER(11));");

			PreparedStatement prepared = DB.prepare("SELECT uuid, invested, at FROM Invests WHERE invested != -1");
			ResultSet rs = prepared.executeQuery();

			while (rs.next())
				InvestManager.map.put(UUID.fromString(rs.getString("uuid")), Maps.immutableEntry(rs.getLong("at"), rs.getDouble("invested")));

			prepared.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}

		InvestManager.TABLE = DB.getTable("Invests");
		InvestManager.load();
		ServerUtil.registerCommand("invest", new InvestCommand());

		ServerUtil.registerCommand("settings", new SettingsCommand());
		ScoreboardManager.load(this);

		ServerUtil.registerCommand("objective", new ObjectiveCommand());

		ServerUtil.registerCommand("help", new HelpCommand());
		ServerUtil.registerCommand("announce", new AnnounceCommand());
		ServerUtil.registerCommand("worlds", new WorldsCommand());

		ServerUtil.registerCommand("leezop", new OpCommand());
		ServerUtil.registerCommand("leezdeop", new DeopCommand());

		ServerUtil.registerListeners(this,
				new PlayersListener(),
				new CommandsListener(),
				new MotdListener()
		);

		ChallengePlugin.load(this);

		if (Bukkit.getOnlinePlayers().size() >= 1) {
			NMS.packet.sendGlobalTablist(TAB_HEADER, TAB_FOOTER);
		}

		new AutoMessage();
		if (CLUSTER_HOST)
			new Rebooter().start();

		final boolean useScoreboard = NMS.scoreboard != null;
		if (useScoreboard)
			getLogger().info("Able to use scoreboards!");

		ObjectiveManager.load(this);
		for (Player player : Bukkit.getOnlinePlayers()) {
			ObjectiveManager.loadPlayer(player);
			ObjectiveManager.addToBB(player);

			if (useScoreboard)
				ScoreboardManager.createScoreboard(player);
		}
	}

	@Override
	public void onDisable() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			ObjectiveManager.removeFromBB(player);
		}
		ScoreboardManager.clear();

		if (!rebooting)
			Bukkit.broadcastMessage(PREFIX + "§4Un élément majeur du serveur vient d'être désactivé.. Merci de contacter un administrateur au plus vite !");
	}

}
