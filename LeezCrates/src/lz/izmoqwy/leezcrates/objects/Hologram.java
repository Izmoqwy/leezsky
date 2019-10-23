package lz.izmoqwy.leezcrates.objects;

import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.List;

public class Hologram {

	// Not storing the ArmorStand object directly for "bug prevention"
	@Getter
	private List<Integer> armorStandId = Lists.newArrayList();

	@Getter
	private Location location;
	@Getter
	private String[] text;

	public Hologram(Location location, String[] text) {
		this.location = location;
		this.text = text;
	}

	public Hologram(Location location, List<String> text) {
		this(location, text.toArray(new String[0]));
	}

	public void removeCurrentAS() {
		location.getWorld().getChunkAt(location);
		for (Entity entity : location.getWorld().getNearbyEntities(location, 2, 2 + .5D  * (text.length - 1), 2)) {
			if (entity.getType() == EntityType.ARMOR_STAND) {
				ArmorStand as = (ArmorStand) entity;
				if (as.isCustomNameVisible() && !as.hasGravity() && !as.isVisible()) {
					as.remove();
				}
			}
		}
	}

	public void spawn() {
		if (!armorStandId.isEmpty() || location == null)
			return;

		removeCurrentAS();
		spawn(0, true);
	}

	public void spawn(int index, boolean spawnNext) {
		if (!armorStandId.isEmpty() || location == null)
			return;

		ArmorStand as = location.getWorld().spawn(location.clone().add(0, index * .25D, 0), ArmorStand.class);
		as.setInvulnerable(true);
		as.setGravity(false);
		as.setVisible(false);
		as.setCanPickupItems(false);

		as.setCustomName(text[text.length - (index + 1)]);
		as.setCustomNameVisible(true);

		if (spawnNext && index < (text.length - 1))
			spawn(index + 1, true);
	}

	public void moveTo(Location location) {
		if (!armorStandId.isEmpty()) {
			this.location.getWorld().getChunkAt(this.location);
			for (Entity entity : this.location.getWorld().getNearbyEntities(this.location, 2, 2, 2)) {
				if (entity.getType() == EntityType.ARMOR_STAND && armorStandId.contains(entity.getEntityId())) {
					ArmorStand as = (ArmorStand) entity;
					as.teleport(location.clone().add(0, this.location.distance(location), 0));
					return;
				}
			}
			this.armorStandId.clear();
		}
		this.location = location;
		spawn();
	}
}
