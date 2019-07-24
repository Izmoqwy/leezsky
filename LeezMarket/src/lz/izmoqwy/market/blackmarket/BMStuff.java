package lz.izmoqwy.market.blackmarket;

import lz.izmoqwy.market.blackmarket.stuff.titane.*;

public class BMStuff {

	public static final TitaneHelmet TITANE_HELMET;
	public static final TitaneChestplate TITANE_CHESTPLATE;
	public static final TitaneLeggings TITANE_LEGGINGS;
	public static final TitaneBoots TITANE_BOOTS;
	public static final TitaneShield TITANE_SHIELD;

	static {
		// Todo: Load prices dynamicly
		TITANE_HELMET = new TitaneHelmet((int) 1.5e3);
		TITANE_CHESTPLATE = new TitaneChestplate((int) 4e3);
		TITANE_LEGGINGS = new TitaneLeggings((int) 2e3);
		TITANE_BOOTS = new TitaneBoots((int) 3e3);
		TITANE_SHIELD = new TitaneShield((int) 3e3);
	}

}
