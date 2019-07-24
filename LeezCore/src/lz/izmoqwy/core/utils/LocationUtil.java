package lz.izmoqwy.core.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;

public class LocationUtil {

	public static Location getSafeLocation(Location origin) {
		if (origin.getBlockY() <= 1) return null;

		Location to = origin;
		Block block;
		while ((block = to.getBlock()) != null && !block.getType().isSolid()) {
			if (to.getBlockY() <= 1) return null;
			to.setY(to.getY() - 1f);
		}

		to.setY(to.getY() + 1.1f);
		return to;
	}

	public static String fromPath(String from, String path) {
		return from != null && !from.endsWith(".") ? from + "." + path : path;
	}

	public static void yamlFullSave(YamlConfiguration config, Location location, String path) {
		config.set(fromPath(path, "world"), location.getWorld().getName());
		config.set(fromPath(path, "x"), Math.floor(location.getX() * 1000) / 1000);
		config.set(fromPath(path, "y"), Math.floor(location.getY() * 1000) / 1000);
		config.set(fromPath(path, "z"), Math.floor(location.getZ() * 1000) / 1000);
		config.set(fromPath(path, "yaw"), Math.floor(location.getYaw() * 10) / 10);
		config.set(fromPath(path, "pitch"), Math.floor(location.getPitch() * 10) / 10);
	}

	public static Location yamlFullLoad(YamlConfiguration config, String from) {
		String[] all_paths = new String[]{"world", "x", "y", "z", "yaw", "pitch"};
		for (String path : all_paths) {
			if (!config.isSet(fromPath(from, path)))
				return null;
		}

		double x = config.getDouble(fromPath(from, "x")), y = config.getDouble(fromPath(from, "y")), z = config.getDouble(fromPath(from, "z"));
		double yaw = config.getDouble(fromPath(from, "yaw")), pitch = config.getDouble(fromPath(from, "pitch"));

		return new Location(Bukkit.getWorld(config.getString("npc.world")), x, y, z, (float) yaw, (float) pitch);
	}

}
