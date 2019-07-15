package lz.izmoqwy.core.hooks.interfaces;

import org.bukkit.OfflinePlayer;

public interface ModerationHook {

	boolean isMuted(OfflinePlayer player);
	boolean isBanned(OfflinePlayer player);

	String getName();

}
