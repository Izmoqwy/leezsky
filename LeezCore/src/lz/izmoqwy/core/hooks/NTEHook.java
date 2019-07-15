package lz.izmoqwy.core.hooks;

import com.nametagedit.plugin.NametagEdit;
import com.nametagedit.plugin.NametagHandler;
import com.nametagedit.plugin.NametagManager;
import com.nametagedit.plugin.api.INametagApi;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NTEHook {

	public INametagApi getApi() {
		return NametagEdit.getApi();
	}

	public boolean forceReload() {
		try {
			Field field = getApi().getClass().getDeclaredField("handler");
			field.setAccessible(true);

			NametagHandler handler = (NametagHandler)field.get(getApi());

			// Reset tags
			NametagManager manager = handler.getNametagManager();
			Method method = manager.getClass().getDeclaredMethod("reset");
			method.setAccessible(true);
			method.invoke(manager);

			// Re-aply tags
			handler.applyTags();

			return true;
		}
		catch(NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
			ex.printStackTrace();
			return false;
		}
	}

}
