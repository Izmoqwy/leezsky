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

public class ShopSection {

	@Getter private String displayName, description;
	@Getter private boolean enchanted;
	@Getter private int slot, pages;
	@Getter private MaterialData icon;
	@Getter private List<ShopItem> items;
	
	public ShopSection(String displayName, boolean enchanted, int slot, String description, MaterialData data, List<ShopItem> items)
	{
		this.displayName = displayName;
		this.enchanted = enchanted;
		this.slot = slot;
		this.description = description;
		this.icon = data;
		this.items = items;
		this.pages = items.size() / 36;
	}
	
}
