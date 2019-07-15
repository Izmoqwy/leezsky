package lz.izmoqwy.core.api;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.collect.Maps;

public class MapsAPI {

	private static File saveFile;
	public static Map<Integer, String> maps = Maps.newHashMap();

	public static void load(File saveFile) {
		MapsAPI.saveFile = saveFile;
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(saveFile);
		for (String key : yaml.getKeys(false)) {
			maps.put(Integer.parseInt(key), yaml.getString(key + ".data", ""));
		}
	}

	public static void save() {
		if (saveFile == null)
			return;

		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(saveFile);
		for (Entry<Integer, String> map : maps.entrySet()) {
			yaml.set(map.getKey() + "", map.getValue());
		}

		try {
			yaml.save(saveFile);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Deprecated
	public static BufferedImage makeImage(BufferedImage image, String text, Font font, Color fontColor, Color strokeColor, int strokeSize, int textX, int textY) {
		BufferedImage nImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = nImage.createGraphics();
		try {
			graphics.setColor(fontColor);
			graphics.setFont(font);

			graphics.drawString(text, textX, textY);
			if (strokeSize > 0) {
				graphics.setColor(strokeColor);
				graphics.setStroke(new BasicStroke(strokeSize));
				AffineTransform var = new AffineTransform();
				var.translate(textX, textY);
				graphics.draw(new TextLayout(text, font, new FontRenderContext(null, false, false)).getOutline(var));
			}
			return nImage;
		}
		finally {
			graphics.dispose();
		}
	}

}
