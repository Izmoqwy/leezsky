package lz.izmoqwy.leezisland.spawners;

import lombok.Getter;
import lz.izmoqwy.core.world.LiteLocation;
import lz.izmoqwy.leezisland.LeezIsland;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;

import java.util.Random;

@Getter
public class SpawnerData {

	private final LiteLocation location;
	private int amount;

	public SpawnerData(LiteLocation location, int amount) {
		this.location = location;
		this.amount = amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
		SpawnersManager.manager.save(this);
		refresh();
	}

	public void refresh() {
		Bukkit.getScheduler().runTaskLater(LeezIsland.getInstance(), () -> {
			Block block = location.toLocation().getBlock();
			if (block == null || !(block.getState() instanceof CreatureSpawner))
				return;

			CreatureSpawner creatureSpawner = (CreatureSpawner) block.getState();
			creatureSpawner.setSpawnCount(calculateSpawnCount());
			creatureSpawner.setMaxNearbyEntities(amount > 6 ? amount + 3 : 6);
			creatureSpawner.update();
		}, 1L);
	}

	public int calculateSpawnCount() {
		Random random = new Random();
		int count = 0;
		for (int i = 0; i < getAmount(); i++) {
			count += random.nextInt(3) + 1;
		}
		return count;
	}

	@Override
	public String toString() {
		return amount + ";";
	}

	public static SpawnerData from(LiteLocation location, String toString) {
		if (location == null || toString == null)
			return null;

		String[] data = toString.split(";");
		try {
			return new SpawnerData(location, Integer.parseInt(data[0]));
		}
		catch (NumberFormatException ex) {
			return null;
		}
	}
}
