package lz.izmoqwy.core.hooks.crosshooks.interfaces;

import lz.izmoqwy.core.hooks.crosshooks.CrossHook;
import org.bukkit.OfflinePlayer;

public interface LeezIslandCH extends CrossHook {

	IslandInfo getIslandInfo(OfflinePlayer player);

}
