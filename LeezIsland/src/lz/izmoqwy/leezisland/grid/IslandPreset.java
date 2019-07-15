package lz.izmoqwy.leezisland.grid;

import lombok.Getter;

public enum IslandPreset {

	DEFAULT("Par défaut", "default");

	@Getter
	private final String title, schematicName;

	IslandPreset(String title, String schematicName) {
		this.title = title;
		this.schematicName = schematicName;
	}

}
