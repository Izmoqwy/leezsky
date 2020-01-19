package me.izmoqwy.leezsky.challenges.obj;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import me.izmoqwy.leezsky.challenges.ChallengeDifficulty;
import me.izmoqwy.leezsky.challenges.ChallengesManager;
import org.bukkit.entity.Player;

@Getter
@EqualsAndHashCode
public final class ChallengeCategory {

	private final String name;
	private final ChallengeDifficulty difficulty;
	private final Challenge[] challenges;

	public ChallengeCategory(String name, ChallengeDifficulty difficulty, Challenge[] challenges) {
		this.name = name;
		this.difficulty = difficulty;
		this.challenges = challenges;

		for (Challenge challenge : challenges) {
			challenge.setCategory(this);
		}
	}

	public boolean canAccess(Player player) {
		return ChallengesManager.get.getCurrentDifficulty(player).getPower() >= getDifficulty().getPower();
	}

	public int getCompleted(Player player) {
		return ChallengesManager.get.getCompleted(player, this);
	}
	
	public int getRemaining(Player player) {
		return getChallenges().length - getCompleted(player);
	}

}
