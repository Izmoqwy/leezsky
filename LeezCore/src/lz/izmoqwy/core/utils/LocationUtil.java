package lz.izmoqwy.core.utils;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;

import static lz.izmoqwy.core.utils.MathUtil.roundDecimal;

public class LocationUtil {

	public static Location getSafeLocation(Location location) {
		Preconditions.checkNotNull(location);

		if (location.getBlockY() <= 1)
			return null;

		Location to = location.clone();

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

	public static void saveInYaml(YamlConfiguration config, Location location, String to_path) {
		Preconditions.checkNotNull(config);
		Preconditions.checkNotNull(location);

		config.set(fromPath(to_path, "world"), location.getWorld().getName());
		config.set(fromPath(to_path, "x"), roundDecimal(location.getX(), 2));
		config.set(fromPath(to_path, "y"), roundDecimal(location.getY(), 2));
		config.set(fromPath(to_path, "z"), roundDecimal(location.getZ(), 2));
		config.set(fromPath(to_path, "yaw"), (float) roundDecimal(location.getX(), 1));
		config.set(fromPath(to_path, "pitch"), (float) roundDecimal(location.getX(), 1));
	}

	public static Location loadFromYaml(YamlConfiguration config, String from_path) {
		Preconditions.checkNotNull(config);

		String[] all_paths = new String[]{"world", "x", "y", "z", "yaw", "pitch"};
		for (String path : all_paths) {
			if (!config.isSet(fromPath(from_path, path)))
				return null;
		}

		double x = config.getDouble(fromPath(from_path, "x")), y = config.getDouble(fromPath(from_path, "y")), z = config.getDouble(fromPath(from_path, "z"));
		double yaw = config.getDouble(fromPath(from_path, "yaw")), pitch = config.getDouble(fromPath(from_path, "pitch"));

		return new Location(Bukkit.getWorld(config.getString(fromPath(from_path, "world"), "world")), x, y, z, (float) yaw, (float) pitch);
	}

	public static String inlineSerialize(Location location, boolean world, boolean facing) {
		Preconditions.checkNotNull(location);

		StringBuilder stringBuilder = new StringBuilder();
		if (world) {
			stringBuilder.append(location.getWorld().getName());
		}

		stringBuilder.append(roundDecimal(location.getX(), 2)).append(":");
		stringBuilder.append(roundDecimal(location.getY(), 2)).append(":");
		stringBuilder.append(roundDecimal(location.getZ(), 2)).append(":");

		if (facing) {
			stringBuilder.append(roundDecimal(location.getYaw(), 1)).append(":");
			stringBuilder.append(roundDecimal(location.getPitch(), 1)).append(":");
		}
		return stringBuilder.toString();
	}

	public static Location inlineParse(String serialized, World fallbackWorld) {
		Preconditions.checkNotNull(serialized);

		final String[] parts = serialized.split(":");
		if (parts.length < 3 || parts.length > 6)
			return null;

		int index = 0;

		World world = parts.length == 4 || parts.length == 6 ? Bukkit.getWorld(parts[index++]) : null;
		if (world == null)
			world = fallbackWorld;

		double x = Double.parseDouble(parts[index++]),
				y = Double.parseDouble(parts[index++]),
				z = Double.parseDouble(parts[index++]);

		if (parts.length < 5)
			return new Location(world, x, y, z);

		float yaw = Float.parseFloat(parts[index++]),
				pitch = Float.parseFloat(parts[index]);

		return new Location(world, x, y, z, yaw, pitch);
	}

}
