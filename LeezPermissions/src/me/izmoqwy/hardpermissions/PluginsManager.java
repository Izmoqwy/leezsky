package me.izmoqwy.hardpermissions;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class PluginsManager implements Listener {

	private static List<Permission> allPermissions = Lists.newArrayList();

	protected static void calculateAllPermissions() {
		allPermissions.clear();
		for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
			allPermissions.addAll(plugin.getDescription().getPermissions());
		}
	}

	public static List<Permission> getAllPermissions() {
		return allPermissions;
	}

	@EventHandler
	public void onPluginLoad(PluginEnableEvent event) {
		calculateAllPermissions();
	}

}
