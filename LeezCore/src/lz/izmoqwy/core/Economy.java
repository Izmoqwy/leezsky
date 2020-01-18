package lz.izmoqwy.core;

import lz.izmoqwy.core.utils.MathUtil;
import lz.izmoqwy.core.utils.TextUtil;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

public class Economy {

	private static net.milkbowl.vault.economy.Economy economy = null;

	private static NumberFormat PRETTY_FORMAT = NumberFormat.getInstance(Locale.US);

	static {
		RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}

		PRETTY_FORMAT.setRoundingMode(RoundingMode.FLOOR);
		PRETTY_FORMAT.setGroupingUsed(true);
		PRETTY_FORMAT.setMinimumFractionDigits(2);
		PRETTY_FORMAT.setMaximumFractionDigits(2);
	}

	public static boolean withdraw(OfflinePlayer player, double amount) {
		return economy.withdrawPlayer(player, amount).type == EconomyResponse.ResponseType.SUCCESS;
	}

	public static boolean deposit(OfflinePlayer player, double amount) {
		return economy.depositPlayer(player, amount).type == EconomyResponse.ResponseType.SUCCESS;
	}

	public static double getBalance(OfflinePlayer player) {
		return economy.getBalance(player);
	}

	/**
	 * @deprecated use {@link TextUtil#humanReadableNumber(double)}
	 */
	@Deprecated
	public static double round(double balance) {
		return MathUtil.roundDecimal(balance, 2);
	}

}
