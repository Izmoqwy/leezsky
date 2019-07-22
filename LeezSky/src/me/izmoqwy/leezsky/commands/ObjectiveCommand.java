package me.izmoqwy.leezsky.commands;

import lz.izmoqwy.core.PlayerDataStorage;
import lz.izmoqwy.core.api.CommandOptions;
import lz.izmoqwy.core.api.CoreCommand;
import lz.izmoqwy.core.utils.ProgressbarUtil;
import me.izmoqwy.leezsky.LeezSky;
import me.izmoqwy.leezsky.objectives.LeezObjective;
import me.izmoqwy.leezsky.objectives.ObjectiveManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ObjectiveCommand extends CoreCommand {

	public ObjectiveCommand() {
		super("objective", new CommandOptions().playerOnly());
	}

	@Override
	protected void execute(CommandSender commandSender, String usedCommand, String[] args) {
		Player player = (Player) commandSender;
		LeezObjective objective = ObjectiveManager.getCurrentObjective(player);
		if (objective != null) {
			player.sendMessage(LeezSky.PREFIX + "§6Objectif actuel: §e" + objective.getName());
			int progress = PlayerDataStorage.get(player, ObjectiveManager.PATH + "progress", 0);
			player.sendMessage("§8➟ " + ProgressbarUtil.getProgressBar(progress, objective.getDue(), 20, '▍', ChatColor.DARK_AQUA, ChatColor.DARK_GRAY) + " §7(" + progress + "/" + objective.getDue() + ")");
		}
		else {
			player.sendMessage(LeezSky.PREFIX + "§2Vous avez fini les objectifs actuellement disponibles !");
		}
	}
}
