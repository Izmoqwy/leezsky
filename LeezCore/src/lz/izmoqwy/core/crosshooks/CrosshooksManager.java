package lz.izmoqwy.core.crosshooks;

import com.google.common.collect.Maps;
import org.bukkit.plugin.Plugin;

import java.util.Map;

public class CrosshooksManager {

	private static Map<String, CrossHook> hooks = Maps.newHashMap();

	public static void registerHook(Plugin plugin, CrossHook hook) {
		hooks.put(plugin.getName(), hook);
	}

	public static boolean isPluginRegistred(String name) {
		return hooks.containsKey(name);
	}

	public static boolean isHookRegistred(CrossHook hook) {
		return hooks.values().contains(hook);
	}

	public static <Hook> Hook get(String pluginName) {
		return (Hook) hooks.get(pluginName);
	}

	public static <Hook> Hook get(String pluginName, Class<? extends CrossHook> checkType) {
		return checkType.isAssignableFrom(hooks.get(pluginName).getClass()) ? (Hook) hooks.get(pluginName) : null;
	}

}
