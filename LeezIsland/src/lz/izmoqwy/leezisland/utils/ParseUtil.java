package lz.izmoqwy.leezisland.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class ParseUtil {

	public static String loc2str(final Location l) {
		if (l == null) {
			return "";
		}

		return l.getWorld().getName() + ":" + l.getX() + ":" + l.getY() + ":" + l.getZ();
	}

	public static Location str2loc(final String s) {

		if (s == null || s.trim().isEmpty()) {
			return null;
		}

		final String[] parts = s.split(":");
		if (parts.length == 4) {

			final World w = Bukkit.getServer().getWorld(parts[0]);
			final double x = Double.parseDouble(parts[1]);
			final double y = Double.parseDouble(parts[2]);
			final double z = Double.parseDouble(parts[3]);
			return new Location(w, x, y, z);

		}

		return null;
	}

	public static String loc2strNW(final Location l) {
		if (l == null) {
			return "";
		}

		return l.getX() + ":" + l.getY() + ":" + l.getZ() + ":" + l.getPitch() + ":" + l.getYaw();
	}

	public static Location str2locNW(final String s, final String world) {
		if (s == null || s.trim().isEmpty()) {
			return null;
		}

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
