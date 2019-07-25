package lz.izmoqwy.market.blackmarket.illegal;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.istack.internal.NotNull;
import lombok.Getter;
import lz.izmoqwy.core.PlayerSaveManager;
import lz.izmoqwy.core.api.PlayerBackup;
import lz.izmoqwy.core.helpers.PluginHelper;
import lz.izmoqwy.core.utils.ItemUtil;
import lz.izmoqwy.market.Locale;
import lz.izmoqwy.market.MarketPlugin;
import lz.izmoqwy.market.blackmarket.EntityManager;
import lz.izmoqwy.market.rpg.RPGManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@SuppressWarnings({"unchecked", "UnusedReturnValue"})
public class ForbiddenArena implements Listener {

	protected static final String PREFIX = "§5§lArène interdite §8» ", IRON_GOLEM = "§7Golem en Fer", GOLD_GOLEM = "§eGolem en Or";
	protected static final int GOLEMS = 9;

	@Getter
	private static Location teleportPoint;
	@Getter
	private static List<Location> spawnPoints;

	private static Random random = new Random();
	private static boolean firstLoad = true;

	@Getter
	private static Map<Player, ForbiddenPlayer> concurrents = Maps.newHashMap();
	private static ArrayList<IronGolem> golemList = Lists.newArrayList();

	private static ItemStack[] CONTENTS;
	private static ItemStack[] ARMOR_CONTENTS;

	public static void load(Location spawnArena, @NotNull List<Location> spawnPoints) {
		if (spawnArena != null) {
			if (firstLoad) {
				firstLoad = false;
				PluginHelper.loadListener(MarketPlugin.getInstance(), new ForbiddenArena());
				EntityManager.registerEntity("forbidden_golem", 99, ForbiddenFighter.class);
			}
			ForbiddenArena.teleportPoint = spawnArena;
		}
		if (ForbiddenArena.spawnPoints == null || !spawnPoints.isEmpty() || ForbiddenArena.spawnPoints.isEmpty())
			ForbiddenArena.spawnPoints = spawnPoints;

		Inventory template = Bukkit.createInventory(null, InventoryType.PLAYER);

		template.setItem(0, getSword(1));
		template.setItem(1, getBow(1));

		template.setItem(8, ItemUtil.createItem(Material.TOTEM, 3));
		template.setItem(9, ItemUtil.createItem(Material.ARROW));

		CONTENTS = template.getContents();

		ItemStack helmet = ItemUtil.createItem(Material.GOLD_HELMET, "§dCasque §51");
		ItemStack leggings = ItemUtil.createItem(Material.CHAINMAIL_LEGGINGS, "§dJambières §51");
		ItemStack boots = ItemUtil.createItem(Material.GOLD_BOOTS, "§dBottes §51");
		ARMOR_CONTENTS = new ItemStack[]{boots, leggings, getChestplate(1), helmet};

		// Will needs to be removed
		for (ItemStack armorItem : ARMOR_CONTENTS) {
			ItemMeta meta = helmet.getItemMeta();
			meta.setUnbreakable(true);
			armorItem.setItemMeta(meta);
		}
	}

	private static ItemStack getChestplate(int level) {
		Material material;
		Map<Enchantment, Integer> enchants = Maps.newHashMap();
		switch (level) {
			case 1:
				material = Material.CHAINMAIL_CHESTPLATE;
				break;
			case 2:
				material = Material.IRON_CHESTPLATE;
				break;
			case 3:
				material = Material.IRON_CHESTPLATE;
				enchants = ImmutableMap.of(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
				break;
			case 4:
				material = Material.DIAMOND_CHESTPLATE;
				break;
			case 5:
				material = Material.DIAMOND_CHESTPLATE;
				enchants = ImmutableMap.of(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
				break;

			default:
				return null;
		}
		return makeUnbreakable(ItemUtil.createItem(material, "§dPlastron §5" + level, enchants));
	}

	private static ItemStack getSword(int level) {
		Material material;
		Map<Enchantment, Integer> enchants;
		switch (level) {
			case 1:
				material = Material.IRON_SWORD;
				enchants = ImmutableMap.of(Enchantment.DAMAGE_ALL, 4);
				break;
			case 2:
				material = Material.DIAMOND_SWORD;
				enchants = ImmutableMap.of(Enchantment.DAMAGE_ALL, 3, Enchantment.KNOCKBACK, 1);
				break;
			case 3:
				material = Material.DIAMOND_SWORD;
				enchants = ImmutableMap.of(Enchantment.DAMAGE_ALL, 4, Enchantment.FIRE_ASPECT, 1, Enchantment.KNOCKBACK, 2);
				break;

			default:
				return null;
		}
		return makeUnbreakable(ItemUtil.createItem(material, "§dÉpée §5" + level, enchants));
	}

	private static ItemStack getBow(int level) {
		Material material;
		Map<Enchantment, Integer> enchants;
		switch (level) {
			case 1:
				material = Material.BOW;
				enchants = ImmutableMap.of(Enchantment.ARROW_DAMAGE, 2, Enchantment.ARROW_INFINITE, 1);
				break;
			case 2:
				material = Material.BOW;
				enchants = ImmutableMap.of(Enchantment.ARROW_DAMAGE, 4, Enchantment.ARROW_KNOCKBACK, 1, Enchantment.ARROW_INFINITE, 1);
				break;
			case 3:
				material = Material.BOW;
				enchants = ImmutableMap.of(Enchantment.ARROW_DAMAGE, 5, Enchantment.ARROW_FIRE, 1, Enchantment.ARROW_KNOCKBACK, 2, Enchantment.ARROW_INFINITE, 1);
				break;

			default:
				return null;
		}
		return makeUnbreakable(ItemUtil.createItem(material, "§dArc §5" + level, enchants));
	}

	private static ItemStack makeUnbreakable(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		meta.setUnbreakable(true);
		item.setItemMeta(meta);
		return item;
	}

	private static void killAllGolems() {
		if (golemList.isEmpty())
			return;
		killGolems(golemList.size());
	}

	@SuppressWarnings("UnusedReturnValue")
	private static int killGolems(int bound) {
		int j = bound > golemList.size() ? golemList.size() : bound, k = 0;
		if (bound == 0)
			return 0;

		final List<IronGolem> copy = (List<IronGolem>) golemList.clone();
		for (int i = 0; i < j; i++) {
			IronGolem golem = copy.get(i);
			golem.getLocation().getWorld().getChunkAt(golem.getLocation());
			golem.remove();
			golemList.remove(golem);
			k++;
		}

		broadcast("§5" + k + " §dgolems ont été retirés.");
		return k;
	}

	private static int getNeededGolems() {
		return GOLEMS * (concurrents.size() > 1 ? (concurrents.size() - 1) * GOLEMS / 2 : 1);
	}

	private static int spawnGolems(boolean random) {
		return spawnGolems(random ? ForbiddenArena.random.nextInt(getNeededGolems() - golemList.size() - 1) + 1 : getNeededGolems() - golemList.size());
	}

	private static int spawnGolems(int bound) {
		if (concurrents.size() < 1) {
			return 0;
		}

		int k = 0;
		for (int i = 0; i < bound; i++) {
			Location randomPoint = spawnPoints.isEmpty() ? teleportPoint : spawnPoints.get(random.nextInt(spawnPoints.size()));

			ForbiddenFighter fighter = EntityManager.spawnGolem(randomPoint);
			boolean isGolden = (random.nextInt(5) + 1) > 4;

			if (isGolden) {
				fighter.setSpeed(.3F);
			}

			IronGolem golem = (IronGolem) fighter.getBukkitEntity();
			AttributeInstance healthAttr = golem.getAttribute(Attribute.GENERIC_MAX_HEALTH);
			int maxHealth;
			if (!isGolden) {
				golem.setCustomName(IRON_GOLEM);
				maxHealth = 50 + random.nextInt(50);
			}
			else {
				golem.setCustomName(GOLD_GOLEM);
				maxHealth = 75 + random.nextInt(75 + random.nextInt(125));
			}
			golem.setCustomNameVisible(true);
			healthAttr.setBaseValue(maxHealth);

			golemList.add(golem);
			k ++;
		}

		broadcast("§5" + k + " §dgolems ont été ajoutés.");
		return k;
	}


	public static void forceEnd() {
		killAllGolems();
		if (!concurrents.isEmpty()) {
			for (ForbiddenPlayer player : concurrents.values()) {
				// Todo: Give money back to player and backup inventory
				PlayerSaveManager.restore(player.getBackup(), player.getBase(), true);
				player.sendMessage("§4L'arène doit être vidée de toute urgence, vous avez été remboursé, désolé pour le dérangement.");
			}
			concurrents.clear();
		}
	}

	public static boolean isInArena(Player player) {
		return concurrents.containsKey(player);
	}

	protected static void broadcast(String message) {
		for (ForbiddenPlayer player : concurrents.values()) {
			player.sendMessage("§d" + message);
		}
	}

	protected static void broadcast(String message, ForbiddenPlayer excluding) {
		for (ForbiddenPlayer player : concurrents.values()) {
			if (player == excluding)
				continue;
			player.sendMessage("§d" + message);
		}
	}

	public static boolean join(Player bukkitPlayer, boolean sendMessages) {
		if (teleportPoint == null) {
			if (sendMessages)
				bukkitPlayer.sendMessage(Locale.PREFIX + "§cL'arène interdite n'a pas de point de téléportation défini.");
			return false;
		}
		if (isInArena(bukkitPlayer)) {
			if (sendMessages)
				bukkitPlayer.sendMessage(PREFIX + "§cVous êtes déjà dans l'arène !");
			return false;
		}

		ForbiddenPlayer player = new ForbiddenPlayer(bukkitPlayer);
		try {
			player.getBackup().save(PlayerSaveManager.getFile(bukkitPlayer), null, false);
		}
		catch (IOException e) {
			e.printStackTrace();
			bukkitPlayer.sendMessage(Locale.PREFIX + "§4Une erreur est survenue !");
			return false;
		}
		PlayerBackup.clearBukkitPlayer(bukkitPlayer);
		bukkitPlayer.getInventory().setContents(CONTENTS);
		bukkitPlayer.getInventory().setArmorContents(ARMOR_CONTENTS);
		concurrents.put(bukkitPlayer, player);

		bukkitPlayer.teleport(teleportPoint);
		player.sendMessage("§aVous avez rejoint l'arène.");
		broadcast("§5" + bukkitPlayer.getName() + "§d a rejoint l'arène.", player);
		spawnGolems(false);
		return true;
	}

	private static void eliminate(Player bukkitPlayer, boolean sendMessages) {
		if (isInArena(bukkitPlayer)) {
			ForbiddenPlayer player = concurrents.get(bukkitPlayer);

			if (sendMessages) {
				player.sendMessage("§6Vous avez été éliminé !");
			}
			if (player.getGold_earned() + player.getIron_earned() > 0) {
				if (sendMessages && player.getGold_earned() > 0 && player.getIron_earned() > 0) {
					player.sendMessage("§8➥ §bVous avez récolté §6" + player.getGold_earned() + " §eOr §bainsi que §8" + player.getIron_earned() + " §7Fer§b.", false);
				}
				else if (sendMessages && player.getGold_earned() > 0) {
					player.sendMessage("§8➥ §bVous avez récolté §6" + player.getGold_earned() + " §eOr§b.", false);
				}
				else if (sendMessages && player.getIron_earned() > 0) {
					player.sendMessage("§8➥ §bVous avez récolté §8" + player.getIron_earned() + " §7Fer§b.", false);
				}

				int reward = player.getGold_earned() * 3 + player.getIron_earned();
				if (RPGManager.givePoints(bukkitPlayer, reward) && sendMessages) {
					player.sendMessage("§8➥ §6Vous avez gagné §e" + reward + " ✦ Points§6.", false);
				}
			}
			else if (sendMessages) {
				player.sendMessage("§8➥ §cVous n'avez rien gagné.", false);
			}
			PlayerSaveManager.restore(player.getBackup(), bukkitPlayer, true);

			concurrents.remove(bukkitPlayer);
			if (concurrents.isEmpty()) {
				killAllGolems();
			}
			else {
				broadcast("§5" + bukkitPlayer.getName() + " §da été éliminé !");
				if (golemList.size() > getNeededGolems() + GOLEMS / 3) {
					killGolems(golemList.size() - getNeededGolems());
				}
			}
		}
	}

	private static void earnGold(ForbiddenPlayer player, int amount) {
		player.earnGold(amount);
		player.sendMessage("§8+ §e" + amount + "§e Or", false);
	}

	private static void earnIron(ForbiddenPlayer player, int amount) {
		player.earnIron(amount);
		player.sendMessage("§8+ §7" + amount + "§7 Fer", false);

		int newLevel = -1;
		if (player.getIron_earned() >= 25 && player.getItems_level() == 1) {
			newLevel = 2;
		}

		if (newLevel > 1) {
			final int oldLevel = newLevel - 1;

			PlayerInventory inv = player.getBase().getInventory();
			inv.setItem(getSlot(inv, getSword(oldLevel), 0), getSword(newLevel));
			inv.setItem(getSlot(inv, getBow(oldLevel), 1), getBow(newLevel));
			player.getBase().updateInventory();

			player.setItems_level(newLevel);
			player.sendMessage("§aVos objets d'attaque ont été améliorés au niveau §2" + newLevel + " §a!");
		}
	}

	private static int getSlot(Inventory inv, ItemStack item, int def) {
		int old = inv.first(item);
		if (old == -1) {
			int firstEmpty = inv.firstEmpty();
			if (firstEmpty != -1)
				return firstEmpty;
		}

		return old != -1 ? old : def;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onDisconnect(PlayerQuitEvent event) {
		if (isInArena(event.getPlayer()))
			eliminate(event.getPlayer(), false);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDamageEvent(EntityDamageEvent event) {
		if (event.getEntityType() == EntityType.PLAYER) {
			if (isInArena((Player) event.getEntity())) {
				Player player = (Player) event.getEntity();
				if (player.getHealth() <= event.getFinalDamage()) {
					if (player.getInventory().contains(Material.TOTEM) || player.getInventory().getItemInOffHand().getType() == Material.TOTEM) {
						int left = 1;

						if (player.getInventory().getItemInOffHand().getType() == Material.TOTEM) {
							ItemStack item = player.getInventory().getItemInOffHand();
							if (item.getAmount() <= 1) {
								player.getInventory().setItemInOffHand(null);
							}
							else {
								item.setAmount(item.getAmount() - 1);
								left += item.getAmount();
							}
							player.getInventory().setItemInOffHand(item);
						}
						else {
							// Remove the totem with a workaround in case the meta is different
							ItemStack[] items = player.getInventory().getStorageContents();
							boolean remove = true;
							for (int i = 0; i < items.length; ++i) {
								if (items[i] != null && items[i].getType() == Material.TOTEM) {
									if (remove) {
										remove = false;
										if (items[i].getAmount() <= 1)
											player.getInventory().clear(i);
										else {
											ItemStack item = items[i];
											item.setAmount(item.getAmount() - 1);
											player.getInventory().setItem(i, item);
											left += item.getAmount();
										}
									}
									else
										left += items[i].getAmount();
								}
							}
						}
						event.setCancelled(true);
						player.sendMessage(PREFIX + "§cVous avez perdu une vie (" + (left > 1 ? "§4" + left + " §crestantes" : "ceci est votre dernière vie") + ").");
						player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
						player.setFoodLevel(20);
						player.setSaturation(20.F);
						player.setExhaustion(0.F);
					}
					else {
						event.setCancelled(true);
						player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
						eliminate(player, true);
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onGolemKill(EntityDeathEvent event) {
		if (event.getEntityType() == EntityType.IRON_GOLEM && !golemList.isEmpty()) {
			IronGolem golem = (IronGolem) event.getEntity();
			if (golemList.contains(golem)) {
				event.getDrops().clear();
				event.setDroppedExp(0);
				if (golem.getKiller() != null && isInArena(golem.getKiller())) {
					ForbiddenPlayer player = concurrents.get(golem.getKiller());
					if (golem.getCustomName().equals(GOLD_GOLEM)) {
						earnGold(player, random.nextInt(3) + 1);
					}
					else if (golem.getCustomName().equals(IRON_GOLEM)) {
						earnIron(player, random.nextInt(5) + 1);
					}
				}
				else {
					if (golem.getCustomName().equals(GOLD_GOLEM)) {
						event.getDrops().add(ItemUtil.createItem(Material.GOLD_INGOT, random.nextInt(3) + 1));
					}
					else if (golem.getCustomName().equals(IRON_GOLEM)) {
						event.getDrops().add(ItemUtil.createItem(Material.IRON_INGOT, random.nextInt(5) + 1));
					}
				}
				golemList.remove(golem);
				if (golemList.size() < getNeededGolems() - GOLEMS / 3) {
					spawnGolems(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPickup(EntityPickupItemEvent event) {
		if (event.getEntityType() == EntityType.PLAYER) {
			if (isInArena((Player) event.getEntity())) {
				ForbiddenPlayer player = concurrents.get(event.getEntity());
				ItemStack its = event.getItem().getItemStack();
				switch (its.getType()) {
					case IRON_INGOT:
						earnIron(player, its.getAmount());
						break;
					case GOLD_INGOT:
						earnGold(player, its.getAmount());
						break;
				}
				event.setCancelled(true);
				event.getItem().remove();
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onDrop(PlayerDropItemEvent event) {
		if (isInArena(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

}
