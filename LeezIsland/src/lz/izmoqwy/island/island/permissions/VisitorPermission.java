package lz.izmoqwy.island.island.permissions;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

@SuppressWarnings("deprecation")
@Getter
public enum VisitorPermission {

	DOORS('D', Material.ACACIA_DOOR_ITEM, "Ouvrir les portes"),
	GATES('G', Material.FENCE_GATE, "Ouvrir les portillons"),
	BUTTONS('B', Material.STONE_BUTTON, "Utiliser les boutons"),
	LEVERS('L', Material.LEVER, "Utiliser les leviers"),
	PLATES('P', Material.STONE_PLATE, "Activer les plaques de pression"),
	ARMORSTANDS('A', Material.ARMOR_STAND, "Interagir avec les porte armures"),
	REDSTONE('R', Material.DIODE, "Utiliser la redstone", "Modifier les répéteurs et comparateurs"),
	VILLAGERS('V', new MaterialData(Material.MONSTER_EGG, (byte) 120), "Interagir avec les villageois"),
	DROP('d', Material.STICK, "Jeter des objets"),
	PICKUP('p', Material.GOLD_NUGGET, "Ramasser des objets"),
	FLY('F', Material.FEATHER, "Voler"),
	SETHOME('S', Material.SPIDER_EYE, "Définir un home"),

	HITMOBS('H', new MaterialData(Material.MONSTER_EGG, (byte) 54), "Taper les monstres"),
	HITANIMALS('h', new MaterialData(Material.MONSTER_EGG, (byte) 90), "Taper les animaux"),
	HITGOLEMS('g', new MaterialData(Material.MONSTER_EGG), "Taper les golems"),
	RIDING('r', Material.SADDLE, "Utiliser les selles", "Monter sur les chevaux/cochons"),
	USE_LEASH('U', Material.LEASH, "Utiliser les laisses"),
	USE_BOW('b', Material.BOW, "Utiliser l'arc", "Envoyer des flèches"),
	FISH('f', Material.FISHING_ROD, "Pêcher");

	private char val;
	private String title, description;
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

