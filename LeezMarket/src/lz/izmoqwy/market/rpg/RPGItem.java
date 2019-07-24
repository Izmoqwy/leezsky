package lz.izmoqwy.market.rpg;

import lombok.Getter;

public enum RPGItem {

	PICKAXE("Pioche"), FISHROD("Canne à pêche"), HANGAR("Hangar");

	@Getter
	private String displayName;

	RPGItem(String displayName) {
		this.displayName = displayName;
	}

	String dbCol() {
		return "item_" + name().toLowerCase();
	}

}
