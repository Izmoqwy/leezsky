package lz.izmoqwy.core.utils;

import net.md_5.bungee.api.ChatColor;

public class MathUtil {

	public static double roundDecimal(double number, int zeros) {
		double d = Math.pow(10, zeros);
		return Math.floor(number * d) / d;
	}

	public static String getProgressBar(int bars, int currentValue, int maxValue, char symbol, ChatColor fillColor, ChatColor leftColor) {
		int progressBars = bars * (currentValue / maxValue);
		int leftBars = (bars - progressBars);

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(fillColor);
		for (long i = 0; i < progressBars; i++) {
			stringBuilder.append(symbol);
		}

		stringBuilder.append(leftColor);
		for (long i = 0; i < leftBars; i++) {
			stringBuilder.append(symbol);
		}

		return stringBuilder.toString();
	}

}
