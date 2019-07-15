package lz.izmoqwy.core.commands;

import lz.izmoqwy.core.LeezCore;
import lz.izmoqwy.core.api.CommandOptions;
import lz.izmoqwy.core.api.CoreCommand;
import lz.izmoqwy.core.i18n.LocaleManager;
import org.bukkit.command.CommandSender;

public class LeezCoreCommand extends CoreCommand {

	public LeezCoreCommand() {
		super("leezcore", new CommandOptions().withPermission("leezsky.core.command"));
	}

	@Override
	protected void execute(CommandSender commandSender, String usedCommand, String[] args) {
		if (args.length >= 1 && c(args[0], "reloadmessages", "rlmsgs")) {
			LocaleManager.reloadMessages();
			commandSender.sendMessage(LeezCore.PREFIX + "§aMessages rechargés.");
		}
		else if(args.length >= 1 && c(args[0], "help", "?")) {
			commandSender.sendMessage(" ");
			commandSender.sendMessage(LeezCore.PREFIX + "§3Aide pour la commande /leezcore:");
			commandSender.sendMessage("§6/leezcore §ereloadmessages §8- §eRecharger les messages des plugins utilisant LeezCore.");
			commandSender.sendMessage(" ");
		}
		else {
			commandSender.sendMessage(LeezCore.PREFIX + "§cSous-commande inconnue. Faîtes '/leezcore help' pour obtenir la liste des commandes.");
		}
	}
}
