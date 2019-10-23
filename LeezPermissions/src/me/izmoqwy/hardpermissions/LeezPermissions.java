package me.izmoqwy.hardpermissions;

import com.google.common.collect.Lists;
import lombok.Getter;
import lz.izmoqwy.core.crosshooks.CrosshooksManager;
import lz.izmoqwy.core.crosshooks.interfaces.LeezPermissionsCH;
import lz.izmoqwy.core.helpers.PluginHelper;
import lz.izmoqwy.core.hooks.HooksManager;
import lz.izmoqwy.core.i18n.LocaleManager;
import me.izmoqwy.hardpermissions.commands.MainCommand;
import me.izmoqwy.hardpermissions.listeners.DefaultChat;
import me.izmoqwy.hardpermissions.listeners.PlayersListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LeezPermissions extends JavaPlugin {

	@Getter
	private static LeezPermissions instance;

	static Map<UUID, PermissionAttachment> playersAttachments;
	static List<Player> afks = Lists.newArrayList();

	@Override
	public void onEnable() {
		instance = this;
		playersAttachments = new HashMap<>();

		Configs.boot();
		for (Player all : Bukkit.getOnlinePlayers()) {
			onJoin(all);
		}

		PluginHelper.loadListener(this, new PlayersListener());
		PluginHelper.loadListener(this, new DefaultChat());

		PluginHelper.loadListener(this, new PluginsManager());
		PluginHelper.loadCommand("leezpermissions", new MainCommand());

		PluginsManager.calculateAllPermissions();
		load();

		CrosshooksManager.registerHook(this, new LeezPermissionsCH() {
			@Override
			public lz.izmoqwy.core.crosshooks.interfaces.Group getGroup(OfflinePlayer player) {
				Group group = Configs.getOfflinePlayerGroupOrDefault(player);
				return new lz.izmoqwy.core.crosshooks.interfaces.Group() {
					@Override
					public String getName() {
						return group.getName();
					}

					@Override
					public String getPrefix() {
						return group.getPrefix();
					}

					@Override
					public String getSuffix() {
						return group.getSuffix();
					}

					@Override
					public ChatColor getChatColor() {
						return ChatColor.getByChar(group.getChatcolor().length() == 2 ? group.getChatcolor().charAt(1) : group.getChatcolor().charAt(0));
					}
				};
			}

			@Override
			public String getHookName() {
				return "LeezPermissions";
			}
		});
		LocaleManager.register(this, Locale.class);
	}

	@Override
	public void onDisable() {
		if (instance != null) instance = null;

		for (Player player : Bukkit.getOnlinePlayers())
			onQuit(player);
	}

	public static void load() {
		Configs.boot();

		for (Player all : Bukkit.getOnlinePlayers()) {
			onQuit(all);
			onJoinLoad(all);
			if (HooksManager.useNTE()) {
				HooksManager.nte().getApi().reloadNametag(all);
			}
		}

		PluginsManager.calculateAllPermissions();
	}

	public static void reload() {
		Configs.boot();

		for (Player all : Bukkit.getOnlinePlayers()) {
			onQuit(all);
			onJoin(all);
		}

		PluginsManager.calculateAllPermissions();
	}

	public static void onJoin(Player joiner) {
		PermissionAttachment attch = joiner.addAttachment(instance);
		playersAttachments.put(joiner.getUniqueId(), attch);
		Configs.loadPlayer(joiner);
		refreshPermissions(joiner);
	}

	static void onJoinLoad(Player joiner) {
		PermissionAttachment attch = joiner.addAttachment(instance);
		playersAttachments.put(joiner.getUniqueId(), attch);
		Configs.loadPlayerLoad(joiner);
		refreshPermissions(joiner);
	}

	static PermissionAttachment getAttch(Player target) {
		PermissionAttachment attch = null;
		if (playersAttachments.containsKey(target.getUniqueId()))
			attch = playersAttachments.get(target.getUniqueId());
		return attch;
	}

	private static void removeAllPerms(Player target) {
		PermissionAttachment attch = getAttch(target);
		attch.getPermissions().forEach((str, bool) -> attch.unsetPermission(str));
	}

	static void refreshPermissions(Player target) {
		PermissionAttachment attch = getAttch(target);
		if (attch == null) return;
		removeAllPerms(target);
		Group group = Configs.getPlayerGroupOrDefault(target);
		for (String permission : group.getPermissions()) {
			String absPerm = permission.charAt(0) == '-' ? permission.replaceFirst("-", "") : permission;
			boolean authorize = group.getPermissions().contains(absPerm);
			if (afks.contains(target) && absPerm.startsWith("nte.") && !absPerm.endsWith("afk"))
				absPerm += "afk";
			attch.setPermission(absPerm, authorize);
		}
		if (group.getPermissions().contains("*")) {
			for (Permission perm : PluginsManager.getAllPermissions()) {
				attch.setPermission(perm, true);
			}
		}
		if (HooksManager.useNTE()) {
			HooksManager.nte().getApi().reloadNametag(target);
		}
	}

	public static void onQuit(Player leaver) {
		PermissionAttachment attch = getAttch(leaver);
		Configs.unloadPlayer(leaver);
		if (attch != null) {
			removeAllPerms(leaver);
			leaver.removeAttachment(attch);
		}
	}

}
