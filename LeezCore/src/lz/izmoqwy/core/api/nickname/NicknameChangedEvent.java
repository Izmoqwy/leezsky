package lz.izmoqwy.core.api.nickname;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class NicknameChangedEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	private UUID player;
	private String newName;

}
