package me.izmoqwy.leezsky.tasks;

import lz.izmoqwy.core.api.RepeatingTask;
import me.izmoqwy.leezsky.LeezSky;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Calendar;

public class AutomatedRestart extends RepeatingTask {

	public AutomatedRestart() {
		super(5 * 60, 60);
	}

	private int countdown = 0;

	@Override
	public void run() {
		if (countdown > 0) {
			if (countdown <= 5) {
				Bukkit.broadcastMessage("§4(§cRedémarrage§4) §cLe serveur va redémarrer automatiquement dans §6" + countdown + " " + (countdown > 1 ? "minutes" : "minute") + "§c.");
				if (--countdown == 0) {
					new BukkitRunnable() {
						private int timer = 60;

						@Override
						public void run() {
							if (timer > 1) {
								timer--;
								if (timer <= 5 || timer == 10 || timer == 15 || timer == 30 || timer == 45) {
									Bukkit.broadcastMessage("§4(§cRedémarrage§4) §cLe serveur va redémarrer automatiquement dans §e" + timer + " " + (timer > 1 ? "secondes" : "seconde") + "§c.");
								}
							}
							else {
								Bukkit.broadcastMessage("§4(§cRedémarrage§4) §cLe serveur va redémarrer.");
								LeezSky.getInstance().restarting = true;
								Bukkit.getServer().shutdown();
								cancel();
							}
						}
					}.runTaskTimer(LeezSky.getInstance(), 20, 20);
					cancel();
				}
			}
		}
		else {
			Calendar calendar = Calendar.getInstance();
			if (calendar.get(Calendar.HOUR_OF_DAY) == 3 && calendar.get(Calendar.MINUTE) == 25) {
				countdown = 5;
			}
		}
	}

}
