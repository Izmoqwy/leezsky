package lz.izmoqwy.core.top;

import org.bukkit.OfflinePlayer;

import java.util.List;

public interface TopResult {

	OfflinePlayer getPlayer();

	String getItemName();
	List<String> getItemDescription();

}
