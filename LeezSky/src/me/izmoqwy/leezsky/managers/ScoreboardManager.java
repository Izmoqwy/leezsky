package me.izmoqwy.leezsky.managers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lz.izmoqwy.core.Economy;
import lz.izmoqwy.core.crosshooks.CrosshooksManager;
import lz.izmoqwy.core.crosshooks.interfaces.Group;
import lz.izmoqwy.core.crosshooks.interfaces.LeezPermissionsCH;
import lz.izmoqwy.core.helpers.PluginHelper;
import lz.izmoqwy.core.nms.NmsAPI;
import lz.izmoqwy.core.nms.scoreboard.NMSScoreboard;
import lz.izmoqwy.core.utils.TextUtil;
import me.izmoqwy.leezsky.LeezSky;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager implements SettingsManager.SettingUser {

	public static ScoreboardManager INSTANCE = new ScoreboardManager();

	private static Map<UUID, PlayerScoreboard> scoreboardMap = Maps.newHashMap();

	@Override
	public void onSettingUpdate(Player player, SettingsManager.Setting setting, Enum value) {
		if (setting == SettingsManager.SCOREBOARD) {
			if (value == SettingsManager.SimpleToggle.ON)
				createScoreboard(player);
			else
				destroyScoreboard(player);
		}
	}

	public static void load(LeezSky instance) {
		if (PluginHelper.isLoaded("LeezPermissions")) {
			PluginHelper.loadListener(instance, new Listener() {
				@EventHandler
				public void onGroupChange(me.izmoqwy.hardpermissions.events.PlayerGroupChangedEvent event) {
					if (event.getPlayer().isOnline()) {
						UUID uuid = event.getPlayer().getUniqueId();
						if (scoreboardMap.containsKey(uuid)) {
							scoreboardMap.get(uuid).updateGroup("§" + event.getGroup().getChatcolor() + event.getGroup().getName());
						}
					}
				}
			});
		}
	}

	public static void createScoreboard(Player player) {
		if (scoreboardMap.containsKey(player.getUniqueId()) || SettingsManager.SCOREBOARD.getState(player) == SettingsManager.SimpleToggle.OFF)
			return;

		NMSScoreboard scoreboard = NmsAPI.createScoreboard(player, "§6PLAY.LEEZSKY.FR");
		if (scoreboard != null) {
			scoreboard.create();
			scoreboardMap.put(player.getUniqueId(), new PlayerScoreboard(scoreboard));
		}
	}

	public static void refreshMoney(Player player) {
		if (scoreboardMap.containsKey(player.getUniqueId())) {
			scoreboardMap.get(player.getUniqueId()).refreshMoney();
		}
	}

	public static void destroyScoreboard(Player player) {
		UUID uuid = player.getUniqueId();
		if (scoreboardMap.containsKey(uuid)) {
			scoreboardMap.get(uuid).destroy();
			scoreboardMap.remove(uuid);
		}
	}

	public static void clear() {
		scoreboardMap.values().forEach(PlayerScoreboard::destroy);
		scoreboardMap.clear();
	}

	public static class PlayerScoreboard {
		private static final short playerLine = 1;

		private final NMSScoreboard scoreboard;
		private short rankLine, moneyLine;

		public PlayerScoreboard(NMSScoreboard scoreboard) {
			this.scoreboard = scoreboard;
			Player player = scoreboard.getPlayer();

			List<String> sbLines = Lists.newArrayList("§3Joueur: §b" + player.getName());

			if (CrosshooksManager.isPluginRegistred("LeezPermissions")) {
				LeezPermissionsCH permissions = CrosshooksManager.get("LeezPermissions", LeezPermissionsCH.class);
				if (permissions != null) {
					Group group = permissions.getGroup(player);
					if (group != null) {
						rankLine = (short) sbLines.size();
						sbLines.add(groupText(group.getChatColor() + group.getName()));
					}
				}
			}

			moneyLine = (short) sbLines.size();
			sbLines.add(moneyText(player));

			Collections.reverse(sbLines);
			for (int i = 0; i < sbLines.size(); i++) {
				scoreboard.setLine(i, sbLines.get(i));
			}
			short maxLine = (short) (sbLines.size() - 1);
			rankLine = fixLine(rankLine, maxLine);
			moneyLine = fixLine(moneyLine, maxLine);
		}

		private static short fixLine(short line, short max) {
			if (line == 0)
				return -1;

			short res = (short) (line - max);
			if (res < 0)
				return (short) -res;
			return res;
		}

		public void refreshMoney() {
			if (moneyLine == -1)
				return;
			scoreboard.setLine(moneyLine, moneyText(scoreboard.getPlayer()));
		}

		private static String moneyText(Player player) {
			return "§8➥ §3Monnaie: §e" + TextUtil.readbleNumber(Economy.getBalance(player)) + "$";
		}

		public void updateGroup(String groupFullName) {
			if (rankLine == -1)
				return;
			scoreboard.setLine(rankLine, groupText(groupFullName));
		}

		private static String groupText(String groupFullName) {
			return "§8➥ §3Grade: " + groupFullName;
		}

		public void destroy() {
			scoreboard.destroy();
		}
	}

}
