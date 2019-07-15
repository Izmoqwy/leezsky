package lz.izmoqwy.core.helpers;

import java.lang.reflect.Field;

public class ReflectorHelper {

	public static Field getField(Class<?> clazz, String name) {
		try {
			Field field = clazz.getDeclaredField(name);
			field.setAccessible(true);
			return field;
		}
		catch (NoSuchFieldException | SecurityException ex) { ex.printStackTrace(); return null; }
	}
}
