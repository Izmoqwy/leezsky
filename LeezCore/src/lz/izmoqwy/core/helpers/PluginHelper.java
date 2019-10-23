package lz.izmoqwy.core.helpers;

import lz.izmoqwy.core.CorePrinter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class PluginHelper {

	public static void loadCommand(String name, CommandExecutor executor) {
		PluginCommand command = Bukkit.getPluginCommand(name);
		if(command != null)
			command.setExecutor(executor);
		else
			CorePrinter.warn("Command '" + name + "' need to be registred.");
	}

	public static void setTabCompleter(String name, TabCompleter tabCompleter) {
		PluginCommand command = Bukkit.getPluginCommand(name);
		if(command != null)
			command.setTabCompleter(tabCompleter);
		else
			CorePrinter.warn("Command '" + name + "' need to be registred.");
	}

	public static void loadListener(Plugin from, Listener listener) {
		Bukkit.getPluginManager().registerEvents(listener, from);
	}

	public static void performCommand(String command) {
		if (command.startsWith("/"))
			command = command.replaceFirst("/", "");
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
	}

	public static void enablePlugin(String name) {
		PluginManager manager = Bukkit.getPluginManager();
		if(!manager.isPluginEnabled(name)) {

			Plugin plugin = manager.getPlugin(name);
			if(plugin == null) CorePrinter.err("Plugin '" + name + "' can't be loaded.");
			else manager.enablePlugin(plugin);

		}else
			CorePrinter.warn("Plugin '" + name +"' is already enabled.");
	}

	public static void disablePlugin(String name) {
		PluginManager manager = Bukkit.getPluginManager();
		if(manager.isPluginEnabled(name))
			manager.disablePlugin(manager.getPlugin(name));
		else
			CorePrinter.warn("Plugin '" + name +"' isn't enabled.");
	}

	public static boolean isLoaded(String name) {
		return Bukkit.getPluginManager().isPluginEnabled(name);
	}
}
