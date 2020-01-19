package me.izmoqwy.leezsky.challenges.obj;

import lombok.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Predicate;

@Getter
@Builder
public final class Challenge {

	private final String identifier, name, description;
	private final ItemStack icon;

	@Singular("require")
	private final List<ItemStack> required;
	@Singular
	private final List<ItemStack> rewards;

	private double rewardMoney;
	private Predicate<Player> predicate;

	@Setter
	private ChallengeCategory category;

}
