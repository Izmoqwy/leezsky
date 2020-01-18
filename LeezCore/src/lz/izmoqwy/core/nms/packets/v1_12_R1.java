package lz.izmoqwy.core.nms.packets;

import lz.izmoqwy.core.utils.ReflectionUtil;
import net.minecraft.server.v1_12_R1.*;
import net.minecraft.server.v1_12_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_12_R1.PacketPlayOutTitle.EnumTitleAction;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Objects;

public class v1_12_R1 implements NMSPacket {

	@Override
	public void addToTablist(Player player) {
		if (!player.isOnline()) return;
		PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, cp(player).getHandle());
		sendPacket(packet);
	}

	@Override
	public void removeFromTablist(Player player) {
		PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, cp(player).getHandle());
		sendPacket(packet);
	}

	@Override
	public void sendJson(Player player, String json) {
		PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a(json));
		sendPacket(player, packet);
	}

	@Override
	public void sendTitle(Player player, String title, String subTitle) {
		if (title != null) {
			sendPacket(player, new PacketPlayOutTitle(EnumTitleAction.TITLE, serializeText(title)));
		}
		if (subTitle != null) {
			sendPacket(player, new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, serializeText(subTitle)));
		}
	}

	@Override
	public void sendActionbar(Player player, String message) {
		PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\": \"" + message + "\"}"), ChatMessageType.GAME_INFO);
		sendPacket(player, packet);
	}

	private PacketPlayOutPlayerListHeaderFooter getTablistPacket(String header, String footer) {
		PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();

		try {
			if (header != null) {
				Field headerField = ReflectionUtil.getField(packet.getClass(), "a");
				Objects.requireNonNull(headerField).set(packet, ChatSerializer.a("{\"text\": \"" + header + "\"}"));
				headerField.setAccessible(!headerField.isAccessible());
			}
			if (footer != null) {
				Field footerField = ReflectionUtil.getField(packet.getClass(), "b");
				Objects.requireNonNull(footerField).set(packet, ChatSerializer.a("{\"text\": \"" + footer + "\"}"));
				footerField.setAccessible(!footerField.isAccessible());
			}
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return packet;
	}

	private IChatBaseComponent serializeText(String text) {
		return ChatSerializer.a("{\"text\": \"" + text + "\"}");
	}

	@Override
	public void sendTablist(Player player, String header, String footer) {
		sendPacket(player, getTablistPacket(header, footer));
	}

	@Override
	public void sendGlobalTablist(String header, String footer) {
		sendPacket(getTablistPacket(header, footer));
	}

	@Override
	public void sendTimings(Player player, int ticks, int fadeIn, int fadeOut) {
		PacketPlayOutTitle packet = new PacketPlayOutTitle(EnumTitleAction.TIMES, null, fadeIn, ticks, fadeOut);
		sendPacket(player, packet);
	}

	@Override
	public int getLatency(Player player) {
		return cp(player).getHandle().ping;
	}

	@Override
	public void setFakeBorder(Player player, double radius, Location location) {
		WorldBorder border = new WorldBorder();

		border.setSize(radius);
		border.setCenter(location.getX(), location.getZ());
		border.setWarningDistance(0);

		border.world = ((CraftWorld) location.getWorld()).getHandle();

		sendPacket(player, new PacketPlayOutWorldBorder(border, PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_SIZE));
		sendPacket(player, new PacketPlayOutWorldBorder(border, PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_CENTER));
		sendPacket(player, new PacketPlayOutWorldBorder(border, PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_WARNING_BLOCKS));
	}

	private void sendPacket(Packet<?> packet) {
		for (Player all : Bukkit.getOnlinePlayers()) {
			cp(all).getHandle().playerConnection.sendPacket(packet);
		}
	}

	private void sendPacket(Player player, Packet<?> packet) {
		cp(player).getHandle().playerConnection.sendPacket(packet);
	}

	private CraftPlayer cp(Player player) {
		return (CraftPlayer) player;
	}

}
