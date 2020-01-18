package lz.izmoqwy.leezisland.players;

import lombok.Getter;
import lz.izmoqwy.core.self.LeezCore;
import lz.izmoqwy.leezisland.island.Island;
import org.bukkit.entity.Player;
import org.bukkit.entity.Player.Spigot;

public class LeezIslandPlayer extends LeezOffIslandPlayer implements SkyblockPlayer {

	private Player base;
	@Getter
	private Island island;

	public LeezIslandPlayer(Player base, Island island) {
		super(base);

		this.base = base;
		this.island = island;
	}

	@Override
	public boolean hasIsland() {
		return island != null;
	}

	@Override
	public boolean isOwner() {
		return island != null && island.getOwner().getUniqueId().equals(base.getUniqueId());
	}

	@Override
	public Player bukkit() {
		return base;
	}

	@Override
	public Spigot spigot() {
		return base.spigot();
	}

	@Override
	public void sendMessage(String message) {
		base.sendMessage(LeezCore.PREFIX + message);
	}

	@Override
	public void sendRawMessage(String message) {
		base.sendRawMessage(message);
	}

}
