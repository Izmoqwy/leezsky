package lz.izmoqwy.shop.commands;

import lz.izmoqwy.core.command.CommandOptions;
import lz.izmoqwy.core.command.CoreCommand;
import lz.izmoqwy.shop.gui.ShopMainGUI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommand extends CoreCommand {

	public ShopCommand() {
		super("shop", CommandOptions.builder()
				.playerOnly(true)
				.build());
	}

	@Override
	protected void execute(CommandSender commandSender, String usedCommand, String[] args) {
		new ShopMainGUI((Player) commandSender).open();
	}

}
