package lz.izmoqwy.leezisland.island;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

@SuppressWarnings("deprecation")
public enum GeneralPermission {

	SPAWNERS('S', Material.MOB_SPAWNER, "Spawners actifs"),
	MOBSPAWNING('M', new MaterialData(Material.MONSTER_EGG, (byte) 120), "Monstres", "Si désactivé, les monstres ne spawneront plus"),
	FLUIDFLOWING('F', Material.WATER_BUCKET, "Écoulement des fluides", "Si désactivé, les fluides ne couleront plus"),
	GENENABLED('G', Material.COBBLESTONE, "Générateur modifié");

	public char val;
	@Getter
	private String title, description;
	@Getter
	private MaterialData icon;

	GeneralPermission(char val, Material icon, String title) {
		this(val, icon, title, null);
	}

	GeneralPermission(char val, Material icon, String title, String description) {
		this(val, new MaterialData(icon), title, description);
	}

	GeneralPermission(char val, MaterialData icon, String title) {
		this(val, icon, title, null);
	}

	GeneralPermission(char val, MaterialData icon, String title, String description) {
		this.val = val;
		this.icon = icon;
		this.title = title;
		this.description = description;
	}

}