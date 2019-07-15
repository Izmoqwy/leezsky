package lz.izmoqwy.leezisland.island;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

@SuppressWarnings("deprecation")
public enum VisitorPermission {

	DOORS('D', Material.ACACIA_DOOR_ITEM, "Ouvrir les portes"),
	GATES('G', Material.FENCE_GATE, "Ouvrir les portillons"),
	BUTTONS('B', Material.STONE_BUTTON, "Utiliser les boutons"),
	LEVERS('L', Material.LEVER, "Utiliser les leviers"),
	PLATES('P', Material.STONE_PLATE, "Activer les plaques de pression"),
	ARMORSTANDS('A', Material.ARMOR_STAND, "Intéragir avec les porte armures"),
	REDSTONE('R', Material.DIODE, "Utiliser la redstone", "Modifier les répéteurs et comparateurs"),
	VILLAGERS('V', new MaterialData(Material.MONSTER_EGG, (byte) 120), "Intéragir avec les villageois"),
	DROP('d', Material.STICK, "Jeter des objets"),
	PICKUP('p', Material.GOLD_NUGGET, "Ramasser des objets"),
	FLY('F', Material.FEATHER, "Voler"),
	SETHOME('S', Material.SPIDER_EYE, "Définir un home");

	public char val;
	@Getter
	private String title, description;
	@Getter
	private MaterialData icon;

	VisitorPermission(char val, Material icon, String title) {
		this(val, icon, title, null);
	}

	VisitorPermission(char val, Material icon, String title, String description) {
		this(val, new MaterialData(icon), title, description);
	}

	VisitorPermission(char val, MaterialData icon, String title) {
		this(val, icon, title, null);
	}

	VisitorPermission(char val, MaterialData icon, String title, String description) {
		this.val = val;
		this.icon = icon;
		this.title = title;
		this.description = description;
	}

}

