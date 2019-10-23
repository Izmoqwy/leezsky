package lz.izmoqwy.leezcrates.objects;

import lombok.Getter;
import lz.izmoqwy.core.utils.ItemUtil;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.Collections;

public class Reward {

	@Getter
	private MaterialData icon;
	@Getter
	private String displayName, command;
	@Getter
	private int percent;

	public Reward(MaterialData icon, String displayName, String command, int percent) {
		this.icon = icon;
		this.displayName = displayName;
		this.command = command;
		this.percent = percent;
	}

	public ItemStack getItem() {
		return ItemUtil.createItem(icon, displayName, Collections.singletonList("§3Chance: §b" + percent + "%"));
	}
}
