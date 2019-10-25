package me.izmoqwy.leezsky.managers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lz.izmoqwy.core.Economy;
import lz.izmoqwy.core.GUIManager;
import lz.izmoqwy.core.LeezCore;
import lz.izmoqwy.core.api.database.SQLDatabase;
import lz.izmoqwy.core.api.database.exceptions.SQLActionImpossibleException;
import lz.izmoqwy.core.utils.InventoryUtil;
import lz.izmoqwy.core.utils.ItemUtil;
import lz.izmoqwy.core.utils.StoreUtil;
import me.izmoqwy.leezsky.LeezSky;
import me.izmoqwy.leezsky.commands.InvestCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;

public class InvestManager {

	public static final String GUI_NAME = "§eInvest",
			GUI_DEPOSIT_NAME = GUI_NAME + " §8» §aPlacer",
			GUI_WITHDRAW_NAME = GUI_NAME + " §8» §2Retrait",

	GUI_DEPOSIT_CONFIRM_NAME = GUI_NAME + " §8» §aConfirm §7-%s",
			GUI_WITHDRAW_CONFIRM_NAME = GUI_NAME + " §8» §2Confirm";

	private static final String CURRENCY = "$";
	private static final ItemStack GO_BACK = ItemUtil.createItem(Material.BLAZE_ROD, "§cRetour", Collections.singletonList("§7Retourner au menu"));
	private static final Inventory DEPOSIT_INVENTORY = Bukkit.createInventory(null, 9 * 3, GUI_DEPOSIT_NAME),
			WITHDRAW_INVENTORY = Bukkit.createInventory(null, 9 * 3, GUI_WITHDRAW_NAME),

	CONFIRM_DEPOSIT_INVENTORY = Bukkit.createInventory(null, 9 * 3, GUI_DEPOSIT_CONFIRM_NAME),
			CONFIRM_WITHDRAW_INVENTORY = Bukkit.createInventory(null, 9 * 3, GUI_WITHDRAW_CONFIRM_NAME);

	static {
		final ChatColor COLOR = ChatColor.YELLOW;
		DEPOSIT_INVENTORY.setItem(2, ItemUtil.createItem(Material.STICK, COLOR + "10" + CURRENCY));
		DEPOSIT_INVENTORY.setItem(4, ItemUtil.createItem(Material.COAL, COLOR + "100" + CURRENCY));
		DEPOSIT_INVENTORY.setItem(6, ItemUtil.createItem(Material.IRON_INGOT, COLOR + "1 000" + CURRENCY));
		DEPOSIT_INVENTORY.setItem(12, ItemUtil.createItem(Material.GOLD_INGOT, COLOR + "10 000" + CURRENCY));
		DEPOSIT_INVENTORY.setItem(14, ItemUtil.createItem(Material.EMERALD, COLOR + "100 000" + CURRENCY));
		DEPOSIT_INVENTORY.setItem(16, ItemUtil.createItem(Material.DIAMOND, COLOR + "1 000 000" + CURRENCY));

		for (Inventory inv : Arrays.asList(CONFIRM_DEPOSIT_INVENTORY, CONFIRM_WITHDRAW_INVENTORY)) {
			inv.setItem(15, ItemUtil.createItem(new MaterialData(Material.WOOL, (byte) 14), "§cRefuser"));
			inv.setItem(11, ItemUtil.createItem(new MaterialData(Material.WOOL, (byte) 5), "§aConfirmer"));
		}

		for (Inventory inv : Arrays.asList(DEPOSIT_INVENTORY, WITHDRAW_INVENTORY))
			inv.setItem(18, GO_BACK);
	}

	public static SQLDatabase.Table TABLE;
	public static HashMap<UUID, Map.Entry<Long, Double>> map = Maps.newHashMap();
	private static final HashMap<Integer, Integer> amounts = new HashMap<Integer, Integer>() {
		private static final long serialVersionUID = 1L;

		{
			put(2, 10);
			put(4, 1000);
			put(6, 100000);
			put(12, 100);
			put(14, 10000);
			put(16, 1000000);
		}
	};

	private static void investMoney(Player player, double amount) {
		StoreUtil.addOrReplaceInMap(map, player.getUniqueId(), Maps.immutableEntry(System.currentTimeMillis(), amount), current -> Maps.immutableEntry(current.getKey(), current.getValue() + amount));

		try {
			final long timestamp = map.get(player.getUniqueId()).getKey();
			final double invested = map.get(player.getUniqueId()).getValue();

			if (!TABLE.hasResult("invested", "uuid", player.getUniqueId().toString())) {
				LeezSky.DB.execute("INSERT INTO Invests(uuid, invested, at) VALUES (\"{id}\", {invested}, {timestamp})"
						.replace("{id}", player.getUniqueId().toString())
						.replace("{invested}", invested + "")
						.replace("{timestamp}", timestamp + "")
				);
			}
			else {
				TABLE.setDouble("invested", invested, "uuid", player.getUniqueId().toString());
				TABLE.setLong("at", timestamp, "uuid", player.getUniqueId().toString());
			}
			player.sendMessage(LeezCore.PREFIX + MessageFormat.format("§2Vous venez d''investir §e{0}" + CURRENCY + "§a.", amount));
		}
		catch (SQLActionImpossibleException | SQLException e) {
			player.sendMessage(LeezCore.PREFIX + "§4Erreur lors de la sauvegarde dans la BDD, contactez un administrateur rapidement si le problème persiste.");
			e.printStackTrace();
		}
	}

	private static void testMoney(Player player, double amount) {
		if (Economy.withdraw(player, amount)) {
			investMoney(player, amount);
			player.closeInventory();
		}
		else {
			player.sendMessage(LeezCore.PREFIX + "§cSolde insuffisant.");
		}
	}

	private static void guiDepositConfirm(Player player, int slot) {
		Inventory inv = InventoryUtil.copy(CONFIRM_DEPOSIT_INVENTORY, GUI_DEPOSIT_CONFIRM_NAME.replace("%s", slot + ""));
		List<String> informations = Lists.newArrayList();
		if (true)
			informations.addAll(Arrays.asList("§4Attention:", "§cSi vous récupérez l'argent avant 24h,", "§cLe montant sera diminué de 18%"));
		inv.setItem(13, ItemUtil.createItem(Material.BOOK, "§eInformations", informations));
		player.openInventory(inv);
	}

	private static void guiWithdrawConfirm(Player player) {
		Inventory inv = CONFIRM_WITHDRAW_INVENTORY;
		List<String> informations = Arrays.asList("§aVoulez-vous vraiment récupérer le fruit de votre investissement?", "§6Montant: §e" + Economy.round(map.get(player.getUniqueId()).getValue()));
		inv.setItem(13, ItemUtil.createItem(Material.BOOK, "§eInformations", informations));
		player.openInventory(inv);
	}

	public static void load() {
		GUIManager.GUIActionsBuilder menuBuilder = new GUIManager.GUIActionsBuilder();

		menuBuilder.onSlot(11, (player, event) -> {
			player.openInventory(DEPOSIT_INVENTORY);
			return true;
		});
		menuBuilder.onSlot(15, (player, event) -> {
			Inventory withdraw = WITHDRAW_INVENTORY;

			if (!map.containsKey(player.getUniqueId())) {
				withdraw.setItem(13, ItemUtil.createItem(new MaterialData(Material.WOOL, (byte) 14), "§cVous n'avez aucun investissement"));
			}
			else {
				int hours = (int) (System.currentTimeMillis() - (map.getOrDefault(player.getUniqueId(),
						Maps.immutableEntry(System.currentTimeMillis(), 0D))).getKey()) / 3600000;
				if (hours >= 3) {
					double solde = map.get(player.getUniqueId()).getValue();
					for (int i = 0; i < hours; i++) {
						if (i > 168)
							break;
						solde += (0.76 / 100) * solde;
					}
					if (hours < 24)
						solde *= .82;

					withdraw.setItem(13, ItemUtil.createItem(Material.HOPPER, "§aRécuperer", Collections.singletonList("§6Montant: §e" + solde + CURRENCY + "§6.")));
				}
				else {
					withdraw.setItem(13, ItemUtil.createItem(new MaterialData(Material.WOOL, (byte) 7), "§6Votre investissement n'est pas encore prêt", Collections.singletonList("§7Vous avez investi votre argent il y  moins de 3 heures.")));
				}
			}
			player.openInventory(withdraw);
			return true;
		});

		GUIManager.registerInventory(GUI_NAME, menuBuilder.build());

		/*
			Deposit listeners
		 */
		GUIManager.registerInventory(GUI_DEPOSIT_NAME, new GUIManager.GUIActionsBuilder().onSlot(-1, (player, event) -> {
			if (event.getSlot() == 18) {
				player.openInventory(InvestCommand.INVENTORY);
				return true;
			}
			if (amounts.containsKey(event.getSlot()))
				guiDepositConfirm(player, event.getSlot());
			return true;
		}).build());
		GUIManager.registerInventory(GUI_DEPOSIT_CONFIRM_NAME, new GUIManager.GUIActionsBuilder().onSlot(-1, (player, event) -> {
			if (event.getSlot() == 15) {
				player.openInventory(InvestCommand.INVENTORY);
				return true;
			}

			if (event.getSlot() == 11) {
				String[] splitted = event.getClickedInventory().getName().split("-");
				if (splitted.length == 2) {
					int amount = amounts.get(Integer.parseInt(ChatColor.stripColor(splitted[1])));
					testMoney(player, amount);
				}
			}
			return true;
		}).build());

		/*
			Withdraw listeners
		 */
		GUIManager.registerInventory(GUI_WITHDRAW_NAME, new GUIManager.GUIActionsBuilder().onSlot(-1, (player, event) -> {
			if (event.getSlot() == 18) {
				player.openInventory(InvestCommand.INVENTORY);
				return true;
			}
			if (event.getSlot() == 13) {
				if (!map.containsKey(player.getUniqueId())) {
					player.closeInventory();
					player.sendMessage(LeezCore.PREFIX + "§cVous n'avez aucun investissement en cours.");
				}
				else
					guiWithdrawConfirm(player);
			}
			return true;
		}).build());
		GUIManager.registerInventory(GUI_WITHDRAW_CONFIRM_NAME, new GUIManager.GUIActionsBuilder().onSlot(-1, (player, event) -> {
			if (event.getSlot() == 15) {
				player.openInventory(InvestCommand.INVENTORY);
				return true;
			}

			if (event.getSlot() == 11) {

				int hours = (int) (System.currentTimeMillis() - (map.getOrDefault(player.getUniqueId(),
						Maps.immutableEntry(System.currentTimeMillis(), 0D))).getKey()) / 3600000;
				if (hours >= 3) {
					double solde = map.get(player.getUniqueId()).getValue();
					for (int i = 0; i < hours; i++) {
						if (i > 168)
							break;
						solde += (0.76 / 100) * solde;
					}
					if (hours < 24)
						solde *= .82;

					map.remove(player.getUniqueId());
					try {
						TABLE.delete("uuid", player.getUniqueId().toString());
					}
					catch (SQLException e) {
						e.printStackTrace();
					}

					Economy.deposit(player, solde);
					player.sendMessage(LeezCore.PREFIX + "§aVous avez récupé §e" + Economy.round(solde) + CURRENCY + "§a grâce à votre investissement.");
					player.closeInventory();
				}
				else {
					player.sendMessage(LeezCore.PREFIX + "§cVeuillez attendre au minimum 3 heures avant de retirer votre investissement.");
				}
			}

			return true;
		}).build());
	}

}
