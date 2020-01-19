package lz.izmoqwy.core;

import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import lz.izmoqwy.core.self.CorePrinter;
import lz.izmoqwy.core.self.LeezCore;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PlayerDataStorage {

	private static final File folder;

	static {
		folder = new File(LeezCore.instance.getDataFolder(), "playerdata/");
		if (!folder.exists() && !folder.mkdirs())
			CorePrinter.err("Player datas' dir doesn't exist");
	}

	private static Map<UUID, YamlConfiguration> playersYaml = Maps.newHashMap();

	private static File getFile(OfflinePlayer player) throws IOException {
		File file = new File(folder, player.getUniqueId().toString() + ".yml");

		if (!file.exists() && !file.createNewFile())
			return null;
		return file;
	}

	@SneakyThrows(IOException.class)
	public static YamlConfiguration yaml(OfflinePlayer player) {
		if (playersYaml.containsKey(player.getUniqueId())) {
			YamlConfiguration yaml = playersYaml.get(player.getUniqueId());
			if (yaml == null) {
				playersYaml.remove(player.getUniqueId());
				return yaml(player);
			}
			return yaml;
		}
		else {
			YamlConfiguration yaml = YamlConfiguration.loadConfiguration(Objects.requireNonNull(getFile(player)));
			playersYaml.put(player.getUniqueId(), yaml);
			return yaml;
		}
	}

	@SneakyThrows(IOException.class)
	public static void save(OfflinePlayer player) {
		if (playersYaml.containsKey(player.getUniqueId())) {
			playersYaml.get(player.getUniqueId()).save(Objects.requireNonNull(getFile(player)));
		}
	}

	public static Object getObject(OfflinePlayer player, String path) {
		try {
			return yaml(player).get(path);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Object getObject(OfflinePlayer player, String path, Object def) {
		try {
			return yaml(player).get(path, def);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static <T> T get(OfflinePlayer player, String path) {
		final Object obj = getObject(player, path);
		return obj != null ? (T) obj : null;
	}

	public static <T> T get(OfflinePlayer player, String path, T def) {
		if (def == null)
			return get(player, path);

		final Object obj = getObject(player, path, def);
		return def.getClass().isInstance(obj) ? (T) obj : def;
	}

	public static <T> void set(OfflinePlayer player, String path, T value) {
		yaml(player).set(path, value);
	}

}
