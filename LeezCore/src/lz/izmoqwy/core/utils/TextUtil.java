package lz.izmoqwy.core.utils;

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

	public static String readbleNumber(long amount) {
		if (amount < 1e3)
			return Long.toString(amount);

		String letter = null;
		long divider = 1;
		if (amount > 1e12) {
			letter = "T";
			divider = (long) 1e12;
		}
		else if (amount >= 1e9) {
			letter = "G";
			divider = (long) 1e9;
		}
		else if (amount >= 1e6) {
			letter = "M";
			divider = (long) 1e6;
		}
		else if (amount >= 1e3) {
			letter = "K";
			divider = (long) 1e3;
		}
		String str = Double.toString(Math.floor(amount / (divider / 100)) / 100);
		if (str.endsWith(".00"))
			str = str.substring(0, str.length() - 3);
		if (str.endsWith(".0"))
			str = str.substring(0, str.length() - 2);
		if (str.endsWith("0"))
			str = str.substring(0, str.length() - 1);
		return str + letter;
	}
}
