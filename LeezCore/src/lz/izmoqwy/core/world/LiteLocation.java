package lz.izmoqwy.core.world;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;

@Getter @Setter
public class LiteLocation {

	private int x, y, z;
	private String world;

	public LiteLocation(int x, int y, int z, String world) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.world = world;
	}

	public LiteLocation copy() {
		return new LiteLocation(x, y, z, world);
	}

	public Location toLocation() {
		return new Location(Bukkit.getWorld(world), x, y, z);
	}

	@Override
	public String toString() {
		if (world != null)
			return x + ";" + y + ";" + z + ";" + world;
		else
			return x + ";" + y + ";" + z;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31)
				.append(x).append(y).append(z).append(world).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LiteLocation))
			return false;
		if (obj == this)
			return true;

		LiteLocation other = (LiteLocation) obj;
		return other.x == x && other.y == y && other.z == z && other.world.equals(world);
	}

	public static LiteLocation from(Location location) {
		if (location == null)
			return null;
		return new LiteLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getName());
	}

	public static LiteLocation from(String toString) {
		if (toString == null)
			return null;

		String[] data = toString.split(";");
		if (data.length == 3 || data.length == 4) {
			return new LiteLocation(Integer.parseInt(data[0]), Integer.parseInt(data[1]), Integer.parseInt(data[2]), data.length == 4 ? data[3] : null);
		}
		return null;
	}
}
