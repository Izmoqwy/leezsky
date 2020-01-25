package lz.izmoqwy.island.island;

import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lz.izmoqwy.core.api.database.exceptions.SQLActionImpossibleException;
import lz.izmoqwy.core.self.CorePrinter;
import lz.izmoqwy.core.utils.LocationUtil;
import lz.izmoqwy.island.LeezIsland;
import lz.izmoqwy.island.Storage;
import lz.izmoqwy.island.grid.CoopsManager;
import lz.izmoqwy.island.grid.GridManager;
import lz.izmoqwy.island.island.permissions.CoopPermission;
import lz.izmoqwy.island.island.permissions.GeneralPermission;
import lz.izmoqwy.island.island.permissions.VisitorPermission;
import lz.izmoqwy.island.players.SkyblockPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Getter
public class Island {

	@Getter(AccessLevel.NONE)
	public String ID;

	private UUID ownerId;
	private int middleX, middleZ;

	@Setter
	private String name;
	@Setter
	private int level;

	// Maximum avec les paramètres actuels : 125 (Espace de 300 si deux îles "proches" font 250 chacune).
	private short range;
	private boolean locked;
	private Location homeLocation;

	private Map<UUID, IslandMember> membersMap;
	private List<UUID> banList;

	private List<VisitorPermission> visitorsPermissions;
	private List<GeneralPermission> generalPermissions;
	private List<CoopPermission> coopPermissions;

	public Island(String ID, UUID ownerId, String name, int level, Location homeLocation, int middleX, int middleZ, short range, boolean locked,
				  List<IslandMember> members, List<UUID> banList,
				  List<VisitorPermission> visitorsPermissions, List<GeneralPermission> generalPermissions, List<CoopPermission> coopPermissions) {
		this.ID = ID;
		this.ownerId = ownerId;
		this.name = name;
		this.level = level;
		this.homeLocation = homeLocation;

		this.middleX = middleX;
		this.middleZ = middleZ;
		this.range = range;
		calcBounds();

		this.locked = locked;

		Map<UUID, IslandMember> membersMap = Maps.newHashMap();
		members.forEach(member -> membersMap.put(member.getPlayerId(), member));
		this.membersMap = membersMap;
		this.banList = banList;

		this.visitorsPermissions = visitorsPermissions;
		this.generalPermissions = generalPermissions;
		this.coopPermissions = coopPermissions;
	}

	/*
	Bounds
	 */
	private int minX, maxX;
	private int minZ, maxZ;

	private void calcBounds() {
		this.minX = this.middleX - this.range;
		this.minZ = this.middleZ - this.range;
		this.maxX = this.middleX + this.range;
		this.maxZ = this.middleZ + this.range;
	}

	public Location getLowerNE() {
		return new Location(GridManager.getWorld(), minX, 0, minZ);
	}

	public Location getUpperSW() {
		return new Location(GridManager.getWorld(), maxX, GridManager.getWorld().getMaxHeight(), maxZ);
	}

	public void setRange(short range) {
		this.range = range;
		calcBounds();
		saveGeneral();
	}

	public boolean isInBounds(int x, int z) {
		return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
	}

	public boolean isInBounds(Location location) {
		return isInBounds(location.getBlockX(), location.getBlockZ());
	}

	/*
	Other
	 */
	public String getDisplayName() {
		if (name == null)
			return "de " + getOwner().getName();
		return name;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
		saveGeneral();
	}

	public void setHomeLocation(Location location) {
		this.homeLocation = location;
		saveGeneral();
	}

	/*
	Members
	 */
	public boolean hasAccess(OfflinePlayer player) {
		return hasFullAccess(player) || isCooped(player);
	}

	/**
	 * @param player The player to test
	 * @return if the player is in this island (Full access means not a visitor nor a coop)
	 */
	public boolean hasFullAccess(OfflinePlayer player) {
		return isOwner(player.getUniqueId()) || membersMap.containsKey(player.getUniqueId());
	}

	private boolean hasRoleOrAbove(IslandMember member, IslandRole role) {
		if (member == null)
			return false;
		return isOwner(member.getPlayerId()) || member.getRole().ordinal() >= role.ordinal();
	}

	public boolean hasRoleOrAbove(SkyblockPlayer player, IslandRole role) {
		if (isOwner(player.getBaseId()))
			return true;
		return hasRoleOrAbove(membersMap.get(player.getBaseId()), role);
	}

	public boolean hasRoleOrAbove(OfflinePlayer player, IslandRole role) {
		if (isOwner(player.getUniqueId()))
			return true;
		return hasRoleOrAbove(membersMap.get(player.getUniqueId()), role);
	}

	public OfflinePlayer getOwner() {
		return Bukkit.getOfflinePlayer(ownerId);
	}

	public boolean isOwner(UUID playerId) {
		return ownerId.equals(playerId);
	}

	public boolean isOwner(SkyblockPlayer player) {
		return ownerId.equals(player.getBaseId());
	}

	public IslandRole getRole(UUID playerId) {
		if (isOwner(playerId))
			return IslandRole.OWNER;

		return membersMap.get(playerId).getRole();
	}

	public IslandRole getRole(SkyblockPlayer player) {
		return getRole(player.getBaseId());
	}

	public void setRole(IslandMember member, IslandRole role) {
		if (member == null)
			return;

		membersMap.put(member.getPlayerId(), new IslandMember(member.getPlayerId(), role));
		saveMembers();
	}

	public void setRole(OfflinePlayer player, IslandRole role) {
		if (isOwner(player.getUniqueId())) {
			CorePrinter.warn("Cannot change IslandRole of owner --> Is this a bug?");
			return;
		}

		setRole(membersMap.get(player.getUniqueId()), role);
	}

	public boolean hasVisitorPermission(VisitorPermission permission) {
		return visitorsPermissions.contains(permission);
	}

	public boolean hasGeneralPermission(GeneralPermission permission) {
		return generalPermissions.contains(permission);
	}

	public boolean hasCoopPermission(CoopPermission permission) {
		return coopPermissions.contains(permission);
	}

	public boolean isCooped(OfflinePlayer player) {
		return CoopsManager.manager.isCooped(player.getUniqueId(), this);
	}

	public List<UUID> getCoops() {
		return CoopsManager.manager.getCoops(this);
	}

	public boolean isBanned(OfflinePlayer player) {
		return banList.contains(player.getUniqueId());
	}

	public boolean banPlayer(OfflinePlayer player) {
		UUID playerId = player.getUniqueId();
		if (banList.contains(player.getUniqueId()))
			return false;

		if (isCooped(player))
			CoopsManager.manager.unCoop(playerId, this, true);

		banList.add(playerId);
		saveMembers();
		return true;

		// todo: notify island's members
	}

	public void pardonPlayer(OfflinePlayer player) {
		banList.remove(player.getUniqueId());
		saveMembers();
	}

	public void broadcast(String message) {
		Stream.concat(membersMap.values().stream(), Stream.of(new IslandMember(ownerId, IslandRole.OWNER))).forEach(member -> {
			Player onlineMember = Bukkit.getPlayer(member.getPlayerId());
			if (onlineMember != null)
				onlineMember.sendMessage(message);
		});
	}

	public void sendToTeam(SkyblockPlayer sender, String message) {
		if (sender.getIsland() != this)
			return;

		IslandRole senderRole = getRole(sender);
		String displayName = ChatColor.COLOR_CHAR + senderRole.getColorChat() + "(" + senderRole.toString() + ") " + sender.bukkit().getName(),
				coloredMessage = senderRole.ordinal() >= IslandRole.OFFICIER.ordinal() ? message.replaceAll("&([\\da-f])", "§$1") : message;
		if (!coloredMessage.startsWith(Character.toString(ChatColor.COLOR_CHAR)))
			coloredMessage = "§d" + coloredMessage;

		broadcast(ChatColor.DARK_PURPLE + "[Team] " + displayName + " §8➟ " + coloredMessage);
		LeezIsland.logger.info("[Team] " + sender.bukkit().getName() + ": " + message);
	}

	/*
	Serializing / saving
	 */
	public String serializeData() {
		return LocationUtil.inlineSerialize(homeLocation, false, true) + "|" + middleX + "|" + middleZ + "|" + range + "|" + locked;
	}

	public String serializeMembers() {
		StringBuilder stringBuilder = new StringBuilder();

		membersMap.values().forEach(member -> stringBuilder.append(member.getPlayerId().toString()).append("|").append(member.getRole().getId()).append(";"));
		if (!banList.isEmpty()) {
			stringBuilder.append("+");
			banList.forEach(banned -> stringBuilder.append(banned.toString()).append(";"));
		}

		return stringBuilder.toString();
	}

	public String serializePermissions() {
		StringBuilder stringBuilder = new StringBuilder();

		visitorsPermissions.forEach(permission -> stringBuilder.append(permission.getIdentifier()));
		stringBuilder.append("|");
		generalPermissions.forEach(permission -> stringBuilder.append(permission.getIdentifier()));
		stringBuilder.append("|");
		coopPermissions.forEach(permission -> stringBuilder.append(permission.getIdentifier()));

		return stringBuilder.toString();
	}

	public void saveGeneral() {
		try {
			Storage.ISLANDS.setString("general", serializeData(), "island_id", ID);
		}
		catch (SQLActionImpossibleException e) {
			e.printStackTrace();
		}
	}

	public void saveMembers() {
		try {
			Storage.ISLANDS.setString("members", serializeMembers(), "island_id", ID);
		}
		catch (SQLActionImpossibleException e) {
			e.printStackTrace();
		}
	}

	public void savePermissions() {
		try {
			Storage.ISLANDS.setString("settings", serializePermissions(), "island_id", ID);
		}
		catch (SQLActionImpossibleException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Island)) return false;
		Island island = (Island) o;
		return middleX == island.middleX &&
				middleZ == island.middleZ &&
				range == island.range &&
				ID.equals(island.ID);
	}

	@Override
	public int hashCode() {
		return Objects.hash(ID, middleX, middleZ, range);
	}

}
