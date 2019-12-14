package lz.izmoqwy.leezisland.listeners;

import lz.izmoqwy.leezisland.Locale;
import lz.izmoqwy.leezisland.commands.AdminCommand;
import lz.izmoqwy.leezisland.generator.OreGenerator;
import lz.izmoqwy.leezisland.grid.CoopsManager;
import lz.izmoqwy.leezisland.grid.GridManager;
import lz.izmoqwy.leezisland.island.CoopPermission;
import lz.izmoqwy.leezisland.island.GeneralPermission;
import lz.izmoqwy.leezisland.island.Island;
import lz.izmoqwy.leezisland.island.VisitorPermission;
import lz.izmoqwy.leezisland.spawners.SpawnersManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class IslandGuard implements Listener {

	private final List<String> SETHOME_COMMANDS = Arrays.asList("sethome", "esethome", "createhome", "ecreatehome");

	private OreGenerator oreGenerator;

	public IslandGuard() {
		this.oreGenerator = new OreGenerator();
	}

	private boolean canDo(Player player, Location location, Cancellable event, VisitorPermission ifVisitor, CoopPermission ifCoop) {
		if (AdminCommand.BYPASSING.contains(player.getUniqueId())) {
			return true;
		}

		boolean bool;
		Island is = GridManager.getIslandAt(location);
		if (is != null) {
			// Check if player is in the island
			if (is.hasFullAccess(player))
				event.setCancelled(bool = false);
			else {
				if (CoopsManager.isCooped(player.getUniqueId(), is.ID)) {
					if (ifCoop != null) {
						event.setCancelled(bool = !is.hasCoopPermission(ifCoop));
					}
					else
						event.setCancelled(bool = false);
				}
				else {
					// Player can be only a visitor
					if (ifVisitor != null && is.hasVisitorPermission(ifVisitor)) {
						event.setCancelled(bool = false);
					}
					else
						event.setCancelled(bool = true);
				}
			}
		}
		else {
			event.setCancelled(bool = true);
		}

		return !bool;
	}

	private boolean canDo(Player player, Block block, Cancellable event, VisitorPermission ifVisitor, CoopPermission ifCoop) {
		return canDo(player, block.getLocation(), event, ifVisitor, ifCoop);
	}

	private boolean isThereIslandAt(Location location) {
		Island is = GridManager.getIslandAt(location);
		return is != null;
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private boolean testGeneralPermission(Location location, GeneralPermission setting) {
		Island is = GridManager.getIslandAt(location);
		if (is == null)
			return false;

		return is.hasGeneralPermission(setting);
	}

	/*
		World Check Methods
	 */
	private boolean inWorld(Location location) {
		return inWorld(location.getWorld());
	}

	private boolean inWorld(Block block) {
		return inWorld(block.getWorld());
	}

	private boolean inWorld(World world) {
		return GridManager.isGridWorld(world);
	}

	/*
		Glitches prevention
	 */
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPistonExtend(BlockPistonExtendEvent event) {
		if (event.getBlocks().isEmpty() || inWorld(event.getBlock()))
			return;
		event.setCancelled(!isThereIslandAt(event.getBlocks().get(event.getBlocks().size() - 1).getRelative(event.getDirection()).getLocation()));
	}

	/*
		Teleporting
	 */
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onTeleport(PlayerTeleportEvent event) {
		if (event.getTo().getWorld() != GridManager.getWorld())
			return;

		Player player = event.getPlayer();
		if (AdminCommand.BYPASSING.contains(player.getUniqueId()))
			return;

		Island is = GridManager.getIslandAt(event.getTo());
		if (is != null) {
			Island from = GridManager.getIslandAt(event.getFrom());
			if (from == is)
				return;

			if (is.isLocked()) {
				event.setCancelled(true);
				Locale.GUARD_LOCKED.send(player);
			}
			else if (is.getBanneds().contains(player.getUniqueId())) {
				event.setCancelled(true);
				Locale.GUARD_BANNED.send(player);
			}
			else {
				if (is.hasFullAccess(player))
					return;

				if (is.getName() != null)
					Locale.GUARD_ENTER_NAMED.send(player, is.getName());
				else
					Locale.GUARD_ENTER_NONAME.send(player, is.getOwner().getName());

				if (!is.hasVisitorPermission(VisitorPermission.FLY) && player.getAllowFlight()) {
					player.setAllowFlight(false);
					Locale.GUARD_ISLAND_NOFLY.send(player);
				}
			}
		}
	}

	/*
		General Settings
	 */
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onSpawnerSpawn(SpawnerSpawnEvent event) {
		if (inWorld(event.getLocation()))
			return;

		event.setCancelled(!testGeneralPermission(event.getLocation(), GeneralPermission.SPAWNERS));
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onMobSpawn(CreatureSpawnEvent event) {
		if (inWorld(event.getLocation()) || !(event.getEntity() instanceof Monster))
			return;

		event.setCancelled(!testGeneralPermission(event.getLocation(), GeneralPermission.MOBSPAWNING));
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onFlow(BlockFromToEvent event) {
		if (inWorld(event.getToBlock()))
			return;

		Block toBlock = event.getToBlock();
		if (toBlock.getType() != Material.AIR)
			return;

		Island island = GridManager.getIslandAtSafe(toBlock.getLocation());
		if (island != null) {
			if (!island.hasGeneralPermission(GeneralPermission.FLUIDFLOWING)) {
				event.setCancelled(true);
				return;
			}

			if (!island.hasGeneralPermission(GeneralPermission.GENENABLED))
				return;

			int flowId = event.getBlock().getTypeId();
			if (flowId >= 8 && flowId <= 11 && oreGenerator.shouldGenerates(toBlock, flowId)) {
				toBlock.setType(oreGenerator.randomize(island));
			}
		}
	}

	/*
		Guardian
	 */
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onToggleFly(PlayerToggleFlightEvent event) {
		Location location = event.getPlayer().getLocation();
		if (inWorld(location))
			return;

		canDo(event.getPlayer(), location, event, VisitorPermission.FLY, null);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onCommand(PlayerCommandPreprocessEvent event) {
		Location location = event.getPlayer().getLocation();
		if (inWorld(location))
			return;

		final String command = event.getMessage().split(" ")[0].substring(1).toLowerCase();
		if (SETHOME_COMMANDS.contains(command)) {
			if (!canDo(event.getPlayer(), location, event, VisitorPermission.SETHOME, null)) {
				event.getPlayer().sendMessage(Locale.PREFIX + "§cVous ne pouvez pas définir de home sur cette île !");
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPlace(BlockPlaceEvent event) {
		if (inWorld(event.getBlock()))
			return;

		if (canDo(event.getPlayer(), event.getBlock(), event, null, CoopPermission.PLACE)) {
			Material type = event.getBlock().getType();
			if (type == Material.MOB_SPAWNER) {
				SpawnersManager.manager.onSpawnerPlace(event);
			}
			else if (type == Material.CHEST) {
				canDo(event.getPlayer(), event.getBlock(), event, null, CoopPermission.CHEST);
			}
			else if (isShulker(type)) {
				canDo(event.getPlayer(), event.getBlock(), event, null, CoopPermission.SHULKER_BOX);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onBreak(BlockBreakEvent event) {
		if (inWorld(event.getBlock()))
			return;

		if (canDo(event.getPlayer(), event.getBlock(), event, null, CoopPermission.BREAK)) {
			Material type = event.getBlock().getType();
			if (type == Material.MOB_SPAWNER) {
				SpawnersManager.manager.onSpawnerBreak(event);
			}
			else if (type == Material.CHEST) {
				canDo(event.getPlayer(), event.getBlock(), event, null, CoopPermission.CHEST);
			}
			else if (isShulker(type)) {
				canDo(event.getPlayer(), event.getBlock(), event, null, CoopPermission.SHULKER_BOX);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onEmptyBucket(PlayerBucketEmptyEvent event) {
		if (inWorld(event.getBlockClicked()))
			return;
		canDo(event.getPlayer(), event.getBlockClicked(), event, null, CoopPermission.BUCKETS);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onFillBucket(PlayerBucketFillEvent event) {
		if (inWorld(event.getBlockClicked()))
			return;
		canDo(event.getPlayer(), event.getBlockClicked(), event, null, CoopPermission.BUCKETS);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPickup(EntityPickupItemEvent event) {
		if (inWorld(event.getEntity().getWorld()))
			return;
		if (event.getEntityType() == EntityType.PLAYER) {
			canDo((Player) event.getEntity(), event.getEntity().getLocation(), event, VisitorPermission.PICKUP, null);
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onDrop(PlayerDropItemEvent event) {
		if (inWorld(event.getPlayer().getLocation()))
			return;
		canDo(event.getPlayer(), event.getPlayer().getLocation(), event, VisitorPermission.DROP, null);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (inWorld(player.getWorld()))
			return;

		if (event.getAction() == Action.PHYSICAL) {
			if (event.hasBlock()) {
				Block actionned = event.getClickedBlock();
				switch (actionned.getType()) {
					case SOIL:
						event.setCancelled(true);
						return;
					case GOLD_PLATE:
					case IRON_PLATE:
					case STONE_PLATE:
					case WOOD_PLATE:
						canDo(player, player.getLocation(), event, VisitorPermission.PLATES, CoopPermission.ACTIONNERS);
						return;
				}
			}
		}

		if (event.hasItem()) {
			ItemStack item = event.getItem();
			Location location = player.getLocation();
			switch (item.getType()) {
				case FIREBALL:
				case FLINT_AND_STEEL:
					if (!event.hasBlock())
						break;

					if (!canDo(player, location, event, null, CoopPermission.FIRE)) {
						event.setUseItemInHand(Event.Result.DENY);
						return;
					}
					break;
				case BUCKET:
				case WATER_BUCKET:
				case LAVA_BUCKET:
					if (!event.hasBlock())
						break;

					if (!canDo(player, location, event, null, CoopPermission.BUCKETS)) {
						event.setUseItemInHand(Event.Result.DENY);
						return;
					}
					break;
			}
		}
		if (event.hasBlock()) {
			Block clicked = event.getClickedBlock();
			switch (clicked.getType()) {
				case CHEST:
				case TRAPPED_CHEST:
				case STORAGE_MINECART:
					canDo(player, clicked, event, null, CoopPermission.CHEST);
					break;
				case LEVER:
					canDo(player, clicked, event, VisitorPermission.LEVERS, CoopPermission.ACTIONNERS);
					break;
				case STONE_BUTTON:
				case WOOD_BUTTON:
					canDo(player, clicked, event, VisitorPermission.BUTTONS, CoopPermission.ACTIONNERS);
					break;
				case FENCE_GATE:
				case ACACIA_FENCE_GATE:
				case BIRCH_FENCE_GATE:
				case DARK_OAK_FENCE_GATE:
				case JUNGLE_FENCE_GATE:
				case SPRUCE_FENCE_GATE:
					canDo(player, clicked, event, VisitorPermission.GATES, null);
					break;
				case REDSTONE_COMPARATOR_OFF:
				case REDSTONE_COMPARATOR_ON:
				case DIODE_BLOCK_OFF:
				case DIODE_BLOCK_ON:
					canDo(player, clicked, event, VisitorPermission.REDSTONE, CoopPermission.REDSTONE);
					break;
				case ACACIA_DOOR:
				case BIRCH_DOOR:
				case DARK_OAK_DOOR:
				case JUNGLE_DOOR:
				case SPRUCE_DOOR:
				case WOODEN_DOOR:
					canDo(player, clicked, event, VisitorPermission.DOORS, null);
					break;
				case FURNACE:
				case BURNING_FURNACE:
				case DISPENSER:
				case DROPPER:
				case HOPPER:
				case HOPPER_MINECART:
				case BREWING_STAND:
					canDo(player, clicked, event, null, CoopPermission.CONTAINERS);
					break;
				default:
					Material material = clicked.getType();
					if (isShulker(material)) {
						canDo(player, clicked, event, null, CoopPermission.SHULKER_BOX);
					}
					break;
			}
		}

	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onHit(EntityDamageByEntityEvent event) {
		if (event.getDamager().getType() != EntityType.PLAYER || inWorld(event.getDamager().getWorld()))
			return;

		Player damager = (Player) event.getDamager();
		Entity victim = event.getEntity();
		Location location = victim.getLocation();
		if (victim.getType() == EntityType.PLAYER) {
			// No setting for PvP for the moment
			event.setCancelled(true);
			damager.sendMessage(Locale.PREFIX + "§cLe PvP est désactivé dans ce monde !");
		}
		else if (victim instanceof Monster || victim.getType() == EntityType.SLIME) {
			if (!canDo(damager, location, event, VisitorPermission.HITMOBS, null)) {
				damager.sendMessage(Locale.PREFIX + "§cVous ne pouvez pas taper les animaux sur cette île !");
			}
		}
		else if (victim instanceof Animals || victim.getType() == EntityType.SQUID) {
			if (!canDo(damager, location, event, VisitorPermission.HITANIMALS, null)) {
				damager.sendMessage(Locale.PREFIX + "§cVous ne pouvez pas taper les animaux sur cette île !");
			}
		}
		else if (victim instanceof Golem) {
			if (!canDo(damager, location, event, VisitorPermission.HITGOLEMS, null)) {
				damager.sendMessage(Locale.PREFIX + "§cVous ne pouvez pas taper les Golems sur cette île !");
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onRod(PlayerFishEvent event) {
		Entity victim = event.getCaught();
		if (victim == null || inWorld(victim.getWorld()))
			return;

		Player damager = event.getPlayer();
		if (victim.getType() == EntityType.PLAYER) {
			if (victim.equals(damager))
				return;

			event.setCancelled(true);
			event.getHook().remove();

			damager.sendMessage(Locale.PREFIX + "§cLe PvP est désactivé dans ce monde !");
		}
		else if (victim.getType() == EntityType.ARMOR_STAND || victim.getType() == EntityType.ENDER_CRYSTAL) {
			canDo(damager, victim.getLocation(), event, null, CoopPermission.BREAK);
		}
		else if (victim instanceof Monster || victim.getType() == EntityType.SLIME) {
			if (!canDo(damager, victim.getLocation(), event, VisitorPermission.HITMOBS, null)) {
				damager.sendMessage(Locale.PREFIX + "§cVous ne pouvez pas taper les monstres sur cette île !");
			}
		}
		else if (victim instanceof Animals || victim.getType() == EntityType.SQUID) {
			if (!canDo(damager, victim.getLocation(), event, VisitorPermission.HITANIMALS, null)) {
				damager.sendMessage(Locale.PREFIX + "§cVous ne pouvez pas taper les animaux sur cette île !");
			}
		}
		else if (victim instanceof Golem) {
			if (!canDo(damager, victim.getLocation(), event, VisitorPermission.HITGOLEMS, null)) {
				damager.sendMessage(Locale.PREFIX + "§cVous ne pouvez pas taper les golems sur cette île !");
			}
		}
	}

	private static boolean isShulker(Material material) {
		return material.ordinal() >= Material.WHITE_SHULKER_BOX.ordinal() && material.ordinal() <= Material.BLACK_SHULKER_BOX.ordinal();
	}

}
