package lz.izmoqwy.market.blackmarket.illegal;

import lombok.Getter;
import lombok.Setter;
import lz.izmoqwy.core.api.PlayerBackup;
import org.bukkit.entity.Player;

public class ForbiddenPlayer {

	@Getter
	private Player base;
	@Getter
	private PlayerBackup backup;

	@Getter @Setter
	private int armor_level = 1, items_level = 1;

	@Getter
	private long joinedAt;
	@Getter
	private int gold_earned = 0, iron_earned = 0;

	public ForbiddenPlayer(Player base) {
		this.base = base;
		this.backup = PlayerBackup.fromBukkitPlayer(base);
		this.joinedAt = System.currentTimeMillis();
	}

	public void earnGold(int amount) {
		gold_earned += amount;
	}

	public void earnIron(int amount) {
		iron_earned += amount;
	}

	public void sendMessage(String message) {
		sendMessage(message, true);
	}

	public void sendMessage(String message, boolean withPrefix) {
		if (base.isOnline())
			base.sendMessage(withPrefix ? ForbiddenArena.PREFIX + message : message);
	}
}
