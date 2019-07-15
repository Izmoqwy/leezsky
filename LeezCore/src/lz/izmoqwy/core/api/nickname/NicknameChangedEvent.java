package lz.izmoqwy.core.api.nickname;

import java.util.UUID;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NicknameChangedEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private UUID player;
	private String newName;

	protected NicknameChangedEvent(UUID player, String newName) {
		this.player = player;
		this.newName = newName;
	}

	public UUID getPlayer() {
		return player;
	}

	public String getNewName() {
		return newName;
	}

}
