package lz.izmoqwy.core.api;

import com.google.common.collect.Maps;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

public class MapsAPI {

	private File saveFile;

	@Getter
	private Map<Integer, String> maps = Maps.newHashMap();

	public MapsAPI(File saveFile) {
		this.saveFile = saveFile;
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(saveFile);
		for (String key : yaml.getKeys(false)) {
			maps.put(Integer.parseInt(key), yaml.getString(key + ".data", ""));
		}
	}

	public void save() {
		if (saveFile == null)
			return;

		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(saveFile);
		for (Entry<Integer, String> map : maps.entrySet()) {
			yaml.set(map.getKey() + "", map.getValue());
		}

		try {
			yaml.save(saveFile);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}
