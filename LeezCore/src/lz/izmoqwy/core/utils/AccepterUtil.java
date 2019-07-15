package lz.izmoqwy.core.utils;

import lz.izmoqwy.core.FireAction;
import lz.izmoqwy.core.LeezCore;
import lz.izmoqwy.core.fanciful.FancyMessage;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class AccepterUtil {

	public static void send(Player player, String acceptCommand, String denyCommand) {
		FancyMessage message = new FancyMessage();
		message.text(LeezCore.PREFIX + "§eAcceptez ou refusez en cliquant §6> ");
		message.then("✔").command("/" + acceptCommand).tooltip("§aAccepter").color(ChatColor.DARK_GREEN);
		message.then(" ");
		message.then("✖").command("/" + denyCommand).tooltip("§cRefuser").color(ChatColor.DARK_RED);
		message.send(player);
	}

}
