package lz.izmoqwy.core.hooks.crosshooks.interfaces;

import lz.izmoqwy.core.hooks.crosshooks.CrossHook;
import org.bukkit.OfflinePlayer;

public interface LeezPermissionsCH extends CrossHook {

	Group getGroup(OfflinePlayer player);

}
