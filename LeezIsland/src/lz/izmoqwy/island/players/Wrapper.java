package lz.izmoqwy.island.players;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lz.izmoqwy.core.api.database.exceptions.SQLActionImpossibleException;
import lz.izmoqwy.core.utils.LocationUtil;
import lz.izmoqwy.island.Storage;
import lz.izmoqwy.island.grid.*;
import lz.izmoqwy.island.island.*;
import lz.izmoqwy.island.island.permissions.CoopPermission;
import lz.izmoqwy.island.island.permissions.GeneralPermission;
import lz.izmoqwy.island.island.permissions.VisitorPermission;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static lz.izmoqwy.island.LeezIsland.logger;
import static lz.izmoqwy.island.Storage.ISLANDS;
import static lz.izmoqwy.island.Storage.PLAYERS;

public class Wrapper {

	@Getter
	private static final Map<UUID, SkyblockPlayer> players = Maps.newHashMap();
	@Getter
	private static final Map<String, Island> islands = Maps.newHashMap();

	public static SkyblockPlayer wrapPlayer(Player player) {
		UUID playerId = player.getUniqueId();
		if (!players.containsKey(playerId)) {
			try {
				players.put(playerId, loadPlayer(player));
			}
			catch (SQLActionImpossibleException e) {
				e.printStackTrace();
				System.err.println("[LeezIsland] Impossible de wrap un joueur à cause d'une erreur SQL (Regardez ci-dessus).");
				return null;
			}
		}

		return players.get(playerId);
	}

	public static Island wrapIsland(String ID) throws SQLActionImpossibleException {
		if (ID == null || !ISLANDS.hasResult("toWrap", "island_id", ID)) {
			return null;
		}
		if (islands.containsKey(ID))
			return islands.get(ID);

		String leader, name, settings, general, members;
		int level;

		try {
			PreparedStatement preparedStatement = Storage.DB.prepare("SELECT `leader`, `name`, `level`, `settings`, `general`, `members` FROM Islands WHERE `island_id` = ?");
			preparedStatement.setString(1, ID);
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				leader = resultSet.getString("leader");
				name = resultSet.getString("name");
				settings = resultSet.getString("settings");
				general = resultSet.getString("general");
				members = resultSet.getString("members");
				level = resultSet.getInt("level");

				preparedStatement.close();
			}
			else {
				preparedStatement.close();
				return null;
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

		List<IslandMember> memberList = Lists.newArrayList();
		List<UUID> banList = Lists.newArrayList();

		if (!members.isEmpty()) {
			String[] parts = members.replaceAll(" ", "").split("\\+");

			if (parts.length >= 1) {
				String[] _members = parts[0].split(Pattern.quote(";"));
				for (String member : _members) {
					String[] memberParts = member.split(Pattern.quote("|"));

					String uuid = memberParts[0];
					if (uuid.isEmpty())
						continue;

					if (PLAYERS.getString("island_id", "player_id", uuid).equals(ID))
						memberList.add(new IslandMember(UUID.fromString(uuid), IslandRole.fromID(Integer.parseInt(memberParts[1]))));
					else {
						logger.warning("Warn when loading island #" + ID + " because " + uuid + " is counted in but in his datas, he's either in another island or he hasn't island.");
					}
				}
			}
			if (parts.length >= 2) {
				String[] _banned = parts[1].split(Pattern.quote(";"));
				for (String banned : _banned) {
					banList.add(UUID.fromString(banned));
				}
			}
		}

		List<VisitorPermission> visitorsPermissions = Lists.newArrayList();
		List<GeneralPermission> generalPermissions = Lists.newArrayList();
		List<CoopPermission> coopPermissions = Lists.newArrayList();

		if (settings != null && !settings.isEmpty()) {
			String[] _permissions = settings.split(Pattern.quote("|"));

			final String _visitors = _permissions[0], _general = _permissions[1], _coops = _permissions[2];
			visitorsPermissions =
					Arrays.stream(VisitorPermission.values()).filter(permission -> _visitors.contains(Character.toString(permission.getIdentifier()))).collect(Collectors.toList());
			generalPermissions =
					Arrays.stream(GeneralPermission.values()).filter(permission -> _general.contains(Character.toString(permission.getIdentifier()))).collect(Collectors.toList());
			coopPermissions =
					Arrays.stream(CoopPermission.values()).filter(permission -> _coops.contains(Character.toString(permission.getIdentifier()))).collect(Collectors.toList());
		}

		String[] content = general.split(Pattern.quote("|"));
		Island island = new Island(ID,
				UUID.fromString(leader), name, level,
				LocationUtil.inlineParse(content[0], GridManager.getWorld()),
				Integer.parseInt(content[1]), Integer.parseInt(content[2]), Short.parseShort(content[3]),
				Boolean.parseBoolean(content[4]), memberList, banList,
				visitorsPermissions, generalPermissions, coopPermissions);

		islands.put(ID, island);
		return island;
	}

	private static LeezSkyblockPlayer loadPlayer(Player player) throws SQLActionImpossibleException {
		String islandId, personalHome;
		long lastRestart;

		try {
			PreparedStatement preparedStatement = Storage.DB.prepare("SELECT `island_id`, `personalHome`, `lastRestart` FROM Players WHERE `player_id` = ?");
			preparedStatement.setString(1, player.getUniqueId().toString());

			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				islandId = resultSet.getString("island_id");
				personalHome = resultSet.getString("personalHome");
				lastRestart = resultSet.getLong("lastRestart");

				preparedStatement.close();
			}
			else {
				preparedStatement.close();
				return new LeezSkyblockPlayer(player, null, null, -1);
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

		return new LeezSkyblockPlayer(
				player, wrapIsland(islandId),
				LocationUtil.inlineParse(personalHome, GridManager.getWorld()), lastRestart
		);
	}

	public static OfflineSkyblockPlayer getOfflinePlayer(OfflinePlayer player) {
		return new LeezOfflineSkyblockPlayer(player);
	}

	public static Island getOfflinePlayerIsland(OfflinePlayer player) {
		try {
			return wrapIsland(PLAYERS.getString("island_id", "player_id", player.getUniqueId().toString(), null));
		}
		catch (SQLActionImpossibleException e) {
			e.printStackTrace();
		}
		return null;
	}

}
