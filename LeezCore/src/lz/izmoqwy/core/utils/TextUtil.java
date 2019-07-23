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
}
