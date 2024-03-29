package me.izmoqwy.leezsky.objectives;

import com.google.common.collect.Maps;
import lz.izmoqwy.core.PlayerDataStorage;
import lz.izmoqwy.core.utils.ServerUtil;
import lz.izmoqwy.core.utils.StoreUtil;
import me.izmoqwy.leezsky.LeezSky;
import me.izmoqwy.leezsky.managers.SettingsManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;

public class ObjectiveManager {

	private static final int OBJECTIVES_SIZE;
	public static final String PATH = "objectives.";

	static {
		OBJECTIVES_SIZE = LeezObjective.values().length;
	}

	private static Map<UUID, Map.Entry<LeezObjective, BossBar>> players = Maps.newHashMap();
	private static Map<UUID, Integer> indexes = Maps.newHashMap();

	private static Map<LeezObjective, BossBar> bossBarMap = Maps.newHashMap();

	private static String getName(LeezObjective objective, int done) {
		return "§6Objectif: §e" + objective.getName() + (objective.getDue() > 1 ? " §7(" + done + "/" + objective.getDue() + ")" : "");
	}

	public static void load(JavaPlugin instance) {
		for (LeezObjective objective : LeezObjective.values()) {
			BarStyle barStyle = BarStyle.SOLID;
			switch (objective.getDue()) {
				case 6:
					barStyle = BarStyle.SEGMENTED_6;
					break;
				case 10:
					barStyle = BarStyle.SEGMENTED_10;
					break;
				case 12:
					barStyle = BarStyle.SEGMENTED_12;
					break;
				case 20:
					barStyle = BarStyle.SEGMENTED_20;
					break;
			}
			BossBar bossBar = Bukkit.getServer().createBossBar(getName(objective, 0), BarColor.BLUE, barStyle);
			bossBar.setProgress(0.D);
			bossBarMap.put(objective, bossBar);
		}

		ServerUtil.registerListeners(instance, ObjectiveListener.INSTANCE);
	}

	public static void loadPlayer(OfflinePlayer player) {
		//int index = PlayerDataStorage.get(player, PATH + "current", 0);

		// TODO: Dev only
		int index = 0;
		indexes.put(player.getUniqueId(), index);
		nextObjective(player, true);
	}

	public static LeezObjective getCurrentObjective(OfflinePlayer player) {
		if (players.containsKey(player.getUniqueId())) {
			Map.Entry<LeezObjective, BossBar> entry = players.get(player.getUniqueId());
			if (entry == null)
				return null;

			return entry.getKey();
		}
		else
			return null;
	}

	public static BossBar getPlayerObjectiveBB(OfflinePlayer player) {
		if (players.containsKey(player.getUniqueId())) {
			Map.Entry<LeezObjective, BossBar> entry = players.get(player.getUniqueId());
			if (entry == null) {
				return nextObjective(player, false);
			}

			if (entry.getValue() != null)
				return entry.getValue();
			else
				return bossBarMap.get(entry.getKey());
		}
		else
			return nextObjective(player, false);
	}

	protected static BossBar nextObjective(OfflinePlayer player, boolean fromLoad) {
		final int index = indexes.getOrDefault(player.getUniqueId(), -1) + (fromLoad ? 0 : 1);

		LeezObjective objective = OBJECTIVES_SIZE > index ? LeezObjective.values()[Math.max(index, 0)] : null;

		indexes.put(player.getUniqueId(), index);
		if (!fromLoad && index > 0) {
			PlayerDataStorage.set(player, PATH + "current", index);
			PlayerDataStorage.set(player, PATH + "progress", 0);
			PlayerDataStorage.save(player);
		}

		if (objective == null) {
			players.remove(player.getUniqueId());
			return null;
		}
		else {
			BossBar bossBar = null;
			if (objective.getDue() > 1) {
				BossBar model = bossBarMap.get(objective);
				bossBar = Bukkit.getServer().createBossBar(model.getTitle(), model.getColor(), model.getStyle());

				int done = PlayerDataStorage.get(player, PATH + "progress", 0);
				bossBar.setTitle(getName(objective, done));
				bossBar.setProgress(StoreUtil.mapValue(done, 0, objective.getDue(), 0, 1));
			}
			else if (fromLoad && PlayerDataStorage.get(player, PATH + "progress", 0) > 0) {
				if (player.isOnline()) {
					player.getPlayer().sendMessage(LeezSky.PREFIX + "§cVos objectifs semblaient corrompu, nous les avons remis à zéro.");
				}
				indexes.remove(player.getUniqueId());
				PlayerDataStorage.set(player, PATH + "current", 0);
				PlayerDataStorage.set(player, PATH + "progress", 0);
				PlayerDataStorage.save(player);
				return nextObjective(player, false);
			}
			Map.Entry<LeezObjective, BossBar> entry = Maps.immutableEntry(objective, bossBar);
			players.put(player.getUniqueId(), entry);

			return getPlayerObjectiveBB(player);
		}
	}

	public static void addToBB(Player player) {
		if (SettingsManager.OBJECTIVE_BOSSBAR.getState(player) == SettingsManager.SimpleToggle.ON && players.containsKey(player.getUniqueId())) {
			Map.Entry<LeezObjective, BossBar> entry = players.get(player.getUniqueId());
			if (entry == null)
				return;

			if (entry.getValue() != null) {
				entry.getValue().addPlayer(player);
				return;
			}
			bossBarMap.get(entry.getKey()).addPlayer(player);
		}
	}

	public static void removeFromBB(Player player) {
		if (players.containsKey(player.getUniqueId())) {
			Map.Entry<LeezObjective, BossBar> entry = players.get(player.getUniqueId());
			if (entry == null)
				return;

			if (entry.getValue() != null) {
				entry.getValue().removePlayer(player);
				return;
			}
			bossBarMap.get(entry.getKey()).removePlayer(player);
		}
	}

	public static void complete(LeezObjective objective, Player player) {
		complete(objective, player, 1);
	}

	public static void complete(LeezObjective objective, Player player, int amount) {
		if (objective.getDue() > 1) {
			BossBar bossBar = getPlayerObjectiveBB(player);

			int progress = PlayerDataStorage.get(player, PATH + "progress", 0) + amount;
			if (progress < objective.getDue()) {
				bossBar.setProgress(bossBar.getProgress() + StoreUtil.mapValue(amount, 0, objective.getDue(), 0, 1));
				bossBar.setTitle(getName(objective, progress));

				PlayerDataStorage.set(player, PATH + "progress", progress);
				PlayerDataStorage.save(player);
				return;
			}
		}

		removeFromBB(player);

		nextObjective(player, false);
		LeezObjective newObjective = getCurrentObjective(player);

		double xp = Math.pow(objective.ordinal() + 5, 2);
		xp /= 1.9D;
		int realXp = (int) Math.floor(xp * 3);
		realXp = realXp - (realXp % 25);
		if (realXp == 0)
			realXp = 10;

		player.giveExp(realXp);

		player.sendMessage(" ");
		player.sendMessage(LeezSky.PREFIX + "§aObjectif accomplit, vous avez gagné §2" + realXp + "xp §a!");
		if (newObjective != null)
			player.sendMessage(LeezSky.PREFIX + "§6Nouvel objectif: §e" + newObjective.getName());
		else
			player.sendMessage(LeezSky.PREFIX + "§2Vous avez terminé tous les objectifs actuellement disponibles, d'autres arriveront très prochainement !");
		player.sendMessage(" ");

		player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);

		if (newObjective != null)
			addToBB(player);
	}

}
