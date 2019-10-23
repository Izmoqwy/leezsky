package lz.izmoqwy.core.nms;

import lz.izmoqwy.core.CorePrinter;
import lz.izmoqwy.core.nms.packets.NMSPacket;
import lz.izmoqwy.core.nms.packets.v1_11_R1;
import lz.izmoqwy.core.nms.packets.v1_12_R1;
import lz.izmoqwy.core.nms.packets.v1_8_R3;
import lz.izmoqwy.core.nms.scoreboard.NMSScoreboard;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;

public class NmsAPI {

	public static NMSPacket packet = null;
	public static Class<? extends NMSScoreboard> scoreboard = null;

	public static void load() {
		String nms_version = Bukkit.getServer().getClass().getPackage().getName();
		nms_version = nms_version.substring(nms_version.lastIndexOf(".") + 1);

		if (nms_version.equalsIgnoreCase("v1_8_R3")) {
			packet = new v1_8_R3();
		}
		else if (nms_version.equalsIgnoreCase("v1_11_R1")) {
			packet = new v1_11_R1();
		}
		else if (nms_version.equalsIgnoreCase("v1_12_R1")) {
			packet = new v1_12_R1();
			scoreboard = lz.izmoqwy.core.nms.scoreboard.v1_12_R1.class;
		}
		else {
			CorePrinter.err("No NMS system available for that version (" + nms_version + ") found.");
			return;
		}
		CorePrinter.print("NMS system loaded correctly.");
	}

	public static NMSScoreboard createScoreboard(Player player, String title) {
		if (scoreboard == null)
			return null;

		try {
			return scoreboard.getDeclaredConstructor(Player.class, String.class).newInstance(player, title);
		}
		catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
	}
}
