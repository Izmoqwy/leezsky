/*
 * That file is a part of [HFS] Test
 * Copyright Izmoqwy
 * Created the 20 juil. 2018
 * You can edit for your personal use.
 * You're not allowed to redistribute it at your own.
 */

package me.izmoqwy.leezsky.challenges;

public enum Difficulty {

	EASY("§aFacile", 1), MEDIUM("§eMoyen", 2), HARD("§cDifficile", 3), HARDCORE("§4Très dûr", 4);
	
	private final String text;
	private final int power;
	
	Difficulty(String text, int power) {
		
		this.text = text;
		this.power = power;
		
	}
	
	@Override
	public String toString() {
		
		return text;
		
	}
	
	public int getPower() {
		
		return power;
		
	}
	
}
