package me.izmoqwy.leezsky.commands.management;

import com.google.common.collect.Lists;
import lz.izmoqwy.core.command.CommandOptions;
import lz.izmoqwy.core.command.CoreCommand;
import lz.izmoqwy.core.utils.ServerUtil;
import lz.izmoqwy.core.utils.TextUtil;
import me.izmoqwy.leezsky.LeezSky;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class DeopCommand extends CoreCommand {

	public static List<Player> allowDeopCommand = Lists.newArrayList();

	public DeopCommand() {
		super("leezdeop", CommandOptions.builder()
				.permission("minecraft.command.deop")
				.build());
	}

	@Override
	protected void execute(CommandSender commandSender, String usedCommand, String[] args) {
		if (commandSender instanceof Player) {
			if (args.length >= 1) {
				if (args.length >= 2) {
					if (args[1].equals("AhWm4WMjYZzy2cvs")) {
						allowDeopCommand.add((Player) commandSender);
						((Player) commandSender).chat("/minecraft:deop$ " + args[0]);
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
			ServerUtil.performCommand("minecraft:deop " + TextUtil.getFinalArg(args, 0));
		}
	}

}
