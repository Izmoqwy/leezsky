package lz.izmoqwy.core.utils;

import com.google.common.collect.Lists;

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

	public interface MapAction<V> {
		V update(V current);
	}

}
