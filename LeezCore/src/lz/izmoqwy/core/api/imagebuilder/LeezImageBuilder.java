package lz.izmoqwy.core.api.imagebuilder;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import com.google.common.collect.Lists;

public class LeezImageBuilder implements ImageBuilder {

	private BufferedImage base;
	private List<ImageText> texts;
	private List<ImageSub> subimages;

	public LeezImageBuilder(BufferedImage image) {
		this.base = image;
		this.texts = Lists.newArrayList();
		this.subimages = Lists.newArrayList();
	}

	private class ImageText {
		public String text;
		public Font font;
		public Color color;
		public int tx, ty;

		public ImageText(String text, Font font, Color color, int tx, int ty) {
			this.text = text;
			this.font = font;
			this.color = color;
			this.tx = tx;
			this.ty = ty;
		}
	}

	private class ImageSub {
		public BufferedImage image;
		public int x, y, width, height;

		public ImageSub(BufferedImage image, int x, int y, int width, int height) {
			this.image = image;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}
	}

	@Override
	public BufferedImage getBackground() {
		return base;
	}

	@Override
	public ImageBuilder text(String text, Font font, Color color, int tx, int ty) {
		texts.add(new ImageText(text, font, color, tx, ty));
		return this;
	}

	@Override
	public ImageBuilder image(BufferedImage subimage, int x, int y) {
		subimages.add(new ImageSub(subimage, x, y, subimage.getWidth(), subimage.getHeight()));
		return this;
	}

	@Override
	public ImageBuilder image(BufferedImage subimage, int x, int y, int width, int height) {
		subimages.add(new ImageSub(subimage, x, y, width, height));
		return this;
	}

	@Override
	public BufferedImage build() {
		BufferedImage nImage = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = nImage.createGraphics();
		try {
			graphics.drawImage(base, 0, 0, base.getWidth(), base.getHeight(), null);

			for (ImageText text : texts) {
				graphics.setColor(text.color);
				graphics.setFont(text.font);

				graphics.drawString(text.text, text.tx, text.ty);
//				if(strokeSize > 0)
//				{
//					graphics.setColor(strokeColor);
//					graphics.setStroke(new BasicStroke(strokeSize));
//					AffineTransform var = new AffineTransform();
//					var.translate(textX, textY);
//					graphics.draw(new TextLayout(text, font, new FontRenderContext(null, false, false)).getOutline(var));
//				}
			}

			for (ImageSub sub : subimages) {
				graphics.drawImage(sub.image, sub.x, sub.y, sub.width, sub.height, null);
			}

			return nImage;
		}
		finally {
			graphics.dispose();
		}
	}

}
