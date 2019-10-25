package lz.izmoqwy.core.crosshooks.interfaces;

import java.util.UUID;

public interface IslandInfo {

	String getName();
	int getLevel();

	String getRoleName(UUID player, boolean color);
	IslandRelationship getRelationship(UUID player);

}
