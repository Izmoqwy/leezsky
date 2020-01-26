package lz.izmoqwy.island.generator;

import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
public class OreGeneratorSettings {

	private List<Ore> ores;
	private int totalByte;

	public OreGeneratorSettings(Map<Material, Byte> materialByteMap) {
		this.totalByte = materialByteMap.values().stream().mapToInt(Integer::valueOf).sum();

		List<Ore> ores = Lists.newArrayList();
		List<Material> keys = new ArrayList<>(materialByteMap.keySet());
		Collections.shuffle(keys);

		// Pass individual rate on over 100
		keys.forEach(key -> {
			double overPercentage = 100 * materialByteMap.get(key) / (totalByte * 1d);
			ores.add(new Ore(key, materialByteMap.get(key), overPercentage));
		});

		this.ores = ores;
	}

}
