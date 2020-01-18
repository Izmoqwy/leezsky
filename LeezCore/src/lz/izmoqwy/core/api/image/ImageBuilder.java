package lz.izmoqwy.core.api.image;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;

public interface ImageBuilder {

	BufferedImage getBackground();
	
	ImageBuilder text(String text, Font font, Color color, int tx, int ty);
	ImageBuilder image(BufferedImage subImage, int x, int y);
	ImageBuilder image(BufferedImage subImage, int x, int y, int width, int height);
	
	BufferedImage build();
	
}
