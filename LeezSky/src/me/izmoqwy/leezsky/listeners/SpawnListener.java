package me.izmoqwy.leezsky.listeners;

import com.google.common.collect.Lists;
import me.izmoqwy.leezsky.LeezSky;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.util.List;

public class SpawnListener implements Listener {

	private final World SPAWN_WORLD;

	public SpawnListener(World spawnWorld) {
		SPAWN_WORLD = spawnWorld;
	}

	public boolean needProtectAt(Location location) {
		return needProtectAt(location.getWorld());
	}

	public boolean needProtectAt(Entity entity) {
		return needProtectAt(entity.getWorld());
	}

	public boolean needProtectAt(Block block) {
		return needProtectAt(block.getWorld());
	}

	public boolean needProtectAt(World world) {
		return world.equals(SPAWN_WORLD);
	}

	/*
		Stupid preventions
	 */
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onMobSpawn(final EntitySpawnEvent event) {
		event.setCancelled(needProtectAt(event.getEntity()));
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onWeather(final WeatherChangeEvent event) {
		event.setCancelled(needProtectAt(event.getWorld()));
	}

	/*
	 * Protections
	 */
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onBreak(final BlockBreakEvent event) {
		event.setCancelled(needProtectAt(event.getPlayer().getWorld()) && !event.getPlayer().isOp());
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPlace(final BlockPlaceEvent event) {
		event.setCancelled(needProtectAt(event.getPlayer().getWorld()) && !event.getPlayer().isOp());
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onBucketDispense(final PlayerBucketEmptyEvent event) {
		event.setCancelled(needProtectAt(event.getPlayer().getWorld()) && !event.getPlayer().isOp());
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onBucketFill(final PlayerBucketFillEvent event) {
		event.setCancelled(needProtectAt(event.getPlayer().getWorld()) && !event.getPlayer().isOp());
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onBucketDispense(final PlayerArmorStandManipulateEvent event) {
		event.setCancelled(needProtectAt(event.getPlayer().getWorld()) && !event.getPlayer().isOp());
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onDamage(final EntityDamageEvent event) {
		event.setCancelled(needProtectAt(event.getEntity().getWorld()) && !event.getEntity().isOp());
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPlaceItemframe(final HangingPlaceEvent event) {
		event.setCancelled(needProtectAt(event.getEntity().getWorld()) && !event.getEntity().isOp());
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onBreakItemframe(final HangingBreakByEntityEvent event) {
		event.setCancelled(needProtectAt(event.getEntity().getWorld()) && !event.getEntity().isOp());
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onCatch(final PlayerFishEvent event) {
		if (event.getState() == PlayerFishEvent.State.CAUGHT_ENTITY) {
			Player player = event.getPlayer();
			Entity entity = event.getCaught();
			if (needProtectAt(entity)) {
				player.sendMessage(LeezSky.PREFIX + "§cLe PvP n'est pas activé dans ce monde !");
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onHit(final EntityDamageByEntityEvent event) {
		Entity victim = event.getEntity();
		if (event.getDamager().getType() == EntityType.PLAYER) {
			Player attacker = (Player) event.getDamager();
			if (victim.getType() == EntityType.PLAYER) {
				event.setCancelled(true);
				attacker.sendMessage(LeezSky.PREFIX + "§cLe PvP n'est pas activé dans ce monde !");
			}
		}
		else if (event.getDamager() instanceof Projectile) {
			ProjectileSource shooter = ((Projectile) event.getDamager()).getShooter();
			if (shooter instanceof Player) {
				if (victim.getType() == EntityType.PLAYER) {
					Player attacker = (Player) shooter;
					attacker.sendMessage(LeezSky.PREFIX + "§cLe PvP n'est pas activé dans ce monde !");
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onEnityExplode(final EntityExplodeEvent event) {
		List<Block> removables = Lists.newArrayList();
		for (Block block : event.blockList()) {
			if (needProtectAt(block)) {
				removables.add(block);
			}
		}

		for (Block removable : removables) {
			event.blockList().remove(removable);
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onBlockExplode(final BlockExplodeEvent event) {
		List<Block> removables = Lists.newArrayList();
		for (Block block : event.blockList()) {
			if (needProtectAt(block)) {
				removables.add(block);
			}
		}

		for (Block removable : removables) {
			event.blockList().remove(removable);
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onBlockIgnite(final BlockIgniteEvent event) {
		event.setCancelled(needProtectAt(event.getBlock()));
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onBlockBurn(final BlockBurnEvent event) {
		event.setCancelled(needProtectAt(event.getBlock()));
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onInteract(final PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (!needProtectAt(event.getClickedBlock())) return;

		ItemStack hand = event.getItem();
		if (hand == null) return;

		if (hand.getType() == Material.FLINT_AND_STEEL || hand.getType() == Material.FIREBALL) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onSplashPotion(final PotionSplashEvent event) {
		event.setCancelled(needProtectAt(event.getEntity()));
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPortal(final PlayerPortalEvent event) {
		event.setCancelled(true);
		event.getPlayer().performCommand("warp nether");
	}

}
