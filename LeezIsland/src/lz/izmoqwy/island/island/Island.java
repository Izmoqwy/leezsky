package lz.izmoqwy.island.island;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import lz.izmoqwy.core.self.CorePrinter;
import lz.izmoqwy.core.api.database.exceptions.SQLActionImpossibleException;
import lz.izmoqwy.core.utils.LocationUtil;
import lz.izmoqwy.island.Storage;
import lz.izmoqwy.island.grid.*;
import lz.izmoqwy.island.island.permissions.CoopPermission;
import lz.izmoqwy.island.island.permissions.GeneralPermission;
import lz.izmoqwy.island.island.permissions.VisitorPermission;
import lz.izmoqwy.island.players.SkyblockPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Island {

	public String ID;
	private String owner;
	private int midX, midZ;

	@Getter @Setter
	private String name;
	@Getter @Setter
	private int level;

	/*
	 * Maximum avec les paramètres actuels, 125 (Espace de 300 si deux îles "proches" font 250 chacune).
	 */
	@Getter
	private short range;
	@Getter
	private boolean locked;
	@Getter
	private Location home;

	@Getter
	private Map<UUID, IslandMember> membersMap;
	@Getter
	private List<UUID> banneds;

	@Getter
	private List<VisitorPermission> visitorsPermissions;
	@Getter
	private List<GeneralPermission> generalPermissions;
	@Getter
	private List<CoopPermission> coopPermissions;

	public Island(String ID, String owner, String name, int level, Location home, int midX, int midZ, short range, boolean locked,
				  List<IslandMember> members, List<UUID> banneds,
				  List<VisitorPermission> visitorsPermissions, List<GeneralPermission> generalPermissions, List<CoopPermission> coopPermissions) {
		this.ID = ID;
		this.owner = owner;
		this.name = name;
		this.level = level;
		this.home = home;

		this.midX = midX;
		this.midZ = midZ;
		this.range = range;
		calcBounds();

		this.locked = locked;

		Map<UUID, IslandMember> membersMap = Maps.newHashMap();
		members.forEach(member -> membersMap.put(member.getUniqueId(), member));
		this.membersMap = membersMap;
		this.banneds = banneds;

		this.visitorsPermissions = visitorsPermissions;
		this.generalPermissions = generalPermissions;
		this.coopPermissions = coopPermissions;
	}

	@Override
	public String toString() {
		return LocationUtil.inlineSerialize(home, false, true) + "|" + midX + "|" + midZ + "|" + range + "|" + locked;
	}

	public String toString_members() {
		StringBuilder bldr = new StringBuilder();

		membersMap.values().forEach(member -> bldr.append(member.getUniqueId().toString()).append("|").append(member.getRole().id).append(";"));
		if (!banneds.isEmpty()) {
			bldr.append("+");
			banneds.forEach(banned -> bldr.append(banned.toString()).append(";"));
		}

		return bldr.toString();
	}

	public String toString_permissions() {
		StringBuilder bldr = new StringBuilder();
		for (VisitorPermission perm : visitorsPermissions) {
			bldr.append(perm.getVal());
		}
		bldr.append("|");
		for (GeneralPermission perm : generalPermissions) {
			bldr.append(perm.getVal());
		}
		bldr.append("|");
		for (CoopPermission perm : coopPermissions) {
			bldr.append(perm.getVal());
		}
		return bldr.toString();
	}

	public OfflinePlayer getOwner() {
		return Bukkit.getOfflinePlayer(UUID.fromString(owner));
	}

	public String getDisplayName() {
		if (name == null)
			return "de " +  getOwner().getName();
		return name;
	}

	public void setHome(Location location) {
		this.home = location;
		save();
	}

	protected void setHomeWithoutSaving(Location location) {
		this.home = location;
	}

	public int getMiddleX() {
		return this.midX;
	}

	public int getMiddleZ() {
		return this.midZ;
	}

	private int minX, maxX;
	private int minZ, maxZ;

	private void calcBounds() {
		this.minX = this.midX - this.range;
		this.minZ = this.midZ - this.range;
		this.maxX = this.midX + this.range;
		this.maxZ = this.midZ + this.range;
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
		save();
	}

	public boolean isInBounds(int x, int z) {
		return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
	}

	public boolean isInBounds(Location location) {
		return isInBounds(location.getBlockX(), location.getBlockZ());
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
		save();
	}

	public boolean hasAccess(OfflinePlayer player) {
		return hasFullAccess(player) || CoopsManager.isCooped(player.getUniqueId(), ID);
	}

	/**
	 * @param player The player to test
	 * @return if the player is in this island (Full access means not a visitor nor a coop)
	 */
	public boolean hasFullAccess(OfflinePlayer player) {
		return player.getUniqueId().equals(getOwner().getUniqueId()) || membersMap.containsKey(player.getUniqueId());
	}

	public boolean hasRoleOrAbove(IslandMember member, IslandRole role) {
		if (member == null)
			return false;
		return isOwner(member.getUniqueId()) || member.getRole().ordinal() >= role.ordinal();
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

	public boolean isOwner(UUID uuid) {
		return uuid.equals(getOwner().getUniqueId());
	}

	public boolean isOwner(SkyblockPlayer player) {
		return player.getBaseId().equals(getOwner().getUniqueId());
	}

	public IslandRole getRole(UUID player) {
		if (player.equals(getOwner().getUniqueId()))
			return IslandRole.OWNER;

		return membersMap.get(player).getRole();
	}

	public IslandRole getRole(SkyblockPlayer player) {
		return getRole(player.getBaseId());
	}

	public void setRole(IslandMember member, IslandRole role) {
		if (member == null)
			return;

		membersMap.put(member.getUniqueId(), new IslandMember(member.getUniqueId(), role));
		saveMembers();
	}

	public void setRole(OfflinePlayer player, IslandRole role) {
		if (isOwner(player.getUniqueId())) {
			CorePrinter.warn("Cannot change IslandRole of owner --> Is this a bug?");
			return;
		}
		setRole(membersMap.get(player.getUniqueId()), role);
	}

	public void setRole(SkyblockPlayer player, IslandRole role) {
		if (isOwner(player.getBaseId())) {
			CorePrinter.warn("Cannot change IslandRole of owner --> Is this a bug?");
			return;
		}
		setRole(membersMap.get(player.getBaseId()), role);
	}

	public boolean hasVisitorPermission(VisitorPermission permission) {
		return this.visitorsPermissions.contains(permission);
	}

	public boolean hasGeneralPermission(GeneralPermission permission) {
		return this.generalPermissions.contains(permission);
	}

	public boolean hasCoopPermission(CoopPermission permission) {
		return this.coopPermissions.contains(permission);
	}

	public void updatePermissions() {
		savePermissions();
	}

	public void save() {
		try {
			Storage.ISLANDS.setString("toWrap", toString(), "island_id", ID);
		}
		catch (SQLActionImpossibleException e) {
			e.printStackTrace();
		}
	}

	public void saveMembers() {
		try {
			Storage.ISLANDS.setString("members_toWrap", toString_members(), "island_id", ID);
		}
		catch (SQLActionImpossibleException e) {
			e.printStackTrace();
		}
	}

	protected void savePermissions() {
		try {
			Storage.ISLANDS.setString("settings", toString_permissions(), "island_id", ID);
		}
		catch (SQLActionImpossibleException e) {
			e.printStackTrace();
		}
	}

}
