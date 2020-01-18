package lz.izmoqwy.core.utils;

import java.lang.reflect.Field;

public class ReflectionUtil {

	public static Field getField(Class<?> aClass, String fieldName) {
		Field field = null;
		try {
			field = aClass.getDeclaredField(fieldName);
			field.setAccessible(true);
		}
		catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		return field;
	}

}
