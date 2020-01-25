package lz.izmoqwy.island.island.permissions;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

@SuppressWarnings("deprecation")
@Getter
public enum GeneralPermission {

	SPAWNERS('S', Material.MOB_SPAWNER, "Spawners actifs"),
	MOB_SPAWN('M', new MaterialData(Material.MONSTER_EGG, (byte) 120), "Monstres", "Si désactivé, les monstres n'apparaîtront plus"),
	FLUID_FLOW('F', Material.WATER_BUCKET, "Écoulement des fluides", "Si désactivé, les fluides ne couleront plus"),
	CUSTOM_GENERATOR('G', Material.COBBLESTONE, "Générateur modifié");

	private final char identifier;
	private final String title, description;
	private final MaterialData icon;

	GeneralPermission(char identifier, Material icon, String title) {
		this(identifier, icon, title, null);
	}

	GeneralPermission(char identifier, Material icon, String title, String description) {
		this(identifier, new MaterialData(icon), title, description);
	}

	GeneralPermission(char identifier, MaterialData icon, String title, String description) {
		this.identifier = identifier;
		this.icon = icon;
		this.title = title;
		this.description = description;
	}

}