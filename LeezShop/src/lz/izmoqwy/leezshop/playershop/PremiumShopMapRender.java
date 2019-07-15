/*
 * That file is a part of [Leezsky] Shop
 * Copyright Izmoqwy
 * You can edit for your personal use.
 * You're not allowed to redistribute it at your own.
 */

package lz.izmoqwy.leezshop.playershop;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import lz.izmoqwy.leezshop.LeezShop;

public class PremiumShopMapRender extends MapRenderer {

	@SuppressWarnings("deprecation")
	@Override
	public void render(MapView view, MapCanvas canvas, Player player) 
	{
		short id = view.getId();
		if(LeezShop.MAPS.containsKey(id))
		{
			PremiumShop shop = LeezShop.MAPS.get(id);
			canvas.drawImage(0, 0, shop.getImage());
		}
	}

}
