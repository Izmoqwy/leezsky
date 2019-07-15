package lz.izmoqwy.core.utils;

import org.bukkit.Location;
import org.bukkit.block.Block;

public class LocationUtil {

	public static Location getSafeLocation(Location origin) {
		if (origin.getBlockY() <= 1) return null;

		Location to = origin;
		Block block;
		while((block = to.getBlock()) != null && !block.getType().isSolid()) {
			if (to.getBlockY() <= 1) return null;
			to.setY(to.getY() - 1f);
		}

		to.setY(to.getY() + 1.1f);
		return to;
	}

}
