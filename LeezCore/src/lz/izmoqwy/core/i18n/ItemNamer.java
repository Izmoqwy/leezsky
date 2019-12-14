package lz.izmoqwy.core.i18n;

import lz.izmoqwy.core.utils.TextUtil;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ItemNamer {

	public String getDisplayName(Material material) {
		return Arrays.stream(material.name().toLowerCase().split("_"))
				.map(TextUtil::capitalizeFirstLetter)
				.collect(Collectors.joining(" "));
	}

}
