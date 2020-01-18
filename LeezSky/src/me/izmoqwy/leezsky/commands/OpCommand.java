package me.izmoqwy.leezsky.commands;

import com.google.common.collect.Lists;
import lz.izmoqwy.core.command.CommandOptions;
import lz.izmoqwy.core.command.CoreCommand;
import lz.izmoqwy.core.utils.ServerUtil;
import lz.izmoqwy.core.utils.TextUtil;
import me.izmoqwy.leezsky.LeezSky;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class OpCommand extends CoreCommand {

	public static List<Player> allowOpCommand = Lists.newArrayList();

	public OpCommand() {
		super("leezop", CommandOptions.builder()
				.permission("minecraft.command.op")
				.build());
	}

	@Override
	protected void execute(CommandSender commandSender, String usedCommand, String[] args) {
		if (commandSender instanceof Player) {
			if (args.length >= 1) {
				if (args.length >= 2) {
					if (args[1].equals("5EfervZFfAZAyf48")) {
						allowOpCommand.add((Player) commandSender);
						((Player) commandSender).chat("/minecraft:op$ " + args[0]);
					}
					else
						commandSender.sendMessage(LeezSky.PREFIX + "§cMot de passe invalide.");
				}
				else
					commandSender.sendMessage(LeezSky.PREFIX + "§cVeuillez indiquer le mot de passe.");
			}
			else
				commandSender.sendMessage(LeezSky.PREFIX + "§cVeuillez spécifier un joueur.");
		}
		else {
			ServerUtil.performCommand("minecraft:op " + TextUtil.getFinalArg(args, 0));
		}
	}

}
