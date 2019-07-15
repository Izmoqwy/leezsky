package lz.izmoqwy.invest;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import lz.izmoqwy.invest.commands.InvestCommand;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import com.google.common.collect.Maps;

import lz.izmoqwy.api.api.database.exceptions.SQLActionImpossibleException;
import lz.izmoqwy.api.utils.Items;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.EconomyResponse;

public class Inventaire implements Listener {

	/*
	 * 2 : 10 4 : 1000 6 : 100 000 12 : 100 14: 10 000 16: 1 000 000
	 */

	protected static HashMap<UUID, Entry<Long, Double>> map = Maps.newHashMap();
	private final HashMap<Integer, Integer> amounts = new HashMap<Integer, Integer>() {
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

	public void addBalance(Player player, double amount) {
		if (map.containsKey(player.getUniqueId())) {
			Entry<Long, Double> current = map.get(player.getUniqueId());
			map.put(player.getUniqueId(), Maps.immutableEntry(current.getKey(), current.getValue() + amount));
		} else
			map.put(player.getUniqueId(), Maps.immutableEntry(System.currentTimeMillis() - 3600000, amount));
		try {

			try {
				Money.db.execute("INSERT INTO Invests(uuid, invested, at) VALUES (\"" + player.getUniqueId().toString()
						+ "\", -1, -1)");
			} catch (SQLException e) {
			}
			Money.INVESTS.setDouble("invested", map.get(player.getUniqueId()).getValue(), "uuid",
					player.getUniqueId().toString());
			Money.INVESTS.setLong("at", map.get(player.getUniqueId()).getKey(), "uuid",
					player.getUniqueId().toString());

		} catch (SQLActionImpossibleException e) {
			player.sendMessage(Money.PREFIX + "�4Erreur lors de la sauvegarde, contactez Vasco si le probl�me persiste.");
			e.printStackTrace();
		}
	}

	public void testMoney(Player player, double amount) {
		if (Money.economy.withdrawPlayer(player, amount).type == EconomyResponse.ResponseType.SUCCESS) {
			addBalance(player, amount);
			player.sendMessage(Money.PREFIX + "�6Vous avez �t� d�bit� de �e" + amount + "$�6.");
		} else
			player.sendMessage(Money.PREFIX + "�cVous n'avez pas assez d'argent.");
	}
	
	public void guiConfirm(Player player, int slot) {
		
		Inventory confirm = Bukkit.createInventory(null, 9*3, "�2Confirmation �8� �7" + slot);
		
		confirm.setItem(13, Items.createItem(Material.BOOK, "�6Information", "//�4Attention ://�cSi vous r�cuperez l'argent avant 24h,//�cle montant sera diminu� de 18%." ));
		confirm.setItem(11, Items.createItem(Material.SLIME_BALL, "�2Valider"));
		confirm.setItem(15, Items.createItem(Material.STONE_BUTTON, "�4Refuser"));
		
		player.openInventory(confirm);
	}

	@EventHandler
	public void onClick(InventoryClickEvent event) {
		if ((event.getClick() == null) || (event.getCurrentItem() == null) || (event.getClickedInventory() == null)) {
			return;
		}

		if (Money.getInstance().setupEconomy()) {
			Player player = (Player) event.getWhoClicked();

			if (event.getClickedInventory().getName() == "�2Investissement") {
				event.setCancelled(true);
				if (event.getSlot() == 11) {
					Inventory retrait = Bukkit.createInventory(null, 27, "�2Placer de l'argent");

					retrait.setItem(2, Items.createItem(Material.STICK, "�e10$"));
					retrait.setItem(12, Items.createItem(Material.COAL, "�e100$"));
					retrait.setItem(4, Items.createItem(Material.IRON_INGOT, "�e1000$"));
					retrait.setItem(14, Items.createItem(Material.GOLD_INGOT, "�e10 000$"));
					retrait.setItem(6, Items.createItem(Material.EMERALD, "�e100 000$"));
					retrait.setItem(16, Items.createItem(Material.DIAMOND, "�e1 000 000$"));
					retrait.setItem(18, Items.createItem(Material.BLAZE_ROD, "�cRetour"));

					player.openInventory(retrait);
				}

				if (event.getSlot() == 15) {

					Inventory pickup = Bukkit.createInventory(null, 9 * 3, "�4Retrait de l'argent");

					pickup.setItem(18, Items.createItem(Material.BLAZE_ROD, "�cRetour"));
					if (!map.containsKey(player.getUniqueId()))
						pickup.setItem(13, Items.createItem(Material.HOPPER, "�cAucun investissement"));
					else {

						int hours = (int) (System.currentTimeMillis()
								- ((Entry<Long, Double>) map.getOrDefault(player.getUniqueId(),
										Maps.immutableEntry(System.currentTimeMillis(), 0D))).getKey())
								/ 3600000;
						if (hours >= 1) {
							double solde = map.get(player.getUniqueId()).getValue();
							for (int i = 0; i < hours; i++) {
								if(i > 168)
									break;
								solde += (0.76 / 100) * solde;
							}
							if(hours < 24)
								solde *= .82;	

							pickup.setItem(13, Items.createItem(Material.HOPPER, "�4R�cup�rer l'argent", 
									"�6A r�cuperer: �e" + solde + "$�6."));
						}
					}
					player.openInventory(pickup);
				}

			}

			else if (event.getClickedInventory().getName().equalsIgnoreCase("�2Placer de l'argent")) {
				event.setCancelled(true);
				if (event.getSlot() == 18) {
					player.openInventory(InvestCommand.inv);
					return;
				}
				if (amounts.containsKey(event.getSlot()))
					guiConfirm(player, event.getSlot());
			}
			else if (event.getClickedInventory().getName().startsWith("�2Confirmation"))
			{
				event.setCancelled(true);
				int amount = amounts.get(Integer.parseInt(
						ChatColor.stripColor(event.getClickedInventory().getName()).split("� ")[1]));
				
				if(event.getSlot() == 15) {
					player.openInventory(InvestCommand.inv);
				}
				if(event.getSlot() == 11) {
					testMoney(player, amount);
				}
			}

			else if (event.getClickedInventory().getName().equalsIgnoreCase("�4Retrait de l'argent")) {
				event.setCancelled(true);
				if (event.getSlot() == 18) {
					player.openInventory(InvestCommand.inv);
				}
				if (event.getSlot() == 13) {

					if (!map.containsKey(player.getUniqueId()))
						player.closeInventory();
					else {

						Inventory verif = Bukkit.createInventory(null, 9 * 3, "�1Confirmation");

						verif.setItem(18, Items.createItem(Material.BLAZE_ROD, "�cRetour"));
						verif.setItem(11, Items.createItem(Material.SLIME_BALL, "�2Valider"));
						verif.setItem(15, Items.createItem(Material.STONE_BUTTON, "�4Refuser", "�4Quitter le menu"));

						player.openInventory(verif);
					}
				}
			}

			else if (event.getClickedInventory().getName().equalsIgnoreCase("�1Confirmation")) {
				event.setCancelled(true);
				if (event.getSlot() == 18) {
					player.openInventory(InvestCommand.inv);
				}
				if (event.getSlot() == 11) {
					if (!map.containsKey(player.getUniqueId())) {
						player.sendMessage(Money.PREFIX + "�cAucun investissement en cours");
						return;
					}
					int hours = (int) (System.currentTimeMillis()
							- ((Entry<Long, Double>) map.getOrDefault(player.getUniqueId(),
									Maps.immutableEntry(System.currentTimeMillis(), 0D))).getKey())
							/ 3600000;
					if (hours >= 1) {
						double solde = map.get(player.getUniqueId()).getValue();
						for (int i = 0; i < hours; i++) {
							if(i > 168)
								break;
							solde += (0.76 / 100) * solde;
						}
						if(hours < 24)
							solde *= .82;

						Money.economy.depositPlayer(player, solde);
						player.sendMessage(Money.PREFIX + "�6Vous avez r�cuper� �e" + solde + "$�6.");
						map.remove(player.getUniqueId());
						try {
							Money.INVESTS.setDouble("invested", -1, "uuid", player.getUniqueId().toString());
						} catch (SQLActionImpossibleException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return;

					} else
						player.sendMessage(
								Money.PREFIX + "�cVeuillez attendre au minimum une heure avant de retirer vos b�n�fices.");
				}

				if (event.getSlot() == 15) {
					player.closeInventory();
				}
			}
		}
	}
}
