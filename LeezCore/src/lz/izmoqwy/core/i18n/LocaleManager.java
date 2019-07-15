package lz.izmoqwy.core.i18n;

import com.google.common.collect.Maps;
import lz.izmoqwy.core.CorePrinter;
import lz.izmoqwy.core.LeezCore;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class LocaleManager {

	private static Map<Plugin, Class<? extends i18nLocale>> pluginMap = Maps.newHashMap();

	public static <E extends Enum<E>> void register(Plugin plugin, Class<? extends i18nLocale> locale) {
		pluginMap.put(plugin, locale);
		reload(plugin);
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	private static void reload(Plugin plugin) {
		Class<? extends  i18nLocale> clazz = pluginMap.get(plugin);
		if (clazz == null) {
			CorePrinter.warn("Cannot (re)load {0}''s messages due to invalid locale class passed.", plugin.getName());
			return;
		}

		File file = getPluginMessages(plugin);
		if (!file.exists()) {
			if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
			try {
				file.createNewFile();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

		i18nLocale[] values = clazz.getEnumConstants();
		for (i18nLocale value : values) {
			final String path = value.getEnumName().toLowerCase().replace('_', '.');
			if (yaml.isSet(path)) {
				if (yaml.isString(path)) {
					value.set(yaml.getString(path).replace('&', '§'));
				}
				else {
					CorePrinter.print("Invalid message in message file \"{0}\" (path: {1})", file.getName(), path);
				}
			}
			else
				yaml.set(path, value.getSavableMessage().replace('§', '&'));
		}

		try {
			yaml.save(file);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void reloadMessages() {
		for (Plugin plugin : pluginMap.keySet()) {
			reload(plugin);
		}
	}

	private static File getPluginMessages(Plugin plugin) {
		return new File(LeezCore.instance.getDataFolder(), "messages/" + plugin.getName().toLowerCase() + ".yml");
	}

}
