package lz.izmoqwy.market.npc;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import lz.izmoqwy.market.MarketPlugin;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class NPC_v1_12_R1 {

	@Getter
	private String displayName;
	@Getter
	private Location location;
	@Getter
	private String url, signature;

	private EntityPlayer entityPlayer;

	public NPC_v1_12_R1(String displayName, Location location, String url, String signature) {
		this.displayName = displayName;
		this.location = location;
		this.url = url;
		this.signature = signature;
	}

	public void createEntity() {
		MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
		WorldServer world = ((CraftWorld) location.getWorld()).getHandle();

		GameProfile profile = new GameProfile(UUID.randomUUID(), displayName);
		profile.getProperties().put("textures", new Property("textures", url != null ? url : "none=", signature != null ? signature : "none="));

		EntityPlayer npc = new EntityPlayer(server, world, profile, new PlayerInteractManager(world));
		npc.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		this.entityPlayer = npc;
	}

	public void spawn() {
		for (Player player : Bukkit.getOnlinePlayers())
			spawn(player);
	}

	public void spawn(Player player) {
		if (entityPlayer == null)
			createEntity();

		if (entityPlayer == null)
			return;

		PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
		connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer));
		connection.sendPacket(new PacketPlayOutNamedEntitySpawn(entityPlayer));
		connection.sendPacket(new PacketPlayOutEntityHeadRotation(entityPlayer, (byte) ((location.getYaw() * 256.0F) / 360.0F)));
		Bukkit.getScheduler().scheduleSyncDelayedTask(MarketPlugin.getInstance(), () -> connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer)), 5);
	}

	public void updateSkin(Player player) {
		if (entityPlayer == null)
			return;

		PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
		connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer));
		Bukkit.getScheduler().scheduleSyncDelayedTask(MarketPlugin.getInstance(), () -> connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer)), 5);
	}

	public void despawn() {
		for (Player player : Bukkit.getOnlinePlayers())
			despawn(player);
	}

	public void despawn(Player player) {
		if (entityPlayer == null)
			return;

		PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
		connection.sendPacket(new PacketPlayOutEntityDestroy(entityPlayer.getId()));
	}

	public void move(Location location) {
		this.location = location;

		despawn();
		entityPlayer = null;
		Bukkit.getScheduler().runTaskLater(MarketPlugin.getInstance(), this::spawn, 5);
	}

	public int getEntityId() {
		return entityPlayer != null ? entityPlayer.getId() : -1;
	}

	public org.bukkit.World getWorld() {
		return entityPlayer != null ? Bukkit.getWorld(entityPlayer.getWorld().worldData.getName()) : null;
	}

}
