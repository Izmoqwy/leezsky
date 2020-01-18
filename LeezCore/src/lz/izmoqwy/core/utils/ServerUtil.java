package lz.izmoqwy.core.utils;

import lz.izmoqwy.core.self.CorePrinter;
import lz.izmoqwy.core.world.VoidChunkGenerator;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class ServerUtil {

	/*
	World
	 */
	public static void registerPersistentVoidWorld(String worldName) {
		Bukkit.createWorld(new WorldCreator(worldName).generator(new VoidChunkGenerator())
				.generateStructures(false).type(WorldType.NORMAL).environment(World.Environment.NORMAL));
	}

	/*
	Plugin-related
	*/
	public static void registerCommand(String name, CommandExecutor executor) {
		PluginCommand command = Bukkit.getPluginCommand(name);
		if (command != null) {
			command.setExecutor(executor);
		}
		else {
			CorePrinter.warn("Command '" + name + "' need to be registered.");
		}
	}

	public static void setCommandTabCompleter(String name, TabCompleter tabCompleter) {
		PluginCommand command = Bukkit.getPluginCommand(name);
		if (command != null) {
			command.setTabCompleter(tabCompleter);
		}
		else {
			CorePrinter.warn("Command '" + name + "' need to be registered.");
		}
	}

	public static void registerListeners(Plugin from, Listener... listeners) {
		for (Listener listener : listeners) {
			Bukkit.getPluginManager().registerEvents(listener, from);
		}
	}

	public static boolean isLoaded(String name) {
		return Bukkit.getPluginManager().isPluginEnabled(name);
	}

	public static void performCommand(String command) {
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.startsWith("/") ? command.replaceFirst("/", "") : command);
	}

}
