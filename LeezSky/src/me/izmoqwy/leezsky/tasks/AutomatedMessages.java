package me.izmoqwy.leezsky.tasks;

import com.google.common.collect.Lists;
import me.izmoqwy.leezsky.LeezSky;
import me.izmoqwy.leezsky.managers.SettingsManager;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class AutomatedMessages extends BukkitRunnable {

	private static final Random random = new Random();

	private final Map<Date, String> ANNOUNCEMENTS = new HashMap<Date, String>() {{
		// put(getDate(2020, 3, 15, 0, 0, 0), "Les recrutements se terminent le 15 mars (Date de lecture des candidatures) !");
	}};

	public final String[] MESSAGES = new String[]{
			"Rejoins notre discord : §bdiscord.gg/mVgduAg §3!",
	};

	public final String[] TIPS = new String[]{
			"Tu peux désactiver les messages automatiques et astuces grâce au '/settings'.",
			"Le /shop est une commande trop longue ? Utilise le diminutif : '§n/ss§e' !",
			"L'emeraude est le block le plus rentable ('/island topvalue').",
			"Le générateur à stone est améliorable au '/island shop'.",
			"Sécurisez les ressources communes contre les traîtres via le '/island vault'.",
	};

	public AutomatedMessages() {
		this.runTaskTimer(LeezSky.getInstance(), 20 * 20 * 60, 20 * 25 * 60);
		new BukkitRunnable() {

			@Override
			public void run() {
				String message = getRandomAnnouncement();
				if (message != null)
					broadcast("§cInformation §8» §d" + message);
			}

		}.runTaskTimer(LeezSky.getInstance(), 20 * 30 * 60, 20 * 50 * 60);
	}

	@Override
	public void run() {
		if (random.nextInt(16) < 5)
			broadcast("§3Message §8» §3" + getRandomMessage());
		else broadcast("§eAstuce §8» §e" + getRandomTip());
	}

	private void broadcast(String message) {
		Bukkit.getOnlinePlayers().stream()
				.filter(player -> SettingsManager.RECEIVE_AUTOMESSAGES.getState(player) == SettingsManager.SimpleToggle.ON)
				.forEach(player -> player.sendMessage(message));
	}

	private String getRandomMessage() {
		return MESSAGES[random.nextInt(MESSAGES.length)];
	}

	private String getRandomTip() {
		return TIPS[random.nextInt(TIPS.length)];
	}

	private String getRandomAnnouncement() {
		List<Date> dates = Lists.newArrayList(ANNOUNCEMENTS.keySet());
		if (dates.isEmpty())
			return null;

		Date date = dates.get(random.nextInt(dates.size()));
		if (date.before(Calendar.getInstance().getTime())) {
			ANNOUNCEMENTS.remove(date);
			return getRandomAnnouncement();
		}

		return ANNOUNCEMENTS.get(date);
	}

	private static Date getDate(int year, int month, int day, int hour, int minutes, int seconds) {
		Calendar calendar = Calendar.getInstance();
		//noinspection MagicConstant
		calendar.set(year, month - 1, day, hour, minutes, seconds);
		return calendar.getTime();
	}

}
