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

	private final char identifier;
	private final String title, description;
	private final MaterialData icon;

	CoopPermission(char identifier, Material icon, String title) {
		this(identifier, icon, title, null);
	}

	CoopPermission(char identifier, Material icon, String title, String description) {
		this(identifier, new MaterialData(icon), title, description);
	}

	CoopPermission(char identifier, MaterialData icon, String title, String description) {
		this.identifier = identifier;
		this.icon = icon;
		this.title = title;
		this.description = description;
	}

}

