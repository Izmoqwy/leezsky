package lz.izmoqwy.leezcrates.listeners;

import lz.izmoqwy.leezcrates.LeezCrates;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class CrateListener implements Listener {

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBreakCrate(BlockBreakEvent event) {
		if (LeezCrates.getCrates().containsKey(event.getBlock().getLocation())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onOpenClick(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (LeezCrates.getCrates().containsKey(event.getClickedBlock().getLocation())) {
				event.setCancelled(true);
				LeezCrates.open(LeezCrates.getCrates().get(event.getClickedBlock().getLocation()), event.getPlayer());
			}
		}
	}

	@EventHandler
	public void onPickup(EntityPickupItemEvent event) {
		if (event.getEntityType() == EntityType.PLAYER) {
			//noinspection SuspiciousMethodCalls
			if (LeezCrates.getOpeningPlayers().contains(event.getEntity())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onClick(InventoryClickEvent event) {
		//noinspection SuspiciousMethodCalls
		if (LeezCrates.getOpeningPlayers().contains(event.getWhoClicked())) {
			event.setCancelled(true);
		}
	}

}
