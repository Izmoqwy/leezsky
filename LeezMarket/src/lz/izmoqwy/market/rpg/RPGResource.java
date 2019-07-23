package lz.izmoqwy.market.rpg;

import lombok.Getter;
import org.bukkit.ChatColor;

public enum RPGResource {

	DARKMATTER("Matière noire", '8', '✺'), URANIUM("Uranium", 'a', '☢'), TITANE("Titane", '7', '❆'), COPPER("Cuivre", 'c', '✻'),
	BZZODARK("Invalide", '0', '✖');

	@Getter
	private String name;
	@Getter
	private char color, symbol;

	RPGResource(String name, char color, char symbol) {
		this.name = name;
		this.color = color;
		this.symbol = symbol;
	}

	public String getFullName() {
		return ChatColor.COLOR_CHAR + "" + color + toString();
	}

	public String getFullName(String amount) {
		return ChatColor.COLOR_CHAR + "" + color + amount + " " + toString();
	}

	@Override
	public String toString() {
		return symbol + " " + name;
	}
}
