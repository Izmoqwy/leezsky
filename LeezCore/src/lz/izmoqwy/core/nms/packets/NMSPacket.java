package lz.izmoqwy.core.nms.packets;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface NMSPacket {

	void addToTablist(Player player);

	void removeFromTablist(Player player);

	void sendJson(Player player, String jsonText);

	void sendTitle(Player player, String title, String subtitle);

	void sendActionbar(Player player, String message);

	void sendTimings(Player player, int ticks, int fadeIn, int fadeOut);

	void sendTablist(Player player, String header, String footer);

	void sendGlobalTablist(String header, String footer);

	int getLatency(Player player);

	void setFakeBorder(Player player, double radius, Location center);

}
