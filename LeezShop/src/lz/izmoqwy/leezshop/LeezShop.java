package lz.izmoqwy.leezshop;

import com.google.common.collect.Maps;
import lz.izmoqwy.core.api.MapsAPI;
import lz.izmoqwy.core.api.imagebuilder.LeezImageBuilder;
import lz.izmoqwy.leezshop.commands.ShopCommand;
import lz.izmoqwy.leezshop.listeners.GUIListener;
import lz.izmoqwy.leezshop.pictures.FontRenderer;
import lz.izmoqwy.leezshop.playershop.PremiumShop;
import lz.izmoqwy.leezshop.playershop.PremiumShopMapRender;
import org.bukkit.Bukkit;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

public class LeezShop extends JavaPlugin {
	
	public static final String PREFIX = "§8» ";

	private static File PICTS_FOLDER, MAPS_FILE;
	private static LeezShop instance;

	private static final String DEF_FONT = "Lucida Sans Typewriter";
	public static final Map<Short, PremiumShop> MAPS = Maps.newHashMap();
	
	@SuppressWarnings("deprecation")
	@Override
	public void onEnable()
	{
		
		instance = this;
		
		saveDefaultConfig();
		ShopLoader.load();
		
		try
		{
			PICTS_FOLDER = new File(getDataFolder(), "playershop/images");
			if (!PICTS_FOLDER.exists()) PICTS_FOLDER.mkdirs();

			MAPS_FILE = new File(getDataFolder(), "playershop/maps.yml");
			if(!MAPS_FILE.exists()) MAPS_FILE.createNewFile();
		}
		catch(IOException ex) { ex.printStackTrace(); }
		
		getCommand("shop").setExecutor(new ShopCommand());
		Bukkit.getPluginManager().registerEvents(new GUIListener(), this);
		
		MapsAPI.load(MAPS_FILE);
		PremiumShopMapRender prenderer = new PremiumShopMapRender();
		BufferedImage defaultBg = null;
		Font font = new Font(DEF_FONT, Font.BOLD, 16);
		Color color = new Color(255, 255, 255);
		try {
			
			defaultBg = ImageIO.read(new File(PICTS_FOLDER, "shop-default.png"));
			
		} catch (IOException e) { e.printStackTrace(); }
		File ITEMS_FOLDER = new File(PICTS_FOLDER, "items/");
		for(Entry<Integer, String> map : MapsAPI.maps.entrySet())
		{
			if (map == null)
				continue;
			if (map.getValue().length() <= 0)
				continue;
			try {
				
				MapView view = Bukkit.getMap(map.getKey().shortValue());
				if (view == null)
					continue;
				
				for(MapRenderer renderer : view.getRenderers())
					view.removeRenderer(renderer);
				view.addRenderer(prenderer);
				
				String text = "Melon",
						textPrice = "Prix à l'unité:",
						price = "3$";
				int startX = (128-FontRenderer.getWidth(font, text)) / 2;
				BufferedImage image = new LeezImageBuilder(defaultBg)
						.text(text, font, color, startX, 20)
						.text("Acheter", font.deriveFont(14F), Color.LIGHT_GRAY, 5, 60)
						.image(ImageIO.read(new File(ITEMS_FOLDER, "melon.png")), 64, 28, 48, 48)
						.text(textPrice, font.deriveFont(12F), color, (128-FontRenderer.getWidth(font.deriveFont(12F), textPrice)) / 2, 96)
						.text(price, font, Color.YELLOW, (128-FontRenderer.getWidth(font, price)) / 2, 114)
						.build();
				MAPS.put(view.getId(), new PremiumShop(image));
				
			} catch (IOException e) { e.printStackTrace(); }
		}
		
	}
	
	static LeezShop getInstance()
	{
		return instance;
	}
	
	@Override
	public void onDisable()
	{
		MapsAPI.save();
	}

}
