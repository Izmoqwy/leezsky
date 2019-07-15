package me.izmoqwy.leezsky.challenges;

import me.izmoqwy.leezsky.challenges.obj.Challenges;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChallengeCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		Player player = (Player)sender;
		player.openInventory(Challenges.getInventory(player, Challenges.categories.get(ChallengePlugin.instance.getIndex(player))));
		return false;
		
	}
	
}
