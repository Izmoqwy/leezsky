package lz.izmoqwy.leezisland.island;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class IslandMember {

	@Getter
	private UUID uniqueId;
	@Getter
	@Setter
	private IslandRole role;

	public IslandMember(UUID uniqueId, IslandRole role) {
		this.uniqueId = uniqueId;
		this.role = role;
	}

}
