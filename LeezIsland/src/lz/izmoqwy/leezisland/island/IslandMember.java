package lz.izmoqwy.leezisland.island;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
public class IslandMember {

	private UUID uniqueId;
	@Setter
	private IslandRole role;

	public IslandMember(UUID uniqueId, IslandRole role) {
		this.uniqueId = uniqueId;
		this.role = role;
	}

}
