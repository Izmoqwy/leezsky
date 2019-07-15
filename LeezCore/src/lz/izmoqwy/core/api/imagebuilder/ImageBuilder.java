package lz.izmoqwy.core.api.imagebuilder;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;

public interface ImageBuilder {

	public BufferedImage getBackground();
	
	public ImageBuilder text(String text, Font font, Color color, int tx, int ty);
	public ImageBuilder image(BufferedImage subimage, int x, int y);
	public ImageBuilder image(BufferedImage subimage, int x, int y, int width, int height);
	
	public BufferedImage build();
	
}
