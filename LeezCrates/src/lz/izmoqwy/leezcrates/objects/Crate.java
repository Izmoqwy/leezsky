package lz.izmoqwy.leezcrates.objects;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class Crate {

	@Getter
	private String id;
	@Getter
	private CrateType type;

	@Getter
	private Location location;
	@Getter
	private String displayName;

	@Getter
	private Hologram hologram;

	public Crate(String id, CrateType type, Location location, String displayName, Hologram hologram) {
		this.id = id;
		this.type = type;
		this.location = location;
		this.hologram = hologram;
		this.displayName = displayName;
	}

	public ItemStack getRandomReward() {
		return type.getRandomReward();
	}
}
