package lz.izmoqwy.core.nms.packets;

import java.lang.reflect.Field;
import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.mojang.authlib.properties.Property;

public interface NMSPacket {
	
	void removeProfileProperty(Player player, String property);
	void putProfileProperties(Player player, String property, Collection<Property> values);
	
	void setNameField(Field field, Player player, String value)
			throws IllegalArgumentException, IllegalAccessException;
	
	void addToTablist(Player player);
	void removeFromTablist(Player player);
	
	void sendJson(Player player, String jsonText);
	
	void sendTitle(Player player, String title, String subtitle, int ticks, int fadein, int fadeout);
	void sendActionbar(Player player, String message);
	
	int getPing(Player player);
	
	void respawn(Player player);
    void setBorder(Player player, double radius, Location location);

    Player loadPlayer(OfflinePlayer offlinePlayer);

}
