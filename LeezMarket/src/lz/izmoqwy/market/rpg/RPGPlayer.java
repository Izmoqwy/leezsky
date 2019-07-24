package lz.izmoqwy.market.rpg;

import lombok.Getter;
import lombok.Setter;
import lz.izmoqwy.core.CorePrinter;
import org.bukkit.OfflinePlayer;

public class RPGPlayer {

	@Getter
	private OfflinePlayer base;

	@Getter @Setter
	private int exp;
	@Getter
	private int points;
	@Getter
	private int energy;
	@Getter
	private int max_energy;

	@Getter
	private long last_get;
	@Getter
	private int res_darkmatter;
	@Getter
	private int res_uranium;
	@Getter
	private int res_titane;
	@Getter
	private int res_copper;

	@Getter
	private long last_fish;
	@Getter
	private int fish_common;
	@Getter
	private int fish_uncommon;

	@Getter
	private int item_pickaxe;
	@Getter
	private int item_fishrod;
	@Getter
	private int item_storage;

	public RPGPlayer(OfflinePlayer base, int exp, int points, int energy, int max_energy, long last_get, int res_darkmatter, int res_uranium, int res_titane, int res_copper, long last_fish, int fish_common, int fish_uncommon, int item_pickaxe, int item_fishrod, int item_stockage) {
		this.base = base;
		this.exp = exp;
		this.points = points;
		this.energy = energy;
		this.max_energy = max_energy;
		this.last_get = last_get;
		this.res_darkmatter = res_darkmatter;
		this.res_uranium = res_uranium;
		this.res_titane = res_titane;
		this.res_copper = res_copper;
		this.last_fish = last_fish;
		this.fish_common = fish_common;
		this.fish_uncommon = fish_uncommon;
		this.item_pickaxe = item_pickaxe;
		this.item_fishrod = item_fishrod;
		this.item_storage = item_stockage;
	}

	public void sendMessage(String message) {
		if (base.isOnline())
			base.getPlayer().sendMessage(message);
		else
			CorePrinter.warn("Trying to send message to an offline player! ({0} -> {1})", base.getName(), message);
	}

	public int calcLevel() {
		int xp = this.exp, needed = 0;
		int level = 0;
		while(xp >= needed) {
			level++;
			if (level > 1e3)
				break;
			needed = nextLevel(level);
		}
		return level;
	}

	private int nextLevel(int level) {
		return (250 * (int) Math.pow(level, 2)) - (250 * level);
	}

}
