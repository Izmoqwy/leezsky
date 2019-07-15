/*
 * That file is a part of [Leezsky] Shop
 * Copyright Izmoqwy
 * You can edit for your personal use.
 * You're not allowed to redistribute it at your own.
 */

package lz.izmoqwy.leezshop.obj;

import lombok.Getter;

public class ItemPrice {
	
	@Getter private double buyPrice;
	@Getter private double sellPrice;
	
	public ItemPrice(double buyPrice, double sellPrice)
	{
		this.buyPrice = buyPrice;
		this.sellPrice = sellPrice;
	}

}
