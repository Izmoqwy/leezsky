package lz.izmoqwy.island.island.permissions;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

@Getter
public enum CoopPermission {

	PLACE('P', Material.COBBLESTONE, "Poser des blocs"),
	BREAK('B', Material.DIAMOND_PICKAXE, "Casser des blocs"),
	CHEST('C', Material.CHEST, "Utiliser les coffres"),
	SHULKER_BOX('S', Material.PURPLE_SHULKER_BOX, "Utiliser les shulkers box"),
	CONTAINERS('c', Material.DROPPER, "Ouvrir les conteneurs"),
	BUCKETS('b', Material.BUCKET, "Utiliser les seaux"),
	FIRE('F', Material.FLINT_AND_STEEL, "Mettre le feu"),

	REDSTONE('R', Material.DIODE, "Utiliser la redstone", "Modifier les répéteurs et comparateurs"),
	ACTIVATORS('A', Material.LEVER, "Actionneurs", "Utiliser les boutons et leviers");

	private char val;
	private String title, description;
	private MaterialData icon;

	CoopPermission(char val, Material icon, String title) {
		this(val, icon, title, null);
	}

	CoopPermission(char val, Material icon, String title, String description) {
		this(val, new MaterialData(icon), title, description);
	}

	CoopPermission(char val, MaterialData icon, String title, String description) {
		this.val = val;
		this.icon = icon;
		this.title = title;
		this.description = description;
	}

}

