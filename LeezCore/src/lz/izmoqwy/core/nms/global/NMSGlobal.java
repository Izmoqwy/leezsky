package lz.izmoqwy.core.nms.global;

import com.mojang.authlib.properties.Property;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Collection;

public interface NMSGlobal {

	void removeProfileProperty(Player player, String property);

	void putProfileProperties(Player player, String property, Collection<Property> values);

	void setNameField(Field field, Player player, String value)
			throws IllegalArgumentException, IllegalAccessException;

	boolean hasInventoryOpened(Player player);

}
