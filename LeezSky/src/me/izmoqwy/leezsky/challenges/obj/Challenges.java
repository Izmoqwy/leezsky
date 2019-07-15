package me.izmoqwy.leezsky.challenges.obj;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lz.izmoqwy.core.utils.ItemUtil;
import me.izmoqwy.leezsky.challenges.ChallengePlugin;
import me.izmoqwy.leezsky.challenges.Difficulty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.*;

public class Challenges {

	private static final ItemStack itm_band = ItemUtil.createItem(md(Material.STAINED_GLASS_PANE, (byte) 5), "§a*"), item_info = ItemUtil.createItem(Material.BOOK, "§3Défis", Collections.singletonList("§bLes défis vous aident à avancer."));

	public static final List<Categorie> categories = getCategories();
	public static final Map<ItemStack, Challenge> icons = getIcons();

	public static final Map<Enchantment, Integer> enchants = new HashMap<Enchantment, Integer>() {
		private static final long serialVersionUID = 1L;

		{
			put(Enchantment.ARROW_FIRE, 1);
		}
	};

	private static ItemStack[] is(ItemStack... itemStacks) {
		return itemStacks;
	}

	private static MaterialData md(Material material, int data) {
		return new MaterialData(material, (byte) data);
	}

	private static List<Categorie> getCategories() {
		List<Categorie> list = Lists.newLinkedList();

		list.add(new Categorie("Arrivant", Difficulty.EASY, new Challenge[]{

				new Challenge("Nourriture primaire", new ItemStack(Material.BREAD), "Faîtes 21 pains",
						is(ItemUtil.createItem(Material.BREAD, 21)), is(ItemUtil.createItem(Material.POTATO_ITEM, 8))),

				new Challenge("Moi aussi je sais pêcher", new ItemStack(Material.RAW_FISH), "Pêchez 9 poissons crus",
						is(ItemUtil.createItem(Material.RAW_FISH, 9)), is(ItemUtil.createItem(Material.CARROT_ITEM, 8))),

				new Challenge("Apprenti fermier", new ItemStack(Material.WHEAT), "Cultivez 24 blés, 24 pommes de terre, 24 carottes.",
						is(ItemUtil.createItem(Material.WHEAT, 24), ItemUtil.createItem(Material.POTATO_ITEM, 24), ItemUtil.createItem(Material.CARROT_ITEM, 24))
						, is(ItemUtil.createItem(Material.SUGAR_CANE, 12))),

				new Challenge("Sucre dans mon café", new ItemStack(Material.SUGAR), "Craftez 96 sucres",
						is(ItemUtil.createItem(Material.SUGAR, 96)), is(ItemUtil.createItem(md(Material.INK_SACK, 15), 32))),

				new Challenge("Compote", new ItemStack(Material.APPLE), "Ramassez 24 pommes",
						is(ItemUtil.createItem(Material.APPLE, 24)), is(ItemUtil.createItem(Material.GLASS, 32)), 130),

				new Challenge("Enclos à vaches", new ItemStack(Material.RAW_BEEF), "Ramenez 48 viandes de vache.",
						is(ItemUtil.createItem(Material.RAW_BEEF, 48)), is(ItemUtil.createItem(Material.COOKIE, 32)), 160),

				new Challenge("Enclos à cochons", new ItemStack(Material.PORK), "Ramenez 48 viandes de cochon.",
						is(ItemUtil.createItem(Material.PORK, 48)), is(ItemUtil.createItem(Material.COOKIE, 32)), 160),

				new Challenge("Enclos à poulets", new ItemStack(Material.RAW_CHICKEN), "Ramenez 48 viandes de poulet.",
						is(ItemUtil.createItem(Material.RAW_CHICKEN, 48)), is(ItemUtil.createItem(Material.COOKIE, 32)), 160),

				new Challenge("Enclos à moutons", new ItemStack(Material.MUTTON), "Ramenez 48 viandes de mouton.",
						is(ItemUtil.createItem(Material.MUTTON, 48)), is(ItemUtil.createItem(Material.COOKIE, 32)), 160),

				new Challenge("Fleur jaune", new ItemStack(Material.YELLOW_FLOWER), "Ramassez 28 fleurs jaunes",
						is(ItemUtil.createItem(Material.YELLOW_FLOWER, 28)), is(ItemUtil.createItem(md(Material.SAPLING, 2), 6))),

				new Challenge("Fleur rouge", new ItemStack(Material.RED_ROSE), "Ramassez 28 fleurs rouges",
						is(ItemUtil.createItem(Material.RED_ROSE, 28)), is(ItemUtil.createItem(md(Material.SAPLING, 4), 6))),

				new Challenge("Cactus", new ItemStack(Material.CACTUS), "Ramassez 48 cactus",
						is(ItemUtil.createItem(Material.CACTUS, 48)), 152),

				new Challenge("Mangeur de chêne", new ItemStack(Material.LOG), "Ramenez 96 bûches de bois de chêne",
						is(ItemUtil.createItem(Material.LOG, 96)), 90),

				new Challenge("Mangeur de sapin", new ItemStack(Material.LOG, 1, (byte) 1), "Ramenez 96 bûches de bois de sapin",
						is(ItemUtil.createItem(md(Material.LOG, 1), 1)), 110),

				new Challenge("Mangeur de bouleau", new ItemStack(Material.LOG, 1, (byte) 2), "Ramenez 96 bûches de bois de bouleau",
						is(ItemUtil.createItem(md(Material.LOG, 2), 96)), 110),

				new Challenge("Ogre de la jungle", new ItemStack(Material.LOG, 1, (byte) 3), "Ramenez 148 bûches de bois d'acajou",
						is(ItemUtil.createItem(md(Material.LOG, 3), 148)), 240),

				new Challenge("Ogre de la savane", new ItemStack(Material.LOG_2), "Ramenez 148 bûches de bois d'acacia",
						is(ItemUtil.createItem(Material.LOG_2, 148)), 240),

				new Challenge("Ogre obscure", new ItemStack(Material.LOG_2, 1, (byte) 1), "Ramenez 148 bûches de bois de chêne noir",
						is(ItemUtil.createItem(md(Material.LOG_2, 1), 148)), 240)

		}));
		list.add(new Categorie("Interessé", Difficulty.EASY, new Challenge[]{

				new Challenge("En création...", new ItemStack(Material.BEDROCK), "Cette catégorie sera remplie à une prochaine MAJ.",
						new ItemStack[0], 0, "impossible")

		}));
		list.add(new Categorie("Farmeur", Difficulty.MEDIUM, new Challenge[]{
				new Challenge("En création...", new ItemStack(Material.BEDROCK), "Cette catégorie sera remplie à une prochaine MAJ.",
						new ItemStack[0], 0, "impossible")

		}));
		list.add(new Categorie("Astucieux", Difficulty.MEDIUM, new Challenge[]{
				new Challenge("En création...", new ItemStack(Material.BEDROCK), "Cette catégorie sera remplie à une prochaine MAJ.",
						new ItemStack[0], 0, "impossible")

		}));
		list.add(new Categorie("Enraçiné", Difficulty.HARD, new Challenge[]{

				new Challenge("En création...", new ItemStack(Material.BEDROCK), "Cette catégorie sera remplie à une prochaine MAJ.",
						new ItemStack[0], 0, "impossible")

		}));
		list.add(new Categorie("Challenger", Difficulty.HARD, new Challenge[]{

				new Challenge("En création...", new ItemStack(Material.BEDROCK), "Cette catégorie sera remplie à une prochaine MAJ.",
						new ItemStack[0], 0, "impossible")

		}));
		list.add(new Categorie("Maître", Difficulty.HARDCORE, new Challenge[]{

				new Challenge("En création...", new ItemStack(Material.BEDROCK), "Cette catégorie sera remplie à une prochaine MAJ.",
						new ItemStack[0], 0, "impossible")

		}));
		return list;
	}

		private static Map<ItemStack, Challenge> getIcons () {
			Map<ItemStack, Challenge> map = Maps.newHashMap();
			for (Categorie cat : categories) {
				for (Challenge chall : cat.getChallenges()) {
					map.put(setup(chall), chall);
				}
			}
			return map;

		}

		@SuppressWarnings("deprecation")
		private static ItemStack setup (Challenge chall){
			ItemStack icon = chall.getIcon();
			return ItemUtil.createItem(md(icon.getType(), icon.getData().getData()), icon.getAmount(), "§3" + chall.getName(), Arrays.asList("§b" + chall.getDescription(), "", "§3Récompense: §e" + chall.getRewardMoney() + "€"));
		}

		@SuppressWarnings("deprecation")
		private static ItemStack setup (Player player, Challenge chall){
			ItemStack icon = chall.getIcon();
			if (ChallengePlugin.instance.isDone(player, chall)) {
				return ItemUtil.createItem(md(icon.getType(), icon.getData().getData()), icon.getAmount(), "§9" + chall.getName(),
						Collections.singletonList("§cVous avez déjà réussi ce défi."), enchants, ItemFlag.HIDE_ENCHANTS);

			}
			return setup(chall);
		}

		public static Inventory getInventory (Player player, Categorie categorie){
			Inventory main = Bukkit.createInventory(null, 4 * 9, "§3Défis");

			int i = 0;
			boolean showRest = true;
			for (Categorie cat : categories) {

				if (!ChallengePlugin.instance.canAccess(player, cat)) showRest = false;
				if (showRest && ChallengePlugin.instance.getRest(player, cat) == 0)
					main.setItem(i, ItemUtil.createItem(Material.PAPER, "§3Palier §b" + cat.getName(),
							Arrays.asList("§3Difficulté: " + cat.getDifficulty().toString(), "§2Vous avez tout completé ici."), enchants, ItemFlag.HIDE_ENCHANTS));
				else main.setItem(i, ItemUtil.createItem(Material.PAPER, "§3Palier §b" + cat.getName(),
						Arrays.asList("§3Difficulté: " + cat.getDifficulty().toString(), (showRest ? "§3Restant(s): §b" + ChallengePlugin.instance.getRest(player, cat) : ""))));
				i++;

			}

			main.setItem(8, item_info);
			doBand(main);

			i = 18;
			for (Challenge chall : categorie.getChallenges()) {
				ItemStack itm = setup(player, chall);
				if (itm.getAmount() > main.getMaxStackSize())
					main.setMaxStackSize(itm.getAmount());

				main.setItem(i, itm);
				i++;
			}
			return main;
		}

		private static void doBand (Inventory inventory){
			inventory.setItem(7, itm_band);
			for (int i = 9; i <= 17; i++) {
				inventory.setItem(i, itm_band);
			}
		}

	}
