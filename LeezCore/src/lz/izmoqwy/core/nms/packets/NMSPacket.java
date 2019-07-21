package lz.izmoqwy.core.nms.packets;

import com.mojang.authlib.properties.Property;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Collection;

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

	void sendTablist(Player player, String header, String footer);
	void sendTablist(String header, String footer);
	
	int getPing(Player player);
	
	void respawn(Player player);
    void setBorder(Player player, double radius, Location location);

    Player loadPlayer(OfflinePlayer offlinePlayer);

}
