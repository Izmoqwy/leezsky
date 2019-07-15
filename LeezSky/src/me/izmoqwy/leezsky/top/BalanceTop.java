package me.izmoqwy.leezsky.top;

import lz.izmoqwy.core.Economy;
import lz.izmoqwy.core.top.Top;
import lz.izmoqwy.core.top.TopResult;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.*;

public class BalanceTop implements Top {

	private final BalanceTopComparator comparator = new BalanceTopComparator();

	@Override
	public TopResult[] calcTop() {
		TopResult[] results = new TopResult[10];

		OfflinePlayer[] players = Bukkit.getOfflinePlayers();
		Arrays.sort(players, comparator);

		for (int i = 0; i < 10; i++) {
			if (i == players.length)
				break;

			OfflinePlayer player = players[i];
			results[i] = new BalanceTopResult(player, Economy.getBalance(player));
		}

		return results;
	}

	@Override
	public String getTopName() {
		return "Joueurs les plus riches";
	}

	@Override
	public String getCommandName() {
		return "balancetop";
	}

	class BalanceTopResult implements TopResult {

		private OfflinePlayer player;
		private double solde;

		BalanceTopResult(OfflinePlayer player, double solde) {
			this.player = player;
			this.solde = solde;
		}

		@Override
		public OfflinePlayer getPlayer() {
			return this.player;
		}

		@Override
		public String getItemName() {
			return "§e" + player.getName();
		}

		@Override
		public List<String> getItemDescription() {
			if (player.isOp())
				return Collections.singletonList("§cCe joueur ne compte pas dans ce top.");
			return Collections.singletonList("§6Solde: §e" + solde);
		}

	}

	class BalanceTopComparator implements Comparator<OfflinePlayer> {

		public int compare(OfflinePlayer a, OfflinePlayer b) {
			double bal1 = a.isOp() ? 0.00 : Economy.getBalance(a), bal2 = b.isOp() ? 0.00 : Economy.getBalance(b);
			return Double.compare(bal1, bal2);
		}

	}

}
