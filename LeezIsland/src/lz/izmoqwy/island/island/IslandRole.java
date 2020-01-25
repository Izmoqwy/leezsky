package lz.izmoqwy.island.island;

import lombok.Getter;

@Getter
public enum IslandRole {

	MEMBER("Membre", '7', 1), RECRUTER("Recruteur", 'e', 2), OFFICIER("Officier", '6', 3),

	// Meant only for '/is team' and should not be used elsewhere
	OWNER("Chef", 'c', -1);

	private String name;
	private char colorChat;
	private int id;

	IslandRole(String name, char color, int id) {
		this.name = name;
		this.colorChat = color;
		this.id = id;
	}

	@Override
	public String toString() {
		return getName();
	}

	public static IslandRole fromID(int id) {
		switch (id) {
			case 1:
				return MEMBER;
			case 2:
				return RECRUTER;
			case 3:
				return OFFICIER;
			default:
				return null;
		}
	}

}
