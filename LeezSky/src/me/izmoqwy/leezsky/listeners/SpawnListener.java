/*
 * That file is a part of [Leezsky] LeezSky
 * Copyright Izmoqwy
 * You can edit for your personal use.
 * You're not allowed to redistribute it at your own.
 */

package me.izmoqwy.leezsky.listeners;

import java.util.List;

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
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import com.google.common.collect.Lists;

import me.izmoqwy.leezsky.LeezSky;

public class SpawnListener implements Listener {
	
	public boolean needProtectAt(Location location) {
		
		return needProtectAt(location.getWorld());
		
	}
	
	public boolean needProtectAt(Entity entity) {
		
		return needProtectAt(entity.getLocation());
		
	}
	
	public boolean needProtectAt(Block block) {
		
		return needProtectAt(block.getLocation());
		
	}
	
	public boolean needProtectAt(World world) {
		
		if(world.getName().equalsIgnoreCase("Spawn")) return true;
		return false;
		
	}

	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onMobSpawn(final EntitySpawnEvent event)
	{
		
		event.setCancelled(needProtectAt(event.getEntity()));
		
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onWeather(final WeatherChangeEvent event)
	{
		
		event.setCancelled(needProtectAt(event.getWorld()));
		
	}
	
	/*
	 * Protections
	 */
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onBreak(final BlockBreakEvent event)
	{
		
		event.setCancelled(needProtectAt(event.getPlayer().getWorld()) && !event.getPlayer().isOp());
		
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onPlace(final BlockPlaceEvent event)
	{
		
		event.setCancelled(needProtectAt(event.getPlayer().getWorld()) && !event.getPlayer().isOp());
		
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onBucketDispense(final PlayerBucketEmptyEvent event)
	{
		
		event.setCancelled(needProtectAt(event.getPlayer().getWorld()) && !event.getPlayer().isOp());
		
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onBucketFill(final PlayerBucketFillEvent event)
	{
		
		event.setCancelled(needProtectAt(event.getPlayer().getWorld()) && !event.getPlayer().isOp());
		
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onBucketDispense(final PlayerArmorStandManipulateEvent event)
	{
		
		event.setCancelled(needProtectAt(event.getPlayer().getWorld()) && !event.getPlayer().isOp());
		
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onDamage(final EntityDamageEvent event)
	{
		
		event.setCancelled(needProtectAt(event.getEntity().getWorld()) && !event.getEntity().isOp());
		
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onPlaceItemframe(final HangingPlaceEvent event) {
		
		event.setCancelled(needProtectAt(event.getEntity().getWorld()) && !event.getEntity().isOp());
		
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onBreakItemframe(final HangingBreakByEntityEvent event) {
		
		event.setCancelled(needProtectAt(event.getEntity().getWorld()) && !event.getEntity().isOp());
		
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onCatch(final PlayerFishEvent event) {
		
		if(event.getState() == PlayerFishEvent.State.CAUGHT_ENTITY) {
			
			Player player = event.getPlayer();
			Entity entity = event.getCaught();
			
			if(needProtectAt(entity)) {
				
				player.sendMessage( LeezSky.PREFIX + "§cLe PvP n'est pas activé dans ce monde !" );
				event.setCancelled(true);
				
			}
			
		}
		
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onHit(final EntityDamageByEntityEvent event) {
		
		Entity victim = event.getEntity();
		if(event.getDamager().getType() == EntityType.PLAYER) {
			
			Player attacker = (Player) event.getDamager();
			if(attacker.isOp())
				return;
			if(victim.getType() == EntityType.PLAYER) {
				
				event.setCancelled(true);
				attacker.sendMessage( LeezSky.PREFIX + "§cLe PvP n'est pas activé dans ce monde !" );
				return;
				
			}
			
		}else if(event.getDamager() instanceof Projectile) {
			
			ProjectileSource shooter = (ProjectileSource)((Projectile)event.getDamager()).getShooter();
			if(shooter instanceof Player) {
				
				if(victim.getType() == EntityType.PLAYER) {
					
					Player attacker = (Player)shooter;
					attacker.sendMessage( LeezSky.PREFIX + "§cLe PvP n'est pas activé dans ce monde !" );
					event.setCancelled(true);	
					
				}
				
			}
			
		}
		
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onExplode(final EntityExplodeEvent event) {
		
		List<Block> removables = Lists.newArrayList();
		for(Block block : event.blockList()) {
			
			if(needProtectAt(block)) {
				
				removables.add(block);
				
			}
			
		}
		
		for(Block removable : removables) {
			
			event.blockList().remove(removable);
			
		}
		
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onExplode(final BlockExplodeEvent event) {
		
		List<Block> removables = Lists.newArrayList();
		for(Block block : event.blockList()) {
			
			if(needProtectAt(block)) {
				
				removables.add(block);
				
			}
			
		}
		
		for(Block removable : removables) {
			
			event.blockList().remove(removable);
			
		}
		
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onBlockIgnite(final BlockIgniteEvent event) {
		
		Block block = event.getBlock();
		if(needProtectAt(block)) {
			
			event.setCancelled(true);
			
		}
		
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onBlockBurn(final BlockBurnEvent event) {
		
		Block block = event.getBlock();
		if(needProtectAt(block)) {
			
			event.setCancelled(true);
			
		}
		
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onInteract(final PlayerInteractEvent event) {
		
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if(!needProtectAt(event.getClickedBlock())) return;
		
		ItemStack hand = event.getItem();
		if(hand == null) return;
		
		if(hand.getType() == Material.FLINT_AND_STEEL || hand.getType() == Material.FIREBALL) {
			
			event.setCancelled(true);
			
		}
	
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onSplashPotion(final PotionSplashEvent event) {
		
		if(needProtectAt(event.getEntity().getLocation())) {
			
			event.setCancelled(true);
			
		}
		
	}
	
	@EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
	public void onPortal(final PlayerPortalEvent event) {
		
		event.setCancelled(true);
		event.getPlayer().performCommand("warp nether");
		
	}
	
}
