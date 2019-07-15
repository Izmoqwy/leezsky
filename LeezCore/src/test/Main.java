package test;

import com.google.common.collect.Maps;
import lz.izmoqwy.core.utils.StoreUtil;

import java.util.List;
import java.util.Map;

public class Main {

	public static void main(String[] args) {
		Map<String, List<String>> dictonnary = Maps.newHashMap();

		StoreUtil.addToMapList(dictonnary, "poney", "pony");
		StoreUtil.addToMapList(dictonnary, "poney", "ponies");
		StoreUtil.addToMapList(dictonnary, "poney", "abricot");

		for (List<String> trs : dictonnary.values()) {
			for (String tr : trs) {
				System.out.println("Test: " + tr);
			}
		}
	}

}
