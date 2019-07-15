/*
 * That file is a part of [Leezsky] Shop
 * Copyright Izmoqwy
 * You can edit for your personal use.
 * You're not allowed to redistribute it at your own.
 */

package lz.izmoqwy.leezshop.playershop;

import java.awt.image.BufferedImage;

public class PremiumShop {

	private final BufferedImage image;
	
	public PremiumShop(BufferedImage image)
	{
		this.image = image;
	}
	
	public BufferedImage getImage()
	{
		return image;
	}
	
}
