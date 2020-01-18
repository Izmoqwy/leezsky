package lz.izmoqwy.core.nms;

import lz.izmoqwy.core.nms.global.NMSGlobal;
import lz.izmoqwy.core.nms.packets.NMSPacket;
import lz.izmoqwy.core.nms.scoreboard.NMSScoreboard;
import lz.izmoqwy.core.self.CorePrinter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;

public class NMS {

	public static NMSPacket packet = null;
	public static NMSGlobal global = null;
	public static Class<? extends NMSScoreboard> scoreboard = null;

	public static void load() {
		String nmsVersion = Bukkit.getServer().getClass().getPackage().getName();
		nmsVersion = nmsVersion.substring(nmsVersion.lastIndexOf(".") + 1);

		if (nmsVersion.equalsIgnoreCase("v1_12_R1")) {
			packet = new lz.izmoqwy.core.nms.packets.v1_12_R1();
			global = new lz.izmoqwy.core.nms.global.v1_12_R1();
			scoreboard = lz.izmoqwy.core.nms.scoreboard.v1_12_R1.class;
		}
		else {
			CorePrinter.err("No NMS system available for that version (" + nmsVersion + ") found.");
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
