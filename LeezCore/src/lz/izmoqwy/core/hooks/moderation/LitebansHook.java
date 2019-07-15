package lz.izmoqwy.core.hooks.moderation;

import litebans.api.Database;
import lz.izmoqwy.core.hooks.interfaces.ModerationHook;
import org.bukkit.OfflinePlayer;

public class LitebansHook implements ModerationHook {

	private final Database db = Database.get();
	private final String ip = "127.0.0.1";

	public boolean isMuted(OfflinePlayer player) {
		return db.isPlayerMuted(player.getUniqueId(), ip);
	}

	public boolean isBanned(OfflinePlayer player) {
		return db.isPlayerBanned(player.getUniqueId(), ip);
	}

	public String getName() {
		return "LiteBans";
	}

}
