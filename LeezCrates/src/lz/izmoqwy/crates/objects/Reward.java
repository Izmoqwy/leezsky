package lz.izmoqwy.crates.objects;

import lombok.Getter;
import lz.izmoqwy.core.utils.ItemUtil;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.Collections;

@Getter
public class Reward {

	private MaterialData icon;
	private String displayName, command;
	private int percent;
	private ItemStack item;

	public Reward(MaterialData icon, String displayName, String command, int percent) {
		this.icon = icon;
		this.displayName = displayName;
		this.command = command;
		this.percent = percent;

		this.item = ItemUtil.createItem(icon, displayName, Collections.singletonList("§3Chance: §b" + percent + "%"));
	}

}
