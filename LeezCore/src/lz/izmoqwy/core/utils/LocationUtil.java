package lz.izmoqwy.core.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
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

	private static String fromPath(String from, String path) {
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

		return new Location(Bukkit.getWorld(config.getString(fromPath(from, "world"), "world")), x, y, z, (float) yaw, (float) pitch);
	}

	private static double r(double d, int i) {
		return Math.floor(d * i) / i;
	}

	public static String loc2str(final Location l, final boolean world) {
		if (l == null)
			return "";

		return (world ? l.getWorld().getName() + ":" : "") + r(l.getX(), 100) + ":" + r(l.getY(), 100) + ":" + r(l.getZ(), 100) + ":" + r(l.getYaw(), 10) + ":" + r(l.getPitch(), 10);
	}

	public static Location str2loc(final String s) {
		if (s == null || s.trim().isEmpty())
			return null;

		final String[] parts = s.split(":");
		if (parts.length == 4) {
			final World w = Bukkit.getServer().getWorld(parts[0]);
			final double x = Double.parseDouble(parts[1]);
			final double y = Double.parseDouble(parts[2]);
			final double z = Double.parseDouble(parts[3]);
			return new Location(w, x, y, z);
		}
		else if (parts.length == 6) {
			final World w = Bukkit.getServer().getWorld(parts[0]);
			final double x = Double.parseDouble(parts[1]);
			final double y = Double.parseDouble(parts[2]);
			final double z = Double.parseDouble(parts[3]);
			final double pitch = Double.parseDouble(parts[4]);
			final double yaw = Double.parseDouble(parts[5]);
			return new Location(w, x, y, z, (float) yaw, (float) pitch);
		}

		return null;
	}

	public static Location str2loc(final String s, final String world) {
		if (s == null || s.trim().isEmpty())
			return null;

		final String[] parts = s.split(":");
		if (parts.length == 5) {
			final double x = Double.parseDouble(parts[0]);
			final double y = Double.parseDouble(parts[1]);
			final double z = Double.parseDouble(parts[2]);
			final double pitch = Double.parseDouble(parts[3]);
			final double yaw = Double.parseDouble(parts[4]);
			return new Location(Bukkit.getWorld(world), x, y, z, (float) yaw, (float) pitch);
		}
		else if (parts.length == 3) {
			final double x = Double.parseDouble(parts[0]);
			final double y = Double.parseDouble(parts[1]);
			final double z = Double.parseDouble(parts[2]);
			return new Location(Bukkit.getWorld(world), x, y, z);
		}
		return null;
	}

}
