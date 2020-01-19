package me.izmoqwy.leezsky.challenges;

import lz.izmoqwy.core.api.ItemBuilder;
import lz.izmoqwy.core.gui.MinecraftGUIListener;
import lz.izmoqwy.core.gui.UniqueMinecraftGUI;
import lz.izmoqwy.core.utils.ItemUtil;
import lz.izmoqwy.core.utils.TextUtil;
import me.izmoqwy.leezsky.LeezSky;
import me.izmoqwy.leezsky.challenges.obj.Challenge;
import me.izmoqwy.leezsky.challenges.obj.ChallengeCategory;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ChallengeCategoryGUI extends UniqueMinecraftGUI implements MinecraftGUIListener {

	private static final ItemStack SEP = new ItemBuilder(ItemUtil.quickItem(Material.STAINED_GLASS_PANE, 1, (byte) 5))
			.name(ChatColor.GREEN + "-*-")
			.toItemStack();

	private ChallengeCategory challengeCategory;

	public ChallengeCategoryGUI(Player player, ChallengeCategory challengeCategory) {
		super(null, ChatColor.DARK_AQUA + "Défis", player);
		this.challengeCategory = challengeCategory;

		ChallengesManager manager = ChallengesManager.get;

		// Setup "navbar"
		for (int i = 0; i < Math.min(manager.getCategories().size(), 7); i++) {
			ChallengeCategory category = manager.getCategories().get(i);
			boolean showRemaining = category.canAccess(player);

			int remaining = category.getRemaining(player);
			ItemBuilder itemBuilder = new ItemBuilder(Material.PAPER)
					.name(ChatColor.DARK_AQUA + "Palier " + ChatColor.AQUA + category.getName())
					.appendLore(ChatColor.DARK_AQUA + "Difficulté: " + category.getDifficulty().toString());

			if (showRemaining && remaining == 0) {
				itemBuilder.appendLore(ChatColor.GREEN + "Vous avez tout complété ici");
				itemBuilder.quickEnchant();
			}
			else if (showRemaining)
				itemBuilder.appendLore(ChatColor.DARK_AQUA + (remaining > 1 ? "Restants: " : "Restant: ") + ChatColor.AQUA + remaining);

			setItem(i, itemBuilder.toItemStack());
		}

		setItem(7, SEP);
		setItem(8, new ItemBuilder(Material.BOOK)
				.name(ChatColor.LIGHT_PURPLE + "Besoin d'aide ?")
				.appendLore(ChatColor.DARK_PURPLE + "/docs challenges")
				.toItemStack());

		for (int i = 9; i < 18; i++) {
			setItem(i, SEP);
		}

		// Setup challenges
		for (int i = 0; i < challengeCategory.getChallenges().length; i++) {
			Challenge challenge = challengeCategory.getChallenges()[i];
			ItemBuilder itemBuilder = new ItemBuilder(challenge.getIcon());

			if (manager.isDone(player, challenge)) {
				itemBuilder.name(ChatColor.BLUE + challenge.getName())
						.appendLore(ChatColor.GREEN, "Vous avez déjà complété ce défi")
						.quickEnchant();
			}
			else {
				itemBuilder.name(ChatColor.DARK_AQUA + challenge.getName())
						.appendLore(ChatColor.AQUA, challenge.getDescription(), "");

				if (!challenge.getRewards().isEmpty())
					itemBuilder.appendLore(ChatColor.DARK_AQUA + "Récompenses: " + ChatColor.BLUE + challenge.getRewards().size());
				if (challenge.getRewardMoney() > 0)
					itemBuilder.appendLore(ChatColor.DARK_AQUA + "Récompense monétaire: " + ChatColor.YELLOW + TextUtil.humanReadableNumber(challenge.getRewardMoney()) + "$");
			}

			setItem(i, itemBuilder.toItemStack(), true);
		}

		addListener(this);
	}

	@Override
	public void onClick(Player player, ItemStack clickedItem, int slot) {
		if (slot < 7 && clickedItem.getType() == Material.PAPER) {
			ChallengeCategory newCategory = ChallengesManager.get.getCategories().get(slot);
			if (challengeCategory.equals(newCategory))
				return;

			if (newCategory.canAccess(player)) {
				new ChallengeCategoryGUI(player, newCategory).open(player);
			}
			else {
				player.closeInventory();
				player.sendMessage(LeezSky.PREFIX + "§cVous n'avez pas encore au palier §b" + newCategory.getName() + "§c");
				player.sendMessage("§6Vous devez avoir fini tous les paliers de difficulté " + ChallengesManager.get.getCurrentDifficulty(player) + "§6 pour accéder au palier " +
						"supérieur.");
			}
		}
		else if (slot >= 18) {
			Challenge challenge = challengeCategory.getChallenges()[slot - 18];
			if (challenge == null)
				return;

			if (challenge.getPredicate() != null && !challenge.getPredicate().test(player)) {
				player.sendMessage(LeezSky.PREFIX + ChatColor.RED + "Vous ne remplissez pas la condition demandée.");
				return;
			}

			for (ItemStack required : challenge.getRequired()) {
				if (!player.getInventory().containsAtLeast(new ItemBuilder(required).amount(1).toItemStack(), required.getAmount())) {
					player.closeInventory();
					player.sendMessage(LeezSky.PREFIX + ChatColor.RED + "Vous n'avez pas (assez) de " + required.getType().name() + ". (Il en faut " + required.getAmount() + ")");
					return;
				}
			}

			ItemUtil.take(player, challenge.getRequired().toArray(new ItemStack[0]));
			ChallengesManager.get.complete(player, challenge);
			new ChallengeCategoryGUI(player, ChallengesManager.get.getCurrentCategory(player));
		}

	}

}