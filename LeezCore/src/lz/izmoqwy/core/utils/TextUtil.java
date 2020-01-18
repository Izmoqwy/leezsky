package lz.izmoqwy.core.utils;

import litebans.N;
import lz.izmoqwy.core.FireAction;

import java.util.List;

public class TextUtil {

	public static String getFinalArg(String[] args, int start) {
		StringBuilder builder = new StringBuilder();

		for (int i = start; i < args.length; i++) {
			if (i != start) builder.append(" ");
			builder.append(args[i]);
		}
		return builder.toString();
	}

	public static <T> String iterate(List<T> objects, FireAction.ObjToStr<T> objToStr, String objPrefix, String objSeparation) {
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < objects.size(); i++) {
			if (i > 0) builder.append(objSeparation);
			builder.append(objPrefix).append(objToStr.fire(objects.get(i)));
		}
		return builder.toString();
	}

	public static <T> String iterate(List<T> objects, FireAction.ObjToStr<T> objToStr, String objPrefix, String objSeparation, String lastSeparation) {
		String string = iterate(objects, objToStr, objPrefix, objSeparation);

		final int index = string.lastIndexOf(objSeparation);
		if (index == -1)
			return string;
		return string.substring(0, index) +
				lastSeparation +
				string.substring(index + objSeparation.length());
	}

	public static String capitalizeFirstLetter(String original) {
		if (original == null || original.length() == 0) {
			return original;
		}
		return original.substring(0, 1).toUpperCase() + original.substring(1);
	}

	/*
	Numbers
	 */
	private static String removeTrailing(String string) {
		if (string.endsWith(".00"))
			string = string.substring(0, string.length() - 3);
		else if (string.endsWith(".0"))
			string = string.substring(0, string.length() - 2);

		if (string.endsWith("0"))
			string = string.substring(0, string.length() - 1);

		return string;
	}

	public static String humanReadableNumber(long number) {
		if (number < 1e3)
			return Long.toString(number);

		String suffix = null;
		long divider = 1;

		if (number >= 1e12) {
			suffix = "T";
			divider = (long) 1e12;
		}
		else if (number >= 1e9) {
			suffix = "G";
			divider = (long) 1e9;
		}
		else if (number >= 1e6) {
			suffix = "M";
			divider = (long) 1e6;
		}
		else if (number >= 1e3) {
			suffix = "K";
			divider = (long) 1e3;
		}

		return removeTrailing(Double.toString(Math.floor(number / (divider / 100f)) / 100)) + suffix;
	}

	public static String humanReadableNumber(double number) {
		number = MathUtil.roundDecimal(number, 2);
		if (number < 1e3)
			return Double.toString(number);

		String suffix = null;
		double divider = 1;

		if (number >= 1e12) {
			suffix = "T";
			divider = 1e12;
		}
		else if (number >= 1e9) {
			suffix = "G";
			divider = 1e9;
		}
		else if (number >= 1e6) {
			suffix = "M";
			divider = 1e6;
		}
		else if (number >= 1e3) {
			suffix = "K";
			divider = 1e3;
		}

		return removeTrailing(Double.toString(Math.floor(number / (divider / 100f)) / 100)) + suffix;
	}

}
