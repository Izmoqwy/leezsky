package lz.izmoqwy.core.top;

import lz.izmoqwy.core.LeezCore;
import lz.izmoqwy.core.api.CommandOptions;
import lz.izmoqwy.core.api.CoreCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TopCommand extends CoreCommand {

	private final String commandName;

	TopCommand(String name) {
		super(name, new CommandOptions());

		this.commandName = name;
	}

	@Override
	protected void execute(CommandSender commandSender, String usedCommand, String[] args) {
		if (commandSender instanceof Player) {
			commandSender.sendMessage(LeezCore.PREFIX + "§eOuverture du GUI de top... Cela peut prendre un certain temps...");
			((Player) commandSender).openInventory(TopManager.getGui(commandName));
		}
		else {
			commandSender.sendMessage(LeezCore.PREFIX + "§cLes commandes de tops ne sont pas encore implémentées sans GUI");
		}
	}

}
