/*
 * That file is a part of [Leezsky] LeezSky
 * Copyright Izmoqwy
 * You can edit for your personal use.
 * You're not allowed to redistribute it at your own.
 */

package me.izmoqwy.leezsky.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.izmoqwy.leezsky.LeezSky;

public class HelpCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if(command.getName().equalsIgnoreCase("help"))
		{
			if(args.length >= 1)
			{
				switch(args[0])
				{
				case "leezsky":
					sender.sendMessage(" ");
					sender.sendMessage( LeezSky.PREFIX + "§6Le serveur dispose de son propre plugin skyblock, cela permet d'avoir le contrôle total sur le bugs qui peuvent survenir." );
					sender.sendMessage( LeezSky.PREFIX + "§eVous pouvez attribuer trois rôles aux membres de votre île : Recruteur (Il peut inviter des gens sur votre île ainsi qu'en coop), Officier (C'est comme un sous-chef), Chef (Vous passerez automatiquement Officier). Par défaut, un membre de votre île n'a aucun rôle spécifique mais a assez de permissions pour jouer sans administrer l'île." );
					sender.sendMessage(" ");
					break;
				case "skyblock":
					sender.sendMessage(" ");
					sender.sendMessage(LeezSky.PREFIX + "§6Le skyblock est un mode de jeu très apprécié sur Minecraft et le concept est assez simple et compréhensible.");
					sender.sendMessage(LeezSky.PREFIX + "§eQuand vous commencez l'aventure, vous arrivez sur une île. Le but est de développer un maximum votre île via le biai de farms, décorations, etc.. Poser des blocs permet de gagner des niveaux d'île en fonction de la valeur du bloc posé. Le skyblock en multijoueur apporte des choses tel qu'un classement des meilleurs îles, le fait de pouvoir jouer avec ses amis ou autres.");
					sender.sendMessage(LeezSky.PREFIX + "§d§oBesoin de plus d'informations sur le serveur ? Fais §5/help leezsky§d.");
					sender.sendMessage(" ");
					break;
				case "plugins":
					((Player)sender).chat("/plugins");
					break;
				case "commands":
				case "commandes":
					break;
				}
			}
			else
			{
				sender.sendMessage(" ");
				sender.sendMessage(LeezSky.PREFIX + "§3Liste des commandes d'aide:");
				sender.sendMessage("§6/help §eleezsky §8- §7Obtenir des informations sur le serveur en général");
				sender.sendMessage("§6/help §eskyblock §8- §7Obtenir des informations sur les principes d'un skyblock");
				sender.sendMessage("§6/help §eplugins §8- §7Obtenir des informations sur les plugins");
				sender.sendMessage("§6/help §ecommandes §8- §7Liste des commandes auquelles vous avez accès");
				sender.sendMessage(" ");
			}
			return true;
		}
		return false;
	}
	
}
