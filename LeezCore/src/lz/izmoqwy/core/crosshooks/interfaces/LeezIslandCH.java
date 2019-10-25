package lz.izmoqwy.core.crosshooks.interfaces;

import lz.izmoqwy.core.crosshooks.CrossHook;
import org.bukkit.OfflinePlayer;

public interface LeezIslandCH extends CrossHook {

	IslandInfo getIslandInfo(OfflinePlayer player);

}
