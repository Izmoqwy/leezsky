package lz.izmoqwy.leezisland.generator;

import com.google.common.collect.Maps;
import lombok.Getter;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class OreGeneratorSettings {

	@Getter
	private Map<Material, Double> ores;

	@Getter
	private int totalByte;

	public OreGeneratorSettings(Map<Material, Byte> materialByteMap) {
		totalByte = 0;
		materialByteMap.values().forEach(b -> totalByte += b);

		List<Material> keys = new ArrayList<>(materialByteMap.keySet());
		Collections.shuffle(keys);

		// Pass individual rate on over 100
		Map<Material, Double> newMap = Maps.newHashMap();
		keys.forEach(key -> {
			double overPercentage = 100 * materialByteMap.get(key) / (totalByte * 1d);
			newMap.put(key, overPercentage);
		});
		this.ores = Maps.immutableEnumMap(newMap);
	}
}
