package lz.izmoqwy.island.island;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class IslandMember {

	private UUID playerId;
	private IslandRole role;

	public OfflinePlayer toPlayer(Island island) {
		return island.getMembersMap().containsKey(playerId) ? Bukkit.getOfflinePlayer(playerId) : null;
	}

}
