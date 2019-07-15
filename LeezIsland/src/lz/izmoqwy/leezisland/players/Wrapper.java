package lz.izmoqwy.leezisland.players;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lz.izmoqwy.core.api.database.exceptions.SQLActionImpossibleException;
import lz.izmoqwy.leezisland.Storage;
import lz.izmoqwy.leezisland.grid.*;
import lz.izmoqwy.leezisland.island.*;
import lz.izmoqwy.leezisland.utils.ParseUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import static lz.izmoqwy.leezisland.LeezIsland.log;
import static lz.izmoqwy.leezisland.Storage.ISLANDS;
import static lz.izmoqwy.leezisland.Storage.PLAYERS;

public class Wrapper {

	@Getter
	private static final Map<UUID, SkyblockPlayer> players = Maps.newHashMap();
	@Getter
	private static final Map<String, Island> islands = Maps.newHashMap();

	public static SkyblockPlayer wrapPlayer(Player player) {
		if (!players.containsKey(player.getUniqueId())) {
			try {
				players.put(player.getUniqueId(), loadPlayer(player));
			}
			catch (SQLActionImpossibleException e) {
				e.printStackTrace();
				System.err.println("[LeezIsland] Impossible de wrap un joueur à cause d'une erreur SQL (Regardez ci-dessus).");
				return null;
			}
		}
		return players.get(player.getUniqueId());
	}

	public static OfflineSkyblockPlayer wrapOffPlayer(OfflinePlayer player) {
		return new LeezOffIslandPlayer(player);
	}

	public static Island wrapIsland(String ID) throws SQLActionImpossibleException {
		if (ID == null || !ISLANDS.hasResult("toWrap", "island_id", ID)) {
			return null;
		}
		if (islands.containsKey(ID))
			return islands.get(ID);

		String leader, name, settings, toWrap, members_toWrap;
		int level;
		try {
			PreparedStatement statement = Storage.DB.prepare("SELECT `leader`, `name`, `level`, `settings`, `toWrap`, `members_toWrap` FROM Islands WHERE `island_id` = ?");
			statement.setString(1, ID);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				leader = rs.getString("leader");
				name = rs.getString("name");
				settings = rs.getString("settings");
				toWrap = rs.getString("toWrap");
				members_toWrap = rs.getString("members_toWrap");

				level = rs.getInt("level");

				statement.close();
			}
			else {
				statement.close();
				return null;
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

		String[] content = toWrap.split(Pattern.quote("|"));
		List<IslandMember> memberList = Lists.newArrayList();
		List<UUID> bannedList = Lists.newArrayList();
		if (!members_toWrap.isEmpty()) {
			String[] parts = members_toWrap.replaceAll(" ", "").split("\\+");
			if (parts.length >= 1) {
				String[] members = parts[0].split(Pattern.quote(";"));
				if (members.length > 0) {
					for (String member : members) {
						String[] memberParts = member.split(Pattern.quote("|"));
						String uuid = memberParts[0];
						if (uuid.isEmpty())
							continue;
						if (PLAYERS.getString("island_id", "player_id", uuid).equals(ID))
							memberList.add(new IslandMember(UUID.fromString(uuid), IslandRole.fromID(Integer.parseInt(memberParts[1]))));
						else {
							log.warning("Warn when loading island #" + ID + " because " + uuid + " is counted in but in his datas, he's either in another island or he hasn't island.");
						}
					}
				}
			}

			if (parts.length >= 2) {
				String[] banneds = parts[1].split(Pattern.quote(";"));
				if (banneds.length > 0) {
					for (String banned : banneds) {
						bannedList.add(UUID.fromString(banned));
					}
				}
			}
		}
		List<VisitorPermission> visitorsPermissions = Lists.newArrayList();
		List<GeneralPermission> generalPermissions = Lists.newArrayList();
		List<CoopPermission> coopPermissions = Lists.newArrayList();
		if (settings != null && !settings.isEmpty()) {
			String[] perms2parse = settings.split(Pattern.quote("|"));
			if (perms2parse.length >= 1) {
				String tp = perms2parse[0];
				for (VisitorPermission all : VisitorPermission.values()) {
					if (tp.contains(all.val + ""))
						visitorsPermissions.add(all);
				}
			}
			if (perms2parse.length >= 2) {
				String tp = perms2parse[1];
				for (GeneralPermission all : GeneralPermission.values()) {
					if (tp.contains(all.val + ""))
						generalPermissions.add(all);
				}
			}
			if (perms2parse.length >= 3) {
				String tp = perms2parse[2];
				for (CoopPermission all : CoopPermission.values()) {
					if (tp.contains(all.val + ""))
						coopPermissions.add(all);
				}
			}
		}
		Island island = new Island(ID,
				leader, name, level,
				ParseUtil.str2locNW(content[0], GridManager.getWorld().getName()),
				Double.parseDouble(content[1]), Double.parseDouble(content[2]), Integer.parseInt(content[3]),
				Boolean.parseBoolean(content[4]), memberList, bannedList,
				visitorsPermissions, generalPermissions, coopPermissions);

		islands.put(ID, island);
		return island;
	}

	private static LeezIslandPlayer loadPlayer(Player player) throws SQLActionImpossibleException {
		return new LeezIslandPlayer(player, wrapIsland(PLAYERS.getString("island_id", "player_id", player.getUniqueId().toString(), null)));
	}

	public static Island wrapOffPlayerIsland(OfflinePlayer player) {
		try {
			return wrapIsland(PLAYERS.getString("island_id", "player_id", player.getUniqueId().toString(), null));
		}
		catch (SQLActionImpossibleException e) {
			e.printStackTrace();
		}
		return null;
	}

}
