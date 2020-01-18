package me.izmoqwy.leezsky.commands;

import lz.izmoqwy.core.command.CommandOptions;
import lz.izmoqwy.core.command.CoreCommand;
import lz.izmoqwy.core.utils.PlayerUtil;
import lz.izmoqwy.core.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AnnounceCommand extends CoreCommand {

	public AnnounceCommand() {
		super("announce", CommandOptions.builder()
				.permission("leezsky.commands.announce")
				.cooldown(60)
				.build());
	}

	@Override
	protected void execute(CommandSender commandSender, String usedCommand, String[] args) {
		switch (args[0]) {
			case "info":
				sendAnnounceMessage(TextUtil.getFinalArg(args, 1));
				break;
			case "important":
			case "severe":
				sendAnnounceMessage(TextUtil.getFinalArg(args, 1));
				for (Player player : Bukkit.getOnlinePlayers()) {
					player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
					PlayerUtil.sendTitle(player, "§4Annonce importante", "§cRegardez le chat", 3);
				}
				break;
			default:
				sendAnnounceMessage(TextUtil.getFinalArg(args, 0));
		}
	}

	private void sendAnnounceMessage(String message) {
		Bukkit.broadcastMessage("§4(Annonce) §c" + message.replace('&', '§'));
	}

}
