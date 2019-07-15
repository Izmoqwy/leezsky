/*
 * That file is a part of [Leezsky] Shop
 * Copyright Izmoqwy
 * You can edit for your personal use.
 * You're not allowed to redistribute it at your own.
 */

package lz.izmoqwy.leezshop.pictures;

import java.awt.image.BufferedImage;

import org.bukkit.map.MapCanvas;
import org.bukkit.map.MinecraftFont;

public class RendererUtils {
	
	public static void drawText(MapCanvas canvas, int x, int y, String text)
	{
		
		canvas.drawText(x, y, MinecraftFont.Font, text);
		
	}
	
	public static void drawCenteredXText(MapCanvas canvas, int y, String text)
	{
		
		int width = FontRenderer.getStringWidth(text);
		if(width > 126) return;
		int i = (128-width) / 2;
		drawText(canvas, i, y, text);
		
	}
	
	public static void drawImage(MapCanvas canvas, int startX, int startY, BufferedImage image)
	{
		
		canvas.drawImage(startX, startY, image);
		
	}

}
