package me.izmoqwy.leezsky.challenges;

import com.google.common.collect.Lists;
import lz.izmoqwy.core.Economy;
import lz.izmoqwy.core.PlayerDataStorage;
import lz.izmoqwy.core.utils.ItemUtil;
import lz.izmoqwy.core.utils.ServerUtil;
import me.izmoqwy.leezsky.LeezSky;
import me.izmoqwy.leezsky.challenges.obj.Categorie;
import me.izmoqwy.leezsky.challenges.obj.Challenge;
import me.izmoqwy.leezsky.challenges.obj.Challenges;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.List;

public class ChallengePlugin {

	public static final ChallengePlugin instance = new ChallengePlugin();
	static final String PREFIX = LeezSky.PREFIX;
	private static final String PREFIX_BROADCAST = "§3✸ ", c_ = "challenges.";

	private static File dataFolder;

	public static void load(Plugin from) {
		ServerUtil.registerCommand("challenges", new ChallengeCommand());
		ServerUtil.registerListeners(from, new ChallengesListener(instance));
	}

	public boolean canAccess(Player player, Categorie categorie) {
		return getActual(player).getPower() >= categorie.getDifficulty().getPower();
	}

	void done(Player player, Challenge chall) {
		if (!isDone(player, chall)) {
			Economy.deposit(player, chall.getRewardMoney());
			for (ItemStack reward : chall.getRewards()) {
				ItemUtil.giveItems(player, reward);
			}
			Bukkit.broadcastMessage(ChallengePlugin.PREFIX_BROADCAST + "§aBravo §E" + player.getName() + " §apour avoir réussi le défi §2" + chall.getName() + " §a!");

			YamlConfiguration yaml = PlayerDataStorage.yamlNoThrow(player);

			List<String> completed = Lists.newArrayList();
			if (yaml.getStringList(c_ + "done") != null) completed = yaml.getStringList(c_ + "done");
			completed.add(chall.getName());
			yaml.set(c_ + "done", completed);
			Categorie categorie = chall.getCategorie();
			int nb = getCompleted(player, categorie) + 1;
			yaml.set(c_ + categorie.getName() + ".completed", nb);

			if (getRest(player, categorie) == 0) {
				int secondIndex = nb == 0 ? 1 : (nb == 2 ? 3 : (nb == 4 ? 5 : (nb == 1 ? 0 : (nb == 3 ? 2 : (nb == 5 ? 4 : 6)))));
				Categorie cat = Challenges.categories.get(secondIndex);
				if (getRest(player, cat) == 0) {
					int current = cat.getDifficulty() == Difficulty.EASY ? 0 : (cat.getDifficulty() == Difficulty.MEDIUM ? 1 : (cat.getDifficulty() == Difficulty.HARD ? 2 : 3));
					if (current == 3) {
						Bukkit.broadcastMessage(" ");
						Bukkit.broadcastMessage("§c§k<-> Exploit <->");
						Bukkit.broadcastMessage(ChallengePlugin.PREFIX_BROADCAST + "§2Bravo §6" + player.getName() + " §2qui a terminé tout les défis !!!");
						Bukkit.broadcastMessage(" ");
					}
					else {
						yaml.set(c_ + "current", current + 1);
						Bukkit.broadcastMessage(ChallengePlugin.PREFIX_BROADCAST + "§aBravo §E" + player.getName() + " §aqui passe au niveau de difficulté " + getActual(player).toString() + " §a!");
					}
				}
				else {
					yaml.set(c_ + "currentIndex", secondIndex);
				}
			}

			PlayerDataStorage.saveNoThrow(player);
		}
	}

	public boolean isDone(Player player, Challenge chall) {
		List<String> completed = PlayerDataStorage.get(player, c_ + "done");
		if(completed == null)
			return false;
		return completed.contains(chall.getName());
	}

	public int getIndex(Player player) {
		return PlayerDataStorage.yamlNoThrow(player).getInt(c_ + "currentIndex", 0);
	}

	public Difficulty getActual(Player player) {
		return Difficulty.values()[PlayerDataStorage.get(player, c_ + "current", 0)];
	}

	private int getCompleted(Player player, Categorie categorie) {
		return PlayerDataStorage.get(player,c_ + categorie.getName() + ".completed", 0);
	}

	public int getRest(Player player, Categorie categorie) {
		return categorie.getChallenges().length - getCompleted(player, categorie);
	}

	/*
	 * FUNCs FOR CHALLENGES
	 */
	private int getKilledMobs(Player player) {
		return player.getStatistic(Statistic.MOB_KILLS);
	}

	public boolean impossible(Player player) {
		return false;
	}

	/*
	 * METHODS
	 */

	public boolean hasKill80Mobs(Player player) {
		return getKilledMobs(player) >= 80;
	}
}
