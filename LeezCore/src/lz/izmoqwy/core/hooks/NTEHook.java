package lz.izmoqwy.core.hooks;

import com.nametagedit.plugin.NametagEdit;
import com.nametagedit.plugin.NametagHandler;
import com.nametagedit.plugin.NametagManager;
import com.nametagedit.plugin.api.INametagApi;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class NTEHook {

	public INametagApi getApi() {
		return NametagEdit.getApi();
	}

	public void forceReload() {
		try {
			Field field = getApi().getClass().getDeclaredField("handler");
			field.setAccessible(true);

			NametagHandler handler = (NametagHandler) field.get(getApi());

			// Reset tags
			NametagManager manager = handler.getNametagManager();
			Method method = manager.getClass().getDeclaredMethod("reset");
			method.setAccessible(true);
			method.invoke(manager);

			// Re-apply tags
			handler.applyTags();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
