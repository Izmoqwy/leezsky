package lz.izmoqwy.island.generator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;

@Getter
@AllArgsConstructor
public class Ore {

	private Material type;
	private byte spawningValue;
	private double chance;

}
