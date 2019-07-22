package lz.izmoqwy.core;

import com.google.common.collect.Maps;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class PlayerDataStorage {

	private static final File folder;

	static {
		folder = new File(LeezCore.instance.getDataFolder(), "playerdatas/");
		folder.mkdirs();
	}

	private static Map<UUID, YamlConfiguration> yamls = Maps.newHashMap();

	public static File getFile(OfflinePlayer player) throws IOException {
		final File file = new File(folder, player.getUniqueId().toString() + ".yml");
		if (!file.exists()) {
			file.createNewFile();
		}
		return file;
	}

	public static YamlConfiguration yaml(OfflinePlayer player) throws IOException {
		if (yamls.containsKey(player.getUniqueId())) {
			YamlConfiguration yaml = yamls.get(player.getUniqueId());
			if (yaml == null) {
				yamls.remove(player.getUniqueId());
				return yaml(player);
			}
			return yaml;
		}
		else {
			YamlConfiguration yaml = YamlConfiguration.loadConfiguration(getFile(player));
			yamls.put(player.getUniqueId(), yaml);
			return yaml;
		}
	}

	public static YamlConfiguration yamlNoThrow(OfflinePlayer player) {
		try {
			return yaml(player);
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Object getObject(OfflinePlayer player, String path) {
		try {
			return yaml(player).get(path);
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static <T> T get(OfflinePlayer player, String path) {
		final Object obj = getObject(player, path);
		return obj != null ? (T) obj : null;
	}

	public static Object getObject(OfflinePlayer player, String path, Object def) {
		try {
			return yaml(player).get(path, def);
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static <T> T get(OfflinePlayer player, String path, T def) {
		final Object obj = getObject(player, path, def);
		return obj != null ? (T) obj : def;
	}

	public static <T> void set(OfflinePlayer player, String path, T value) {
		try {
			yaml(player).set(path, value);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void save(OfflinePlayer player) throws IOException {
		if (yamls.containsKey(player.getUniqueId())) {
			yamls.get(player.getUniqueId()).save(getFile(player));
		}
	}

	public static void saveNoThrow(OfflinePlayer player) {
		try {
			save(player);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}
