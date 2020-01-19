package lz.izmoqwy.core.api;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class RepeatingTask extends BukkitRunnable {

	private final long start, repeat;

	public RepeatingTask(long start, long repeat) {
		this.start = start * 20;
		this.repeat = repeat * 20;
	}

	public void start(Plugin plugin) {
		this.runTaskTimer(plugin, start, repeat);
	}

	public void startAsync(Plugin plugin) {
		this.runTaskTimerAsynchronously(plugin, start, repeat);
	}

}
