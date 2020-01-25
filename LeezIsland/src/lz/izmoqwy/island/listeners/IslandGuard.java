package lz.izmoqwy.island.listeners;

import lz.izmoqwy.island.Locale;
import lz.izmoqwy.island.commands.AdminCommand;
import lz.izmoqwy.island.generator.OreGenerator;
import lz.izmoqwy.island.grid.GridManager;
import lz.izmoqwy.island.island.Island;
import lz.izmoqwy.island.island.permissions.CoopPermission;
import lz.izmoqwy.island.island.permissions.GeneralPermission;
import lz.izmoqwy.island.island.permissions.VisitorPermission;
import lz.izmoqwy.island.spawners.SpawnersManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.spigotmc.event.entity.EntityMountEvent;

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
		Island island = GridManager.getIslandAt(location);
		if (island != null) {
			if (island.hasFullAccess(player))
				event.setCancelled(bool = false);
			else {
				if (island.isCooped(player)) {
					if (ifCoop != null) {
						event.setCancelled(bool = !island.hasCoopPermission(ifCoop));
					}
					else
						event.setCancelled(bool = false);
				}
				else {
					// Player can be only a visitor
					if (ifVisitor != null && island.hasVisitorPermission(ifVisitor)) {
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

	private boolean noIslandAt(Location location) {
		return GridManager.getIslandAt(location) == null;
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private boolean testGeneralPermission(Location location, GeneralPermission setting) {
		Island island = GridManager.getIslandAt(location);
		if (island == null)
			return false;

		return island.hasGeneralPermission(setting);
	}

	/*
		World Check Methods
	 */
	private boolean notInWorld(Location location) {
		return notInWorld(location.getWorld());
	}

	private boolean notInWorld(Block block) {
		return notInWorld(block.getWorld());
	}

	private boolean notInWorld(World world) {
		return GridManager.notOnGrid(world);
	}

	/*
		Glitches prevention
	 */
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPistonExtend(BlockPistonExtendEvent event) {
		if (event.getBlocks().isEmpty() || notInWorld(event.getBlock()))
			return;
		event.setCancelled(noIslandAt(event.getBlocks().get(event.getBlocks().size() - 1).getRelative(event.getDirection()).getLocation()));
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onDispenseEvent(BlockDispenseEvent event) {
		if (notInWorld(event.getBlock()))
			return;

		Location targetLocation = event.getVelocity().toLocation(event.getBlock().getWorld());
		event.setCancelled(noIslandAt(targetLocation));
	}

	/*
		Teleporting
	 */
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onTeleport(PlayerTeleportEvent event) {
		if (notInWorld(event.getTo()))
			return;

		Player player = event.getPlayer();
		if (AdminCommand.BYPASSING.contains(player.getUniqueId()))
			return;

		Island island = GridManager.getIslandAt(event.getTo());
		if (island != null) {
			Island from = GridManager.getIslandAt(event.getFrom());
			if (from == island)
				return;

			if (island.isLocked()) {
				event.setCancelled(true);
				Locale.GUARD_LOCKED.send(player);
			}
			else if (island.getBanList().contains(player.getUniqueId())) {
				event.setCancelled(true);
				Locale.GUARD_BANNED.send(player);
			}
			else {
				if (island.hasFullAccess(player))
					return;

				if (island.getName() != null)
					Locale.GUARD_ENTER_NAMED.send(player, island.getName());
				else
					Locale.GUARD_ENTER_NONAME.send(player, island.getOwner().getName());

				if (!island.hasVisitorPermission(VisitorPermission.FLY) && player.getAllowFlight()) {
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
		if (notInWorld(event.getLocation()))
			return;

		event.setCancelled(!testGeneralPermission(event.getLocation(), GeneralPermission.SPAWNERS));
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onMobSpawn(CreatureSpawnEvent event) {
		if (notInWorld(event.getLocation()) || !(event.getEntity() instanceof Monster))
			return;

		event.setCancelled(!testGeneralPermission(event.getLocation(), GeneralPermission.MOB_SPAWN));
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onFlow(BlockFromToEvent event) {
		if (notInWorld(event.getToBlock()))
			return;

		Block toBlock = event.getToBlock();
		if (toBlock.getType() != Material.AIR)
			return;

		Island island = GridManager.getIslandAtSafe(toBlock.getLocation());
		if (island != null) {
			if (!island.hasGeneralPermission(GeneralPermission.FLUID_FLOW)) {
				event.setCancelled(true);
				return;
			}

			if (!island.hasGeneralPermission(GeneralPermission.CUSTOM_GENERATOR))
				return;

			int flowId = event.getBlock().getTypeId();
			if (flowId >= 8 && flowId <= 11 && oreGenerator.shouldGenerates(toBlock, flowId)) {
				toBlock.setType(oreGenerator.randomize(island));
			}
		}
		else {
			event.setCancelled(true);
		}
	}

	/*
		Guardian
	 */
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onToggleFly(PlayerToggleFlightEvent event) {
		Location location = event.getPlayer().getLocation();
		if (notInWorld(location))
			return;

		canDo(event.getPlayer(), location, event, VisitorPermission.FLY, null);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onCommand(PlayerCommandPreprocessEvent event) {
		Location location = event.getPlayer().getLocation();
		if (notInWorld(location))
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
		if (notInWorld(event.getBlock()))
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
		if (notInWorld(event.getBlock()))
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
		if (notInWorld(event.getBlockClicked()))
			return;
		canDo(event.getPlayer(), event.getBlockClicked(), event, null, CoopPermission.BUCKETS);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onFillBucket(PlayerBucketFillEvent event) {
		if (notInWorld(event.getBlockClicked()))
			return;
		canDo(event.getPlayer(), event.getBlockClicked(), event, null, CoopPermission.BUCKETS);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onLeash(PlayerLeashEntityEvent event) {
		if (notInWorld(event.getEntity().getWorld()))
			return;
		canDo(event.getPlayer(), event.getEntity().getLocation(), event, VisitorPermission.USE_LEASH, null);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onUnleash(PlayerUnleashEntityEvent event) {
		if (notInWorld(event.getEntity().getWorld()))
			return;
		canDo(event.getPlayer(), event.getEntity().getLocation(), event, VisitorPermission.USE_LEASH, null);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPickup(EntityPickupItemEvent event) {
		if (notInWorld(event.getEntity().getWorld()))
			return;
		if (event.getEntityType() == EntityType.PLAYER) {
			canDo((Player) event.getEntity(), event.getEntity().getLocation(), event, VisitorPermission.PICKUP, null);
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onDrop(PlayerDropItemEvent event) {
		if (notInWorld(event.getPlayer().getWorld()))
			return;
		canDo(event.getPlayer(), event.getPlayer().getLocation(), event, VisitorPermission.DROP, null);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (notInWorld(player.getWorld()))
			return;

		if (event.getAction() == Action.PHYSICAL) {
			if (event.hasBlock()) {
				Block block = event.getClickedBlock();
				switch (block.getType()) {
					case SOIL:
						event.setCancelled(true);
						return;
					case GOLD_PLATE:
					case IRON_PLATE:
					case STONE_PLATE:
					case WOOD_PLATE:
						canDo(player, player.getLocation(), event, VisitorPermission.PLATES, CoopPermission.ACTIVATORS);
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
					canDo(player, clicked, event, VisitorPermission.LEVERS, CoopPermission.ACTIVATORS);
					break;
				case STONE_BUTTON:
				case WOOD_BUTTON:
					canDo(player, clicked, event, VisitorPermission.BUTTONS, CoopPermission.ACTIVATORS);
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
	public void onInventoryOpen(InventoryOpenEvent event) {
		Player player = (Player) event.getPlayer();
		if (notInWorld(player.getWorld()))
			return;

		if (event.getInventory().getType() == InventoryType.MERCHANT) {
			Inventory inventory = event.getInventory();
			if (inventory.getHolder() != null && inventory.getHolder() instanceof LivingEntity) {
				canDo(player, ((LivingEntity) inventory.getHolder()).getLocation(), event, VisitorPermission.VILLAGERS, null);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onHit(EntityDamageByEntityEvent event) {
		if (event.getDamager().getType() != EntityType.PLAYER || notInWorld(event.getDamager().getWorld()))
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
	public void onShootBow(EntityShootBowEvent event) {
		if (event.getEntity().getType() != EntityType.PLAYER || notInWorld(event.getEntity().getWorld()))
			return;

		Player shooter = (Player) event.getEntity();
		if (!canDo(shooter, shooter.getLocation(), event, VisitorPermission.USE_BOW, null)) {
			shooter.sendMessage(Locale.PREFIX + "§cVous ne pouvez pas utiliser les arcs sur cette île !");
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onFish(PlayerFishEvent event) {
		Entity victim = event.getCaught();
		if (victim == null || notInWorld(victim.getWorld()))
			return;

		Player damager = event.getPlayer();
		if (!canDo(damager, damager.getLocation(), event, VisitorPermission.FISH, null)) {
			damager.sendMessage(Locale.PREFIX + "§cVous ne pouvez pas utiliser de canne à pêche sur cette île !");
		}
		else if (victim.getType() == EntityType.PLAYER) {
			if (victim.equals(damager))
				return;

			event.setCancelled(true);
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

		if (event.isCancelled()) {
			event.getHook().remove();
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onEntityMount(EntityMountEvent event) {
		if (notInWorld(event.getMount().getWorld()) || event.getEntity().getType() != EntityType.PLAYER)
			return;

		Player rider = (Player) event.getEntity();
		if (!canDo(rider, event.getMount().getLocation(), event, VisitorPermission.RIDING, null)) {
			rider.sendMessage(Locale.PREFIX + "§cVous ne pouvez pas monter sur les animaux sur cette île !");
		}
	}

	private static boolean isShulker(Material material) {
		return material.ordinal() >= Material.WHITE_SHULKER_BOX.ordinal() && material.ordinal() <= Material.BLACK_SHULKER_BOX.ordinal();
	}

}
