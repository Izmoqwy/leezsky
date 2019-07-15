package lz.izmoqwy.core.utils;

import net.md_5.bungee.api.ChatColor;

public class ProgressbarUtil {

	public static String getProgressBar(int current, int max, int totalBars, char symbol, ChatColor done, ChatColor left) {
		return getProgressBar(((Integer) current).longValue(), ((Integer) max).longValue(), totalBars, symbol, done, left);
	}

	public static String getProgressBar(long current, long max, int totalBars, char symbol, ChatColor done, ChatColor left) {
		float percent = (float) current / max;
		long progressBars = (long) ((long) totalBars * percent);
		long leftOver = (totalBars - progressBars);

		StringBuilder sb = new StringBuilder();
		sb.append(done);
		for (long i = 0; i < progressBars; i++) {
			sb.append(symbol);
		}
		sb.append(left);
		for (long i = 0; i < leftOver; i++) {
			sb.append(symbol);
		}
		return sb.toString();
	}

}
