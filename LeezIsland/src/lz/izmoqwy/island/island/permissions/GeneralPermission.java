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

	private char val;
	private String title, description;
	private MaterialData icon;

	GeneralPermission(char val, Material icon, String title) {
		this(val, icon, title, null);
	}

	GeneralPermission(char val, Material icon, String title, String description) {
		this(val, new MaterialData(icon), title, description);
	}

	GeneralPermission(char val, MaterialData icon, String title, String description) {
		this.val = val;
		this.icon = icon;
		this.title = title;
		this.description = description;
	}

}