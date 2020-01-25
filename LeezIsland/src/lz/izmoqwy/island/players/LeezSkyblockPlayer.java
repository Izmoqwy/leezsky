package lz.izmoqwy.island.players;

import lombok.Getter;
import lz.izmoqwy.core.self.LeezCore;
import lz.izmoqwy.island.island.Island;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Player.Spigot;

public class LeezSkyblockPlayer extends LeezOfflineSkyblockPlayer implements SkyblockPlayer {

	private Player base;

	@Getter
	private Island island;

	private Location personalHome;
	private long lastRestart;

	public LeezSkyblockPlayer(Player base, Island island, Location personalHome, long lastRestart) {
		super(base);

		this.base = base;
		this.island = island;
		this.personalHome = personalHome;
		this.lastRestart = lastRestart;
	}

	@Override
	public long getLastRestart() {
		return lastRestart;
	}

	@Override
	public void setLastRestart(long lastRestart) {
		this.lastRestart = lastRestart;
		super.setLastRestart(lastRestart);
	}

	@Override
	public boolean hasPersonalHome() {
		return personalHome != null;
	}

	@Override
	public Location getPersonalHome() {
		return personalHome;
	}

	@Override
	public void setPersonalHome(Location personalHome) {
		this.personalHome = personalHome;
		super.setPersonalHome(personalHome);
	}

	@Override
	public boolean hasIsland() {
		return island != null;
	}

	@Override
	public boolean isOwner() {
		return island != null && island.isOwner(this);
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
