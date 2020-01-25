package lz.izmoqwy.leezisland.island.permissions;

import lombok.Getter;

@Getter
public enum PermissionType {

	VISITOR("Visiteurs"), COOP("Coopérants"), GENERAL("Général");

	private String displayName;

	PermissionType(String displayName) {
		this.displayName = displayName;
	}

}
