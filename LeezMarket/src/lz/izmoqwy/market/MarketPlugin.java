package lz.izmoqwy.market;

import lombok.Getter;
import lz.izmoqwy.core.i18n.LocaleManager;
import lz.izmoqwy.market.blackmarket.BlackMarket;
import org.bukkit.plugin.java.JavaPlugin;

public class MarketPlugin extends JavaPlugin {

	@Getter
	private static MarketPlugin instance;

	@Override
	public void onEnable() {
		instance = this;

		BlackMarket.loadAll();

		LocaleManager.register(this, Locale.class);
	}

	@Override
	public void onDisable() {
		BlackMarket.unload();
	}

}
