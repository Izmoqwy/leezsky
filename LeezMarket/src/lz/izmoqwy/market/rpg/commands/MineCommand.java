package lz.izmoqwy.market.rpg.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lz.izmoqwy.core.self.CorePrinter;
import lz.izmoqwy.core.command.CommandOptions;
import lz.izmoqwy.core.utils.TextUtil;
import lz.izmoqwy.market.Locale;
import lz.izmoqwy.market.rpg.RPGCommand;
import lz.izmoqwy.market.rpg.RPGPlayer;
import lz.izmoqwy.market.rpg.RPGResource;
import lz.izmoqwy.market.rpg.RPGStorage;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MineCommand extends RPGCommand {

	public MineCommand(String commandName) {
		super(commandName, CommandOptions.builder()
				.playerOnly(true)
				.build(), true);
	}

	@Override
	protected void execute(RPGPlayer player, String usedCommand, String[] args) {
		int critial = 100, mana = player.getEnergy(), used = 0;

		Random random = new Random();
		int dm = 0, ur = 0, tt = 0, cp = 0;
		for (int i = 0; i < mana; i++) {
			switch (chooseResource(new Random().nextInt(49) + 1, 50)) {
				case DARKMATTER:
					dm += Math.floor((random.nextInt(getPercentage(5, critial, 15) / 2) + 1));
					break;
				case URANIUM:
					ur += Math.floor((random.nextInt(getPercentage(40, critial) / 2) + 1) + critial / 2);
					break;
				case TITANE:
					tt += Math.floor((random.nextInt(getPercentage(60, critial) / 2) + 1) + critial / 2);
					break;
				case COPPER:
					cp += Math.floor((random.nextInt(getPercentage(80, critial) / 2) + 1) + critial / 2);
					break;
				case BZZODARK:
					CorePrinter.warn("Invalid resource picked up by random function on RPG mine!");
					break;
			}
			used++;
		}
		List<Map.Entry<RPGResource, Integer>> ress = Lists.newArrayList();
		List<String> toSet = Lists.newArrayList();
		if (dm > 0) {
			ress.add(Maps.immutableEntry(RPGResource.DARKMATTER, dm));
			toSet.add("`res_darkmatter`=`res_darkmatter` + " + dm);
		}
		if (ur > 0) {
			ress.add(Maps.immutableEntry(RPGResource.URANIUM, ur));
			toSet.add("`res_uranium`=`res_uranium` + " + ur);
		}
		if (tt > 0) {
			ress.add(Maps.immutableEntry(RPGResource.TITANE, tt));
			toSet.add("`res_titane`=`res_titane` + " + tt);
		}
		if (cp > 0) {
			ress.add(Maps.immutableEntry(RPGResource.COPPER, cp));
			toSet.add("`res_copper`=`res_copper` + " + cp);
		}

		if (toSet.isEmpty()) {
			player.sendMessage(Locale.PREFIX + "§CLa commande a eu un comportement innatendu, pour ne pas vous pénaliser, son éxécution a été annulée.");
			return;
		}

		int exp = dm * 3 + ur * 2 + tt + cp;
		int xp = 0, needed = 1;
		while (exp >= needed) {
			xp += 1;
			needed += xp % 2 == 0 ? xp * 2 : xp;
		}

		try {
			PreparedStatement statement = RPGStorage.DB.prepare("UPDATE " + RPGStorage.PLAYERS + " SET " + String.join(", ", toSet) + ", energy=energy - ?, exp=exp + ? WHERE uuid = ?");
			statement.setInt(1, used);
			statement.setInt(2, exp);
			statement.setString(3, player.getBase().getUniqueId().toString());
			statement.executeUpdate();
			statement.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
			player.sendMessage(Locale.PREFIX + "§4Une erreur est survenue ! §cImpossible de miner dans ces conditions.");
			return;
		}
		player.sendMessage(Locale.RPG_PREFIX + "§3Vous avez miné " +
				TextUtil.iterate(ress, entry -> entry.getKey().getFullName(readbleNumber(entry.getValue())), "", "§3, ", " §3et ") +
				" §3en utilisant §e" + used + "⚡§3. §6(§e+" + readbleNumber(exp) + "xp§6)");
		calcLevelUp(player, exp);
	}

	private static int getPercentage(int def, int critial) {
		return (int) Math.floor(critial * ((def + new Random().nextInt(100 - def + 1)) / 100.D));
	}

	private static int getPercentage(int def, int critial, int max) {
		int val = (int) Math.floor(critial * ((def + new Random().nextInt(100 - def + 1)) / 100.D));
		if (val > max)
			return max;
		return val;
	}

	@SuppressWarnings("SameParameterValue")
	private static RPGResource chooseResource(int rdm, int max) {
		double random = rdm / 10.D, partition = max / 1000.D;
		if (random <= partition * 5) {
			return RPGResource.DARKMATTER;
		}
		else if (random <= partition * 25) {
			return RPGResource.URANIUM;
		}
		else if (random <= partition * 70) {
			return RPGResource.COPPER;
		}
		else if (random <= partition * 100) {
			return RPGResource.TITANE;
		}
		else
			// This should NEVER be returned
			return RPGResource.BZZODARK;
	}

}
