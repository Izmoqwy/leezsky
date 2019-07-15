package lz.izmoqwy.core.utils;

import lz.izmoqwy.core.nms.NmsAPI;
import org.bukkit.entity.Player;

public class TitleUtil {

	public static void sendTitle(Player player, String title, String subtitle, int seconds, int fadein, int fadeout) {
		sendTickTitle(player, title, subtitle, seconds * 20, fadein, fadeout);
	}

	public static void sendTitle(Player player, String title, String subtitle, int seconds) {
		sendTickTitle(player, title, subtitle, seconds * 20, 15, 15);
	}

	public static void sendTickTitle(Player player, String title, String subtitle, int ticks, int fadein, int fadeout) {
		NmsAPI.packet.sendTitle(player, title, subtitle, ticks, fadein, fadeout);
	}

	public static void sendTickTitle(Player player, String title, String subtitle, int ticks) {
		sendTickTitle(player, title, subtitle, ticks, 15, 15);
	}

}


