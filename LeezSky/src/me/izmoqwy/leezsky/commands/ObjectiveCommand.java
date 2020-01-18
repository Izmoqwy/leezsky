package me.izmoqwy.leezsky.commands;

import lz.izmoqwy.core.PlayerDataStorage;
import lz.izmoqwy.core.command.CommandOptions;
import lz.izmoqwy.core.command.CoreCommand;
import lz.izmoqwy.core.utils.MathUtil;
import me.izmoqwy.leezsky.LeezSky;
import me.izmoqwy.leezsky.objectives.LeezObjective;
import me.izmoqwy.leezsky.objectives.ObjectiveManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ObjectiveCommand extends CoreCommand {

	public ObjectiveCommand() {
		super("objective", CommandOptions.builder()
				.playerOnly(true)
				.build());
	}

	@Override
	protected void execute(CommandSender commandSender, String usedCommand, String[] args) {
		Player player = (Player) commandSender;
		LeezObjective objective = ObjectiveManager.getCurrentObjective(player);
		if (objective != null) {
			player.sendMessage(LeezSky.PREFIX + "§6Objectif actuel: §e" + objective.getName());
			int progress = PlayerDataStorage.get(player, ObjectiveManager.PATH + "progress", 0);
			player.sendMessage("§8➟ " + MathUtil.getProgressBar(20,
					progress, objective.getDue(), '▍', ChatColor.DARK_AQUA, ChatColor.DARK_GRAY) + " §7(" + progress + "/" + objective.getDue() + ")");
		}
		else {
			player.sendMessage(LeezSky.PREFIX + "§2Vous avez fini les objectifs actuellement disponibles !");
		}
	}

}
