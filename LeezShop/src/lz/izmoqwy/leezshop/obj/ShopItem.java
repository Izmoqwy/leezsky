/*
 * That file is a part of [Leezsky] Shop
 * Copyright Izmoqwy
 * You can edit for your personal use.
 * You're not allowed to redistribute it at your own.
 */

package lz.izmoqwy.leezshop.obj;

import java.util.List;

import org.bukkit.material.MaterialData;

import lombok.Getter;

public class ShopItem {
	
	public ShopSection parent;
	
	@Getter private String name;
	@Getter private List<String> commands;
	@Getter private MaterialData data;
	private ItemPrice price;
	
	public ShopItem(String name, List<String> commands, MaterialData data, ItemPrice price)
	{
		this.name = name;
		this.commands = commands;
		this.data = data;
		this.price = price;
	}
	
	public boolean isBuyable()
	{
		return new Double(price.getBuyPrice()) != null;
	}
	
	public double getBuyPrice()
	{
		return price.getBuyPrice();
	}
	
	public boolean isSellable()
	{
		return new Double(price.getSellPrice()) != null;
	}
	
	public double getSellPrice()
	{
		return price.getSellPrice();
	}

}
