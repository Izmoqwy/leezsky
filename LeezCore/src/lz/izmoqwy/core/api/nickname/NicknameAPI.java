package lz.izmoqwy.core.api.nickname;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.regex.Pattern;

import lz.izmoqwy.core.CorePrinter;
import lz.izmoqwy.core.LeezCore;
import lz.izmoqwy.core.helpers.PluginHelper;
import lz.izmoqwy.core.helpers.ReflectorHelper;
import lz.izmoqwy.core.hooks.HooksManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;

import lz.izmoqwy.core.nms.NmsAPI;

public class NicknameAPI implements Listener {

	private static final NicknameAPI instance = new NicknameAPI();

	private static Field nameField = null;
	private static final Pattern pattern = Pattern.compile("[^a-zA-Z0-9_]");
	private static final Map<Player, String> usernames = Maps.newHashMap();

	public static void load() {

		PluginHelper.loadListener(LeezCore.instance, instance);
		nameField = ReflectorHelper.getField(GameProfile.class, "name");

		for (Player player : Bukkit.getOnlinePlayers()) {
			usernames.put(player, player.getName());
		}
	}

	public static void unload() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			setNickname(player, getRealname(player), false);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPreLogin(PlayerLoginEvent event) {
		usernames.put(event.getPlayer(), event.getPlayer().getName());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPostLogin(PlayerLoginEvent event) {
		if (event.getResult() != Result.ALLOWED) usernames.remove(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onDisconnect(PlayerQuitEvent event) {
		usernames.remove(event.getPlayer());
	}

	public static String getRealname(Player player) {
		return usernames.get(player);
	}

	public static boolean isCorrect(String name) {
		return name.length() <= 16 && name.length() >= 3 && !pattern.matcher(name).find();
	}

	public static boolean setNickName(final Player player, String name) {
		return setNickname(player, name, true);
	}

	private static boolean setNickname(final Player player, String name, boolean refreshNTE) {
		try {

			/*
			 * Rename player
			 */
			NmsAPI.packet.setNameField(nameField, player, name);

			/*
			 * Refresh player from tab
			 */
			NmsAPI.packet.removeFromTablist(player);
			NmsAPI.packet.addToTablist(player);

			/*
			 * Refresh player from world
			 */
			for (Player all : Bukkit.getOnlinePlayers()) {
				all.hidePlayer(player);
				all.showPlayer(player);
			}

			if (!refreshNTE)
				return true;

			Bukkit.getPluginManager().callEvent(new NicknameChangedEvent(player.getUniqueId(), name));

			/*
			 * Reload from tablist
			 */
			if (HooksManager.useNTE()) {
				new BukkitRunnable() {

					@Override
					public void run() {
						HooksManager.nte().getApi().reloadNametag(player);
						new BukkitRunnable() {
							@Override
							public void run() {
								if (HooksManager.nte().getApi().getFakeTeam(player) == null)
									HooksManager.nte().forceReload();
							}
						}.runTaskLater(LeezCore.instance, 20);
					}

				}.runTaskLater(LeezCore.instance, 3);
			}
			return true;
		}
		catch (IllegalArgumentException | IllegalAccessException ex) {
			CorePrinter.err("Unable to set a nick for '" + player.getName() + "'.");
			ex.printStackTrace();
			return false;
		}
	}
}
