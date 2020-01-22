package lz.izmoqwy.core.utils;

import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class StoreUtil {

	public static <K, V> boolean addToMap(Map<K, V> map, K key, V value) {
		if (!map.containsKey(key)) {
			map.put(key, value);
			return true;
		}
		return false;
	}

	public static <K, V> void addOrReplaceInMap(Map<K, V> map, K key, V defaultValue, MapAction<V> replaceAction) {
		if (!map.containsKey(key)) {
			map.put(key, defaultValue);
		}
		else {
			map.replace(key, replaceAction.update(map.get(key)));
		}
	}

	@SafeVarargs
	public static <K, V> void addToMapList(Map<K, List<V>> map, K key, V... values) {
		if (!map.containsKey(key)) {
			map.put(key, new ArrayList<>(Arrays.asList(values)));
		}
		else {
			List<V> list = map.getOrDefault(key, Lists.newArrayList());
			Collections.addAll(list, values);
			map.replace(key, list);
		}
	}

	public static <K, V> boolean removeFromMapList(Map<K, List<V>> map, K key, V value) {
		if (!map.containsKey(key))
			return false;

		return map.getOrDefault(key, Lists.newArrayList()).remove(value);
	}

	public static <K, V> boolean isInMapList(Map<K, List<V>> map, K key, V value) {
		if (!map.containsKey(key))
			return false;

		return map.get(key) != null && map.get(key).contains(value);
	}

	public static <V> boolean addToList(List<V> list, V value) {
		if (!list.contains(value)) {
			list.add(value);
			return true;
		}
		return false;
	}

	public static double mapValue(double x, double in_min, double in_max, double out_min, double out_max) {
		return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
	}

	public static ItemStack itemStackFromYAML(ConfigurationSection configurationSection) {
		if (configurationSection == null)
			return null;

		Material material = Material.getMaterial(configurationSection.getString("material", "AIR").toUpperCase());
		if (material == null || material == Material.AIR)
			return null;

		return new ItemStack(material, configurationSection.getInt("amount", 1), (short) configurationSection.getInt("data", 0));
	}

	@SneakyThrows
	public static boolean createIfMissing(File file) {
		if (file.exists())
			return true;

		if (file.getParentFile().exists() || file.getParentFile().mkdirs())
			return file.createNewFile();
		return false;
	}

	@SneakyThrows
	public static boolean copyTemplateIfMissing(File file, InputStream resource) {
		if (!file.exists()) {
			if (!StoreUtil.createIfMissing(file))
				return false;

			YamlConfiguration defaultConfiguration = YamlConfiguration.loadConfiguration(
					new InputStreamReader(resource, StandardCharsets.UTF_8));
			defaultConfiguration.save(file);
		}
		return true;
	}

	public interface MapAction<V> {

		V update(V current);

	}

}
