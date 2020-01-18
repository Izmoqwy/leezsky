package lz.izmoqwy.core.api.nickname;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import lz.izmoqwy.core.hooks.HooksManager;
import lz.izmoqwy.core.nms.NMS;
import lz.izmoqwy.core.self.CorePrinter;
import lz.izmoqwy.core.self.LeezCore;
import lz.izmoqwy.core.utils.ReflectionUtil;
import lz.izmoqwy.core.utils.ServerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.regex.Pattern;

public class NicknameAPI implements Listener {

	private final Pattern VALID_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");
	public static final NicknameAPI instance = new NicknameAPI();

	private NicknameAPI() {
	}

	private Field profileUsernameField = null;
	private Map<Player, String> originalNicks = Maps.newHashMap();

	public void load() {
		profileUsernameField = ReflectionUtil.getField(GameProfile.class, "name");

		ServerUtil.registerListeners(LeezCore.instance, instance);
		Bukkit.getOnlinePlayers().forEach(player -> originalNicks.put(player, player.getName()));
	}

	public void unload() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			setNickname(player, getOriginalNick(player), false);
		}
	}

	/*
	Events
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPreLogin(PlayerLoginEvent event) {
		originalNicks.put(event.getPlayer(), event.getPlayer().getName());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPostLogin(PlayerLoginEvent event) {
		if (event.getResult() != Result.ALLOWED) originalNicks.remove(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onDisconnect(PlayerQuitEvent event) {
		originalNicks.remove(event.getPlayer());
	}

	/*
	Nick assignment
	 */
	public boolean setNickName(final Player player, String name) {
		return setNickname(player, name, true);
	}

	private boolean setNickname(final Player player, String name, boolean refreshNTE) {
		try {
			NMS.global.setNameField(profileUsernameField, player, name);

			// Refresh player in tablist
			// Can be removed (refreshing in world actually refresh him in tab) ?
			NMS.packet.removeFromTablist(player);
			NMS.packet.addToTablist(player);

			// Refresh player in world
			JavaPlugin pluginInstance = JavaPlugin.getPlugin(LeezCore.class);
			for (Player online : Bukkit.getOnlinePlayers()) {
				online.hidePlayer(pluginInstance, player);
				online.showPlayer(pluginInstance, player);
			}

			if (refreshNTE && HooksManager.useNTE()) {
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
						}.runTaskLater(pluginInstance, 20);
					}

				}.runTaskLater(pluginInstance, 3);
			}

			Bukkit.getPluginManager().callEvent(new NicknameChangedEvent(player.getUniqueId(), name));
			return true;
		}
		catch (IllegalArgumentException | IllegalAccessException ex) {
			CorePrinter.err("Unable to set a nick for '" + player.getName() + "'.");
			ex.printStackTrace();
			return false;
		}
	}

	public String getOriginalNick(Player player) {
		return originalNicks.get(player);
	}

	public boolean isValid(String name) {
		return VALID_PATTERN.matcher(name).matches();
	}

}
