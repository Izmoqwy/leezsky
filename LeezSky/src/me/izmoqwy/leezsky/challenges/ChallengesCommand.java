package me.izmoqwy.leezsky.challenges;

import lz.izmoqwy.core.command.CommandOptions;
import lz.izmoqwy.core.command.CoreCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChallengesCommand extends CoreCommand {

	public ChallengesCommand() {
		super("challenges", CommandOptions.builder()
				.permission("leezsky.commands.challenges").playerOnly(true)
				.build());
	}

	@Override
	protected void execute(CommandSender commandSender, String usedCommand, String[] args) {
		Player player = (Player) commandSender;
		checkValid(!ChallengesManager.get.getCategories().isEmpty(), "Les défis n'ont pas été chargés correctement lors du démarrage.");

		new ChallengeCategoryGUI(player, ChallengesManager.get.getCurrentCategory(player));
	}

}
