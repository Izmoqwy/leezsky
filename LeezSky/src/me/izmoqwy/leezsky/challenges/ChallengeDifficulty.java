/*
 * That file is a part of [HFS] Test
 * Copyright Izmoqwy
 * Created the 20 juil. 2018
 * You can edit for your personal use.
 * You're not allowed to redistribute it at your own.
 */

package me.izmoqwy.leezsky.challenges;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ChallengeDifficulty {

	EASY("§aFacile", 1), MEDIUM("§eMoyen", 2), HARD("§cDifficile", 3), HARDCORE("§4Hardcore", 4);

	private final String text;

	@Getter
	private final int power;

	@Override
	public String toString() {
		return text;
	}

}
