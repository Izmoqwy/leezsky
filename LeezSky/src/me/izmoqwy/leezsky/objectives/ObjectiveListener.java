package me.izmoqwy.leezsky.objectives;

import me.izmoqwy.leezsky.managers.SettingsManager;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.material.MaterialData;

public class ObjectiveListener implements Listener, SettingsManager.SettingUser {

	public static ObjectiveListener INSTANCE = new ObjectiveListener();

	@EventHandler
	public void onJoin(final PlayerJoinEvent event) {
		ObjectiveManager.loadPlayer(event.getPlayer());
		ObjectiveManager.addToBB(event.getPlayer());
	}

	@EventHandler
	public void onQuit(final PlayerQuitEvent event) {
		ObjectiveManager.removeFromBB(event.getPlayer());
	}

	@EventHandler
	public void onBreakBlock(final BlockBreakEvent event) {
		LeezObjective objective = ObjectiveManager.getCurrentObjective(event.getPlayer());
		if (objective != null && objective.getAction() == ObjectiveAction.BREAK) {
			if (objective.toBreak.contains(new MaterialData(event.getBlock().getType()))) {
				ObjectiveManager.complete(objective, event.getPlayer());
			}
		}
	}

	@EventHandler
	public void onBucketDispense(final PlayerBucketEmptyEvent event) {
		LeezObjective objective = ObjectiveManager.getCurrentObjective(event.getPlayer());
		if (objective != null && objective.getAction() == ObjectiveAction.BUCKET) {
			if (objective.toBreak.contains(new MaterialData(event.getBucket()))) {
				ObjectiveManager.complete(objective, event.getPlayer());
			}
		}
	}

	@EventHandler
	public void onEntityDeath(final EntityDeathEvent event) {
		if (event.getEntity() == null || event.getEntityType() == EntityType.PLAYER)
			return;

		LivingEntity entity = event.getEntity();
		if (entity.getKiller() != null) {
			Player player = entity.getKiller();
			LeezObjective objective = ObjectiveManager.getCurrentObjective(player);
			if (objective != null && objective.getAction() == ObjectiveAction.KILL) {
				if (objective.mobType == MobType.HOSTILE) {
					if (event.getEntity() instanceof Monster || event.getEntityType() == EntityType.SLIME) {
						ObjectiveManager.complete(objective, player);
					}
				}
			}
		}
	}

	@EventHandler
	public void onCraft(final CraftItemEvent event) {
		Player player = (Player) event.getWhoClicked();

		LeezObjective objective = ObjectiveManager.getCurrentObjective(player);
		if (objective != null && objective.getAction() == ObjectiveAction.CRAFT) {
			if (objective.toBreak.contains(event.getRecipe().getResult().getData())) {
				ObjectiveManager.complete(objective, player);
			}
		}
	}

	@EventHandler
	public void onSmelt(final FurnaceExtractEvent event) {
		Player player = event.getPlayer();

		LeezObjective objective = ObjectiveManager.getCurrentObjective(player);
		if (objective != null && objective.getAction() == ObjectiveAction.SMELT) {
			if (objective.toBreak.contains(new MaterialData(event.getItemType()))) {
				ObjectiveManager.complete(objective, player, event.getItemAmount());
			}
		}
	}

	@Override
	public void onSettingUpdate(Player player, SettingsManager.Setting setting, Enum value) {
		if (value == SettingsManager.SimpleToggle.ON)
			ObjectiveManager.addToBB(player);
		else
			ObjectiveManager.removeFromBB(player);
	}
}
