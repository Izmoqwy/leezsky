package lz.izmoqwy.shop;

import lombok.Getter;
import lz.izmoqwy.core.self.LeezCore;
import lz.izmoqwy.shop.commands.ShopCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class LeezShop extends JavaPlugin {

	public static final String PREFIX = LeezCore.PREFIX;

	@Getter
	private static LeezShop instance;

	@Override
	public void onEnable() {
		instance = this;

		ShopManager.get.load(this);

		getCommand("shop").setExecutor(new ShopCommand());
	}

}
