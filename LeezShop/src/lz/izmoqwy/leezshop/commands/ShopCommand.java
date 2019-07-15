/*
 * That file is a part of [Leezsky] Shop
 * Copyright Izmoqwy
 * You can edit for your personal use.
 * You're not allowed to redistribute it at your own.
 */

package lz.izmoqwy.leezshop.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import lz.izmoqwy.leezshop.LeezShop;
import lz.izmoqwy.leezshop.ShopLoader;

public class ShopCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if(command.getName().equalsIgnoreCase("shop"))
		{
			if(args.length == 0)
			{
				if(sender instanceof Player)
				{
					((Player) sender).openInventory(ShopLoader.loadInventory());
				}
				else
				{
					sender.sendMessage( LeezShop.PREFIX + "§cSeul un joueur peut utiliser une interface utilisateur." );
				}
			}
			return true;
			
		}
		return false;

	}
	
}
