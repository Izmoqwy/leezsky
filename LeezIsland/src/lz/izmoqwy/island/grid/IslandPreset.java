package lz.izmoqwy.island.grid;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum IslandPreset {

	DEFAULT("Par défaut", "default");

	private final String title, schematicName;

}
