package me.izmoqwy.leezsky.challenges;

import com.google.common.collect.Lists;
import lombok.Getter;
import lz.izmoqwy.core.Economy;
import lz.izmoqwy.core.PlayerDataStorage;
import lz.izmoqwy.core.utils.ItemUtil;
import lz.izmoqwy.core.utils.StoreUtil;
import me.izmoqwy.leezsky.LeezSky;
import me.izmoqwy.leezsky.challenges.obj.Challenge;
import me.izmoqwy.leezsky.challenges.obj.ChallengeCategory;
import me.izmoqwy.leezsky.managers.SettingsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.List;

@Getter
public class ChallengesManager {

	public static final ChallengesManager get = new ChallengesManager();

	private List<ChallengeCategory> categories;

	public void load(Plugin plugin) {
		File file = new File(plugin.getDataFolder(), "challenges.yml");
		if (!StoreUtil.copyTemplateIfMissing(file, LeezSky.getInstance().getResource("templates/challenges.yml")))
			return;

		categories = loadCategories(YamlConfiguration.loadConfiguration(file));
	}

	private List<ChallengeCategory> loadCategories(YamlConfiguration configuration) {
		List<ChallengeCategory> categories = Lists.newLinkedList();

		ConfigurationSection categoriesSection = configuration.getConfigurationSection("categories");
		if (categoriesSection == null)
			return categories;

		for (String categoryKey : categoriesSection.getKeys(false)) {
			ConfigurationSection categorySection = categoriesSection.getConfigurationSection(categoryKey);
			ChallengeDifficulty categoryDifficulty = ChallengeDifficulty.EASY;
			try {
				categoryDifficulty = ChallengeDifficulty.valueOf(categorySection.getString("difficulty", "EASY").toUpperCase());
			}
			catch (IllegalArgumentException ex) {
				LeezSky.getInstance().getLogger().warning("[Challenges] Unknown difficulty for category '" + categoryKey + "'");
			}

			List<Challenge> challenges = Lists.newArrayList();

			ConfigurationSection listSection = categorySection.getConfigurationSection("list");
			if (listSection != null) {
				for (String challengeKey : listSection.getKeys(false)) {
					ConfigurationSection challengeSection = listSection.getConfigurationSection(challengeKey);

					Challenge.ChallengeBuilder challengeBuilder = Challenge.builder()
							.identifier(challengeSection.getString("id",
									challengeSection.getString("name", "Sans nom").replace(' ', '_').toLowerCase()
							))
							.name(challengeSection.getString("name", "Sans nom"))
							.description(challengeSection.getString("description", "Aucune description"));

					ItemStack icon = StoreUtil.itemStackFromYAML(challengeSection.getConfigurationSection("icon"));
					if (icon == null) {
						LeezSky.getInstance().getLogger().warning("[Challenges] Invalid icon for challenge '" + challengeKey + "' in category '" + categoryKey + "'");
						challengeBuilder.icon(new ItemStack(Material.STONE));
					}
					else
						challengeBuilder.icon(icon);

					if (challengeSection.isConfigurationSection("required")) {
						ConfigurationSection allRequiredSection = challengeSection.getConfigurationSection("required");
						for (String requiredKey : allRequiredSection.getKeys(false)) {
							ConfigurationSection requiredSection = allRequiredSection.getConfigurationSection(requiredKey);
							ItemStack reward = StoreUtil.itemStackFromYAML(requiredSection);
							if (reward == null) {
								LeezSky.getInstance().getLogger().warning("[Challenges] Invalid item for required '" + requiredKey + "' in challenge '" + challengeKey + "' in " +
										"category '" + categoryKey + "'");
								continue;
							}

							challengeBuilder.reward(reward);
						}
					}

					if (challengeSection.isConfigurationSection("rewards")) {
						ConfigurationSection rewardsSection = challengeSection.getConfigurationSection("rewards");
						for (String rewardKey : rewardsSection.getKeys(false)) {
							ConfigurationSection rewardSection = rewardsSection.getConfigurationSection(rewardKey);
							ItemStack reward = StoreUtil.itemStackFromYAML(rewardSection);
							if (reward == null) {
								LeezSky.getInstance().getLogger().warning("[Challenges] Invalid item for reward '" + rewardKey + "' in challenge '" + challengeKey + "' in " +
										"category '" + categoryKey + "'");
								continue;
							}

							challengeBuilder.reward(reward);
						}
					}
					challengeBuilder.rewardMoney(challengeSection.getDouble("rewardMoney", 0));

					challenges.add(challengeBuilder.build());
				}
			}

			categories.add(new ChallengeCategory(categorySection.getString("name", "Sans nom"), categoryDifficulty, challenges.toArray(new Challenge[0])));
		}

		return categories;
	}

	private void broadcast(String message) {
		String finalMessage = message != null ? "§3✸ " + message : " ";
		Bukkit.getOnlinePlayers().stream()
				.filter(player -> SettingsManager.RECEIVE_CHALLENGES.getState(player) == SettingsManager.SimpleToggle.ON)
				.forEach(player -> player.sendMessage(finalMessage));
	}

	public boolean isDone(Player player, Challenge challenge) {
		return PlayerDataStorage.get(player, "challenges.done", Lists.newArrayList()).contains(challenge.getIdentifier());
	}

	public void complete(Player player, Challenge challenge) {
		if (isDone(player, challenge))
			return;

		if (challenge.getRewardMoney() > 0)
			Economy.deposit(player, challenge.getRewardMoney());
		ItemUtil.give(player, challenge.getRewards().toArray(new ItemStack[0]));

		broadcast(ChatColor.GREEN + "Bravo à " + ChatColor.YELLOW + player.getName() + ChatColor.GREEN + " pour avoir réussi le défi " + ChatColor.DARK_GREEN + challenge.getName() + ChatColor.GREEN + " !");

		YamlConfiguration yaml = PlayerDataStorage.yaml(player);
		List<String> done = yaml.getStringList("challenges.done");
		if (done == null)
			done = Lists.newArrayList();

		done.add(challenge.getIdentifier());
		yaml.set("challenges.done", done);

		ChallengeCategory category = challenge.getCategory();
		yaml.set("challenges.completed", getCompleted(player, category) + 1);

		if (category.getRemaining(player) == 0) {
			ChallengeDifficulty currentDifficulty = getCurrentDifficulty(player);

			for (ChallengeCategory challengeCategory : getCategories()) {
				if (challengeCategory.getDifficulty() == currentDifficulty && challengeCategory.getRemaining(player) > 0) {
					yaml.set("challenges.currentIndex", getCategories().indexOf(challengeCategory));
					break;
				}
			}

			if (currentDifficulty == ChallengeDifficulty.HARDCORE) {
				broadcast(null);
				broadcast(ChatColor.GOLD + "Bravo à " + ChatColor.YELLOW + player.getName() + ChatColor.GOLD + " pour avoir terminé tous les défis !");
				broadcast(null);
			}
			else {
				ChallengeDifficulty newDifficulty = ChallengeDifficulty.values()[currentDifficulty.ordinal() + 1];

				find:
				while (newDifficulty != null) {
					for (ChallengeCategory challengeCategory : getCategories()) {
						if (challengeCategory.getDifficulty() == newDifficulty && challengeCategory.getRemaining(player) > 0) {
							yaml.set("challenges.currentIndex", getCategories().indexOf(challengeCategory));
							break find;
						}
					}

					if (currentDifficulty.ordinal() == ChallengeDifficulty.values().length)
						newDifficulty = null;
					else
						newDifficulty = ChallengeDifficulty.values()[currentDifficulty.ordinal() + 1];
				}

				if (newDifficulty != null) {
					broadcast(ChatColor.YELLOW + player.getName() + ChatColor.DARK_GREEN + " passe au palier " + newDifficulty + ChatColor.DARK_GREEN + " !");
				}
			}
		}
		PlayerDataStorage.save(player);
	}

	private int getCurrentCategoryIndex(Player player) {
		return PlayerDataStorage.get(player, "challenges.currentIndex", 0);
	}

	public ChallengeCategory getCurrentCategory(Player player) {
		return getCategories().get(getCurrentCategoryIndex(player));
	}

	public ChallengeDifficulty getCurrentDifficulty(Player player) {
		return getCurrentCategory(player).getDifficulty();
	}

	public int getCompleted(Player player, ChallengeCategory category) {
		return PlayerDataStorage.get(player, "challenges.categories." + category.getName() + ".completed", 0);
	}

}
