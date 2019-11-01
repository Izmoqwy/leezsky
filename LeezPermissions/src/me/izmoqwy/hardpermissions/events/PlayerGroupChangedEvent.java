package me.izmoqwy.hardpermissions.events;

import lombok.Getter;
import me.izmoqwy.hardpermissions.Group;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class PlayerGroupChangedEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	private OfflinePlayer player;
	private Group group;

	public PlayerGroupChangedEvent(OfflinePlayer player, Group group) {
		this.player = player;
		this.group = group;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
