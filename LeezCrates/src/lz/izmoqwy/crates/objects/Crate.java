package lz.izmoqwy.crates.objects;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

@Getter
public class Crate {

	private String id;
	private CrateType type;

	private Location location;
	private String displayName;

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
