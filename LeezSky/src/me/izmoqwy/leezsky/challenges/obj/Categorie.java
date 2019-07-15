package me.izmoqwy.leezsky.challenges.obj;

import lombok.Getter;
import me.izmoqwy.leezsky.challenges.Difficulty;

public final class Categorie {

	@Getter
	private final String name;
	@Getter
	private final Difficulty difficulty;
	@Getter
	private final Challenge[] challenges;
	
	protected Categorie(String name, Difficulty difficulty, Challenge[] challenges) {
		this.name = name;
		this.difficulty = difficulty;
		this.challenges = challenges;
		for(Challenge chall : challenges) {
			
			chall.setCategorie(this);
			
		}
	}
	
}
