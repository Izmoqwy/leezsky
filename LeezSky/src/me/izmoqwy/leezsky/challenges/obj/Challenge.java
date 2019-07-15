package me.izmoqwy.leezsky.challenges.obj;

import lombok.Getter;
import lombok.Setter;
import me.izmoqwy.leezsky.challenges.ChallengePlugin;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

public final class Challenge {

	@Getter
	private final String name, description;
	@Getter
	private final ItemStack icon;
	@Getter
	private final ItemStack[] needed, rewards;
	@Getter
	private final double rewardMoney;
	@Getter
	private final long levelNeeded;
	@Getter
	private Method method = null;

	@Getter @Setter
	private Categorie categorie;

	protected Challenge(final String name, final ItemStack icon, final String desc, final ItemStack[] needed, final long levelNeeded, final ItemStack[] rewards, final double rewardMoney) {
		this.name = name;
		this.description = desc;
		this.icon = icon;
		this.needed = needed;
		this.rewards = rewards;
		this.rewardMoney = rewardMoney;
		this.levelNeeded = levelNeeded;
	}

	protected Challenge(final String name, final ItemStack icon, final String desc, final ItemStack[] rewards, final double rewardMoney,
						final String methodName) {
		this(name, icon, desc, new ItemStack[0], 0, rewards, rewardMoney);
		try {

			Method method = ChallengePlugin.class.getDeclaredMethod(methodName, Player.class);
			method.setAccessible(true);
			this.method = method;

		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	protected Challenge(String name, ItemStack icon, String desc, long levelNeeded, ItemStack[] rewards, double rewardMoney) {
		this(name, icon, desc, new ItemStack[0], levelNeeded, rewards, rewardMoney);
	}

	protected Challenge(String name, ItemStack icon, String desc, ItemStack[] needed, ItemStack[] rewards, double rewardMoney) {
		this(name, icon, desc, needed, 0, rewards, rewardMoney);
	}

	protected Challenge(String name, ItemStack icon, String desc, ItemStack[] needed, double rewardMoney) {
		this(name, icon, desc, needed, 0, new ItemStack[0], rewardMoney);
	}

	protected Challenge(String name, ItemStack icon, String desc, ItemStack[] needed, ItemStack[] rewards) {
		this(name, icon, desc, needed, 0, rewards, 0);
	}

}
