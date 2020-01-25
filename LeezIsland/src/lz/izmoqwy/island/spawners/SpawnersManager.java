package lz.izmoqwy.island.spawners;

import com.google.common.collect.Maps;
import lz.izmoqwy.core.api.ItemBuilder;
import lz.izmoqwy.core.api.database.exceptions.SQLActionImpossibleException;
import lz.izmoqwy.core.utils.ItemUtil;
import lz.izmoqwy.core.world.LiteLocation;
import lz.izmoqwy.island.Storage;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")
public class SpawnersManager {

	private static String PREFIX = "§6Spawners §8» ";
	public static SpawnersManager manager = new SpawnersManager();

	public Map<LiteLocation, SpawnerData> spawnerDataMap = Maps.newHashMap();

	public SpawnerData getSpawner(Location fullLocation) {
		if (fullLocation.getBlock().getType() != Material.MOB_SPAWNER)
			return null;

		LiteLocation location = LiteLocation.from(fullLocation);
		if (spawnerDataMap.containsKey(location))
			return spawnerDataMap.get(location);
		else {
			try {
				String data = Storage.SPAWNERS.getString("data", "location", location.toString());
				SpawnerData spawnerData = SpawnerData.from(location, data);
				spawnerDataMap.put(location, spawnerData);
				return spawnerData;
			}
			catch (SQLActionImpossibleException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public void save(SpawnerData spawnerData) {
		String location = spawnerData.getLocation().toString();
		try {
			if (Storage.SPAWNERS.hasResult("data", "location", location)) {
				Storage.SPAWNERS.setString("data", spawnerData.toString(), "location", location);
			}
			else {
				PreparedStatement preparedStatement = Storage.DB.prepare("INSERT INTO " + Storage.SPAWNERS + "(location,data) VALUES (?,?)");
				preparedStatement.setString(1, location);
				preparedStatement.setString(2, spawnerData.toString());
				preparedStatement.execute();
			}
		}
		catch (SQLActionImpossibleException | SQLException e) {
			e.printStackTrace();
		}
	}

	public void delete(Location fullLocation) {
		spawnerDataMap.remove(LiteLocation.from(fullLocation));
	}

	private EntityType getSpawnerEntity(ItemStack itemStack) {
		if (itemStack == null || itemStack.getType() != Material.MOB_SPAWNER || !itemStack.hasItemMeta() || itemStack.getItemMeta().getLore() == null)
			return null;

		ItemMeta meta = itemStack.getItemMeta();
		if (meta.getLore().size() == 1) {
			String firstLine = meta.getLore().get(0);
			if (firstLine.startsWith("§8Spawner ID:")) {
				try {
					return EntityType.fromId(Integer.parseInt(firstLine.substring(16)));
				}
				catch (NumberFormatException ex) {
					return null;
				}
			}
		}

		return null;
	}

	private ItemStack getItemStack(EntityType entityType) {
		if (entityType == null)
			return null;

		return new ItemBuilder(Material.MOB_SPAWNER)
				.name("§e" + entityType.getName().toUpperCase())
				.appendLore("§8Spawner ID: §7" + entityType.getTypeId())
				.toItemStack();
	}

	public void onSpawnerPlace(BlockPlaceEvent event) {
		if (event.getItemInHand() == null || event.getItemInHand().getType() != Material.MOB_SPAWNER)
			return;

		Player player = event.getPlayer();
		EntityType entityType = getSpawnerEntity(event.getItemInHand());
		if (entityType != null) {
			if (!player.isSneaking() && event.getBlockAgainst() != null && event.getBlockAgainst().getType() == Material.MOB_SPAWNER) {
				event.setCancelled(true);

				CreatureSpawner creatureSpawner = (CreatureSpawner) event.getBlockAgainst().getState();
				if (creatureSpawner == null || creatureSpawner.getSpawnedType() != entityType) {
					player.sendMessage(PREFIX + "§cLe spawner que vous essayez d'améliorer n'est pas du même type.");
					return;
				}
				SpawnerData spawnerData = getSpawner(event.getBlockAgainst().getLocation());
				if (spawnerData == null) {
					player.sendMessage(PREFIX + "§cUn problème d'ordre interne est survenu.");
					return;
				}

				final int max = 50;
				if (spawnerData.getAmount() + 1 > max) {
					player.sendMessage(PREFIX + "§cCe spawner est déjà amélioré au maximum (§e§lx" + spawnerData.getAmount() + "§c), visitez le /is shop pour pouvoir amliorer votre capaciter d'amelioration des spawners.");
					return;
				}

				spawnerData.setAmount(spawnerData.getAmount() + 1);
				player.sendMessage(PREFIX + "§aSpawner à §2" + entityType.getName().toUpperCase() + "§a amélioré en §e§lx" + spawnerData.getAmount() + " §a!");

				if (player.getGameMode() != GameMode.CREATIVE)
					ItemUtil.take(player, new ItemBuilder(event.getItemInHand()).amount(1).toItemStack());
				return;
			}

			CreatureSpawner creatureSpawner = (CreatureSpawner) event.getBlockPlaced().getState();
			creatureSpawner.setSpawnedType(entityType);
			creatureSpawner.update();

			LiteLocation location = LiteLocation.from(event.getBlockPlaced().getLocation());
			SpawnerData spawnerData = new SpawnerData(location, 1);
			save(spawnerData);
			spawnerData.refresh();
			spawnerDataMap.put(location, spawnerData);
			player.sendMessage(PREFIX + "§aVous avez placé un spawner à §2" + entityType.getName().toUpperCase() + "§a.");
		}
		else {
			ItemUtil.give(player, getItemStack(EntityType.PIG));
			event.setCancelled(true);
			player.sendMessage(PREFIX + "§cCe spawner est invalide ou corrompu.");
		}
	}

	public void onSpawnerBreak(BlockBreakEvent event) {
		CreatureSpawner creatureSpawner = (CreatureSpawner) event.getBlock().getState();
		if (creatureSpawner == null)
			return;

		ItemStack spawnerItem = getItemStack(creatureSpawner.getSpawnedType());
		if (spawnerItem != null) {
			Player player = event.getPlayer();

			SpawnerData spawnerData = getSpawner(event.getBlock().getLocation());
			if (spawnerData != null && spawnerData.getAmount() >= 1) {
				if (player.isSneaking() && spawnerData.getAmount() > 1) {
					int initialAmount = spawnerData.getAmount();
					spawnerItem.setAmount(initialAmount);

					final List<ItemStack> remainingList = ItemUtil.give(player, spawnerItem);
					final int remaining = remainingList.isEmpty() ? 0 : remainingList.get(0).getAmount();
					if (remaining == initialAmount) {
						event.setCancelled(true);
						player.sendMessage(PREFIX + "§cVotre inventaire est plein, vous ne pouvez pas récupérer ce spawner !");
						return;
					}
					if (remaining == 0) {
						delete(event.getBlock().getLocation());
						player.sendMessage(PREFIX + "§aVous avez récupéré §e§l" + initialAmount + "§a spawners à §2" + creatureSpawner.getSpawnedType().getName().toUpperCase() + "§a.");
					}
					else {
						spawnerData.setAmount(remaining);
						event.setCancelled(true);
						player.sendMessage(PREFIX + "§aVous avez récupéré §e§l" + (initialAmount - remaining) + "§a spawners à §2" + creatureSpawner.getSpawnedType().getName().toUpperCase() + "§a. Restant: §e§lx" + spawnerData.getAmount());
					}
				}
				else {
					if (ItemUtil.give(player, spawnerItem).isEmpty()) {
						event.setExpToDrop(0);

						if (spawnerData.getAmount() > 1) {
							spawnerData.setAmount(spawnerData.getAmount() - 1);
							event.setCancelled(true);
							player.sendMessage(PREFIX + "§aVous avez récupéré un spawner à §2" + creatureSpawner.getSpawnedType().getName().toUpperCase() + "§a. Restant: §e§lx" + spawnerData.getAmount());
						}
						else {
							delete(event.getBlock().getLocation());
							player.sendMessage(PREFIX + "§aVous avez récupéré un spawner à §2" + creatureSpawner.getSpawnedType().getName().toUpperCase() + "§a.");
						}
					}
					else {
						player.sendMessage(PREFIX + "§cVotre inventaire est plein, vous ne pouvez pas récupérer ce spawner !");
						event.setCancelled(true);
					}
				}
			}
		}
	}

}
