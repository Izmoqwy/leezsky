package lz.izmoqwy.leezcrates.objects;

import lombok.Getter;
import lz.izmoqwy.leezcrates.LeezCrates;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.List;

public class CrateType {

	@Getter
	private String name, displayName;

	@Getter
	private boolean broadcasted;

	@Getter
	private MaterialData materialData;
	@Getter
	private List<Reward> rewards;

	public CrateType(String name, String displayName, boolean broadcast, MaterialData materialData, List<Reward> rewards) {
		this.name = name;
		this.displayName = displayName;
		this.broadcasted = broadcast;
		this.materialData = materialData;
		this.rewards = rewards;
	}

	public ItemStack getRandomReward() {
		if (rewards.isEmpty())
			return null;

		double picked = LeezCrates.RANDOM.nextDouble();
		double current = 0;
		for (Reward reward : rewards) {
			current += reward.getPercent() / 100D;
			if (picked <= current) {
				return reward.getItem();
			}
		}

		// Should be reached only if there is less than 100% in total
		return rewards.get(0).getItem();
	}
}
