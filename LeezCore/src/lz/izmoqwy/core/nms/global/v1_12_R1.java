package lz.izmoqwy.core.nms.global;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Collection;

public class v1_12_R1 implements NMSGlobal {

	@Override
	public void removeProfileProperty(Player player, String property) {
		gp(player).getProperties().removeAll(property);
	}

	@Override
	public void putProfileProperties(Player player, String property, Collection<Property> values) {
		gp(player).getProperties().putAll(property, values);
	}

	@Override
	public void setNameField(Field field, Player player, String value)
			throws IllegalArgumentException, IllegalAccessException {
		field.set(gp(player), value);
	}

	@Override
	public boolean hasInventoryOpened(Player player) {
		EntityPlayer entityPlayer = getEntityHuman(player);
		return entityPlayer.activeContainer != entityPlayer.defaultContainer;
	}

	private EntityPlayer getEntityHuman(Player player) {
		return cp(player).getHandle();
	}

	private GameProfile gp(Player player) {
		return cp(player).getProfile();
	}

	private CraftPlayer cp(Player player) {
		return (CraftPlayer) player;
	}

}
