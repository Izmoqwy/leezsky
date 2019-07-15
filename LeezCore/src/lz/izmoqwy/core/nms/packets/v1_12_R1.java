package lz.izmoqwy.core.nms.packets;

import java.lang.reflect.Field;
import java.util.Collection;

import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.minecraft.server.v1_12_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_12_R1.PacketPlayOutTitle.EnumTitleAction;

public class v1_12_R1 implements NMSPacket {

	public void removeProfileProperty(Player player, String property) {
		gp(player).getProperties().removeAll(property);
	}

	public void putProfileProperties(Player player, String property, Collection<Property> values) {
		gp(player).getProperties().putAll(property, values);
	}

	public void setNameField(Field field, Player player, String value)
			throws IllegalArgumentException, IllegalAccessException {
		field.set(gp(player), value);
	}

	public void addToTablist(Player player) {
		if (!player.isOnline()) return;
		PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, new EntityPlayer[]{cp(player).getHandle()});
		sendPacket(packet);
	}

	public void removeFromTablist(Player player) {
		PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, new EntityPlayer[]{cp(player).getHandle()});
		sendPacket(packet);
	}

	public void sendJson(Player player, String json) {
		PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a(json));
		sendPacket(player, packet);
	}

	public void sendTitle(Player player, String title, String subtitle, int ticks, int fadein, int fadeout) {
		PacketPlayOutTitle titlepacket = new PacketPlayOutTitle(EnumTitleAction.TITLE, ChatSerializer.a("{\"text\": \"" + title + "\"}")),
				subtitlepacket = new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, ChatSerializer.a("{\"text\": \"" + subtitle + "\"}"));
		sendPacket(player, titlepacket);
		sendPacket(player, subtitlepacket);
		sendTime(player, ticks, fadein, fadeout);
	}

	public void sendActionbar(Player player, String message) {
		PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\": \"" + message + "\"}"), ChatMessageType.GAME_INFO);
		sendPacket(player, packet);
	}

	private void sendTime(Player player, int duration, int fadein, int fadeout) {
		PacketPlayOutTitle packet = new PacketPlayOutTitle(EnumTitleAction.TIMES, null, fadein, duration, fadeout);
		sendPacket(player, packet);
	}

	public int getPing(Player player) {
		return cp(player).getHandle().ping;
	}

	@SuppressWarnings("deprecation")
	public void respawn(Player player) {
		PacketPlayOutRespawn packet = new PacketPlayOutRespawn(cp(player).getWorld().getEnvironment().getId(),
				EnumDifficulty.getById(cp(player).getWorld().getDifficulty().getValue()),
				WorldType.getType(cp(player).getWorld().getWorldType().getName()),
				EnumGamemode.getById(cp(player).getGameMode().getValue()));
		sendPacket(player, packet);
	}

	public void setBorder(Player player, double radius, Location location) {
		WorldBorder border = new WorldBorder();

		border.setSize(radius);
		border.setCenter(location.getX(), location.getZ());
		border.setWarningDistance(0);

		border.world = (WorldServer) cp(player).getHandle().world;

		sendPacket(player, new PacketPlayOutWorldBorder(border, PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_SIZE));
		sendPacket(player, new PacketPlayOutWorldBorder(border, PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_CENTER));
		sendPacket(player, new PacketPlayOutWorldBorder(border, PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_WARNING_BLOCKS));
	}

	public Player loadPlayer(OfflinePlayer player) {
		MinecraftServer minecraftserver = MinecraftServer.getServer();
		GameProfile gameprofile = new GameProfile(player.getUniqueId(), player.getName());
		EntityPlayer entity = new EntityPlayer(minecraftserver, minecraftserver.getWorldServer(0), gameprofile, new PlayerInteractManager(minecraftserver.getWorldServer(0)));

		return entity.getBukkitEntity();
	}

	private void sendPacket(Packet<?> packet) {
		for (Player all : Bukkit.getOnlinePlayers()) {
			cp(all).getHandle().playerConnection.sendPacket(packet);
		}
	}

	private void sendPacket(Player player, Packet<?> packet) {
		cp(player).getHandle().playerConnection.sendPacket(packet);
	}

	private GameProfile gp(Player player) {
		return cp(player).getProfile();
	}

	private CraftPlayer cp(Player player) {
		return (CraftPlayer) player;
	}

}
