package me.izmoqwy.leezsky.objectives;

import me.izmoqwy.leezsky.managers.SettingsManager;
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

import static org.bukkit.entity.EntityType.PLAYER;
import static org.bukkit.entity.EntityType.SLIME;

public class ObjectiveListener implements Listener, SettingsManager.SettingUser {

	public static ObjectiveListener INSTANCE = new ObjectiveListener();

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		ObjectiveManager.loadPlayer(event.getPlayer());
		ObjectiveManager.addToBB(event.getPlayer());
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		ObjectiveManager.removeFromBB(event.getPlayer());
	}

	@EventHandler
	public void onBreakBlock(BlockBreakEvent event) {
		LeezObjective objective = ObjectiveManager.getCurrentObjective(event.getPlayer());
		if (objective != null && objective.getAction() == ObjectiveAction.BREAK
				&& objective.getBlocks().contains(new MaterialData(event.getBlock().getType()))) {
			ObjectiveManager.complete(objective, event.getPlayer());
		}
	}

	@EventHandler
	public void onBucketDispense(PlayerBucketEmptyEvent event) {
		LeezObjective objective = ObjectiveManager.getCurrentObjective(event.getPlayer());
		if (objective != null && objective.getAction() == ObjectiveAction.EMPTY_BUCKET
				&& objective.getBlocks().contains(new MaterialData(event.getBucket()))) {
			ObjectiveManager.complete(objective, event.getPlayer());
		}
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		LivingEntity entity = event.getEntity();
		if (entity.getKiller() != null) {
			Player player = entity.getKiller();
			LeezObjective objective = ObjectiveManager.getCurrentObjective(player);

			if (objective != null && objective.getAction() == ObjectiveAction.KILL) {
				if (entity.getType() == PLAYER && objective.getMobType() != MobType.PLAYER)
					return;

				if (objective.getMobType() == MobType.HOSTILE &&
						!(entity instanceof Monster || entity.getType() == SLIME)) {
					return;
				}
				ObjectiveManager.complete(objective, player);
			}
		}
	}

	@EventHandler
	public void onCraft(CraftItemEvent event) {
		Player player = (Player) event.getWhoClicked();
		LeezObjective objective = ObjectiveManager.getCurrentObjective(player);
		if (objective != null && objective.getAction() == ObjectiveAction.CRAFT &&
				objective.getBlocks().contains(event.getRecipe().getResult().getData())) {
			ObjectiveManager.complete(objective, player);
		}
	}

	@EventHandler
	public void onSmelt(FurnaceExtractEvent event) {
		LeezObjective objective = ObjectiveManager.getCurrentObjective(event.getPlayer());
		if (objective != null && objective.getAction() == ObjectiveAction.SMELT &&
				objective.getBlocks().contains(new MaterialData(event.getItemType()))) {
			ObjectiveManager.complete(objective, event.getPlayer(), event.getItemAmount());
		}
	}

	@Override
	public void onSettingUpdate(Player player, SettingsManager.Setting<?> setting, Enum<?> value) {
		if (value == SettingsManager.SimpleToggle.ON)
			ObjectiveManager.addToBB(player);
		else
			ObjectiveManager.removeFromBB(player);
	}

}
