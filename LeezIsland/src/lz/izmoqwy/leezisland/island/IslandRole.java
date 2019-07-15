package lz.izmoqwy.leezisland.island;

import lombok.Getter;

public enum IslandRole {

	MEMBER("Membre", '7',1), RECRUTER("Recruteur", 'e', 2), OFFICIER("Officier", '6', 3),

	// owner role here just for /is team
	// should not be used elsewhere
	OWNER("Chef", 'c', -1);

	public String name;
	public int id;

	@Getter
	private char colorChat;

	IslandRole(String name, char color, int id) {
		this.name = name;
		this.colorChat = color;
		this.id = id;
	}

	@Override
	public String toString() {
		return this.name;
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
