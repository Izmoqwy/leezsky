package me.izmoqwy.leezsky.tasks;

import me.izmoqwy.leezsky.LeezSky;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class AutoMessage extends BukkitRunnable {

	private static Random random = new Random();
	public static Map<Date, String> MESSAGES_MaxDated = new HashMap<Date, String>() {
		private static final long serialVersionUID = 7123143157376827815L;

		{
			//put(getDate(2019, 3, 15, 0, 0, 0), "Les recrutements se terminent le 15 septembre (Date de lecture des candidatures) !");
		}
	};
	public static String[] MESSAGES = new String[]{
			"Rejoins notre discord : §bdiscord.gg/mVgduAg §3!",
	};

	public static String[] TIPS = new String[]{
			"Tu peux désactiver les messages automatiques et astuces grâce au /settings.",
			"Le /shop est une commande trop longue ? Utilise le diminutif : §n/ss§e !",
			"Vendre via le shop te fait perdre du temps ? Utilise les coffres vendeurs ! §7(/shop -> Spéciaux)",
			"L'emeraude est le block le plus rentable (/island topvalue).",
			"Le générateur à stone est améliorable au /island shop.",
			"Sécurise tes ressources contre les trahisseurs via le /island vault.",
	};

	public AutoMessage() {
		this.runTaskTimer(LeezSky.getInstance(), 15 * 20 * 60, 10 * 20 * 60);
		new BukkitRunnable() {

			@Override
			public void run() {

				String message = getRandomDatedMessage();
				if (message != null) Bukkit.broadcastMessage("§cInformation §8» §d" + message);

			}

		}.runTaskTimer(LeezSky.getInstance(), 10 * 20, (30 * 20 * 60) + 54000);
	}

	@Override
	public void run() {
		if (random.nextInt(16) < 5)
			Bukkit.broadcastMessage("§3Message §8» §3" + getRandomMessage());
		else Bukkit.broadcastMessage("§eAstuce §8» §e" + getRandomTip());
	}

	private static String getRandomMessage() {
		return MESSAGES[random.nextInt(MESSAGES.length)];
	}

	private static String getRandomTip() {
		return TIPS[random.nextInt(TIPS.length)];
	}

	private static String getRandomDatedMessage() {
		Date[] dates = MESSAGES_MaxDated.keySet().toArray(new Date[]{});
		if (!(dates.length >= 1)) return null;
		Date date = dates[random.nextInt(dates.length)], date1 = Calendar.getInstance().getTime();
		if (date.before(date1)) {
			MESSAGES_MaxDated.remove(date);
		}
		else return MESSAGES_MaxDated.get(date);
		return getRandomDatedMessage();
	}

	private static Date getDate(int year, int month, int day, int hour, int minutes, int seconds) {
		Calendar cal = Calendar.getInstance();
		cal.set(year, month - 1, day, hour, minutes);
		cal.set(Calendar.SECOND, seconds);
		return cal.getTime();
	}
}
