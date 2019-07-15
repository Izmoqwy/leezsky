/*
 * That file is a part of [Leezsky] Shop
 * Copyright Izmoqwy
 * You can edit for your personal use.
 * You're not allowed to redistribute it at your own.
 */

package lz.izmoqwy.leezshop.pictures;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;

import org.bukkit.map.MapFont;
import org.bukkit.map.MinecraftFont;

public class FontRenderer {	

	/*
	 * 
	 */
	
	public static int getWidth(Font font, String text)
	{
		
		return (int)font.getStringBounds(text, new FontRenderContext(new AffineTransform(), true, true)).getWidth();
		
	}
	
	/*
	 * 
	 */
	
	public static int getCharacterWidth(char c)
	{
		if(c >= '\u2588' && c <= '\u258F')
		{
			return ('\u258F' - c) + 2;
		}
		
		switch(c)
		{
			case ' ':
				return 4;
			case '\u2714':
				return 8;
			case '\u2718':
				return 7;
			default:
				MapFont.CharacterSprite mcChar = MinecraftFont.Font.getChar(c);
				if(mcChar != null)
					return mcChar.getWidth() + 1;
				return 0;
		}
	}
	
    public static int getStringWidth(String text)
    {
        if (text == null)
        {
            return 0;
        }
        else
        {
            float f = 0.0F;
            boolean flag = false;

            for (int i = 0; i < text.length(); ++i)
            {
                char c0 = text.charAt(i);
                float f1 = getCharacterWidth(c0);

                if (f1 < 0.0F && i < text.length() - 1)
                {
                    ++i;
                    c0 = text.charAt(i);

                    if (c0 != 'l' && c0 != 'L')
                    {
                        if (c0 == 'r' || c0 == 'R')
                        {
                            flag = false;
                        }
                    }
                    else
                    {
                        flag = true;
                    }

                    f1 = 0.0F;
                }

                f += f1;

                if (flag && f1 > 0.0F)
                {
                    f += 1.0F;
                }
            }

            return (int)f;
        }
    }
	
}
