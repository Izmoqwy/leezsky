package lz.izmoqwy.core;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Economy
{
    private static net.milkbowl.vault.economy.Economy economy = null;

    static {
        RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
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

    public static double round(double balance) {
        return round("" + balance);
    }

    public static double round(String balance) {
        return Math.floor(Double.parseDouble(balance) * 100) / 100;
    }
}
