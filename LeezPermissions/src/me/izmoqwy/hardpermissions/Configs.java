package me.izmoqwy.hardpermissions;

import com.google.common.collect.Lists;
import lz.izmoqwy.core.hooks.HooksManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Configs {

	static File groups, players;
	static FileConfiguration groups_c, players_c;

	private static LeezPermissions ins;

	//Groups
	private static List<Group> all_groups;
	private static Group default_group;

	//Players
	private static Map<UUID, Group> all_players;

	protected static void boot() {
		ins = LeezPermissions.getInstance();

		/*
		 * Groups
		 */
		groups = new File(ins.getDataFolder(), "groups.yml");
		groups_c = YamlConfiguration.loadConfiguration(groups);
		YamlConfiguration defaultGroups = getDefaultConfig("groups");
		groups_c.setDefaults(defaultGroups);
		if (!groups.exists()) ins.saveResource("groups.yml", false);

		reloadAllGroups();

		/*
		 * Players
		 */
		players = new File(ins.getDataFolder(), "players.yml");
		players_c = YamlConfiguration.loadConfiguration(players);
		YamlConfiguration defaultPlayers = getDefaultConfig("players");
		 players_c.setDefaults(defaultPlayers);
		if (!players.exists()) ins.saveResource("players.yml", false);

		reloadAllPlayers();

	}

	private static YamlConfiguration getDefaultConfig(String name) {
		Reader rd = new InputStreamReader(ins.getResource(name + ".yml"), StandardCharsets.UTF_8);
		return YamlConfiguration.loadConfiguration(rd);
	}

	private static void reloadGroups() {
		groups_c = YamlConfiguration.loadConfiguration(groups);
	}

	private static void reloadPlayers() {
		players_c = YamlConfiguration.loadConfiguration(players);
	}

	private static boolean saveGroups() {
		try {
			groups_c.save(groups);
			return true;
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	private static boolean savePlayers() {
		try {
			players_c.save(players);
			return true;
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	protected static void reloadAllGroups() {
		reloadGroups();

		all_groups = Lists.newLinkedList();
		Map<String, List<String>> inheritances = new HashMap<>();
		for (String name : groups_c.getKeys(false)) {
			ConfigurationSection section = groups_c.getConfigurationSection(name);

			String prefix = section.getString("info.prefix", ""), suffix = section.getString("info.suffix", ""),
					chatcolor = section.getString("info.chatcolor", "§7");
			int power = section.getInt("power", 0);

			List<String> permissions = Lists.newArrayList(), inheris = null;
			if (section.isSet("permissions") && section.isList("permissions"))
				permissions = section.getStringList("permissions");
			if (section.isSet("inheritance") && section.isList("inheritance"))
				inheris = section.getStringList("inheritance");

			Group grp = new Group(name, power, prefix, suffix, permissions, chatcolor);
			all_groups.add(grp);
			if (inheris != null && !inheris.isEmpty()) inheritances.put(name, inheris);

			if (default_group == null && section.isSet("default") && section.isBoolean("default") && section.getBoolean("default"))
				default_group = grp;
		}

		inheritances.forEach((groupName, list) -> {
			Group g = getGroup(groupName);
			if (g == null) return;

			List<Group> is = Lists.newArrayList();
			for (String name : list) {
				Group t = getGroup(name);
				if (t != null) is.add(t);
			}
			g.setInheritances(is);
		});

		for (Group group : getAllGroups()) {
			List<String> perms = group.getPermissions();
			if (group.getInheritances() == null) continue;
			for (Group ine : group.getInheritances()) {
				for (String perm : ine.getPermissions()) {
					if (perm.startsWith("nte.")) continue;
					if (!perms.contains(perm)) perms.add(perm);
				}
			}
			group.setPermissions(perms);

		}

		if (default_group == null)
			default_group = new Group("Default_Group_" + new Random().nextInt(5000),0, "", "", Lists.newArrayList(), "§7");
	}

	protected static void reloadAllPlayers() {
		reloadPlayers();

		all_players = new HashMap<>();
		for (Player all : Bukkit.getOnlinePlayers()) {
			if (players_c.isConfigurationSection(all.getUniqueId().toString())) {
				ConfigurationSection section = players_c.getConfigurationSection(all.getUniqueId().toString());
				if (section.isSet("group") && section.isString("group")) {
					Group group = getGroup(section.getString("group"));
					if (group != null) all_players.put(all.getUniqueId(), group);
				}
			}
		}
	}

	public static void loadPlayerLoad(Player player) {
		if (all_players == null) all_players = new HashMap<>();
		if (players_c.isConfigurationSection(player.getUniqueId().toString())) {
			ConfigurationSection section = players_c.getConfigurationSection(player.getUniqueId().toString());
			if (section.isSet("group") && section.isString("group")) {
				Group group = getGroup(section.getString("group"));
				if (group != null) all_players.put(player.getUniqueId(), group);
			}
		}

	}

	public static void loadPlayer(Player player) {
		if (all_players == null) all_players = new HashMap<>();
		if (players_c.isConfigurationSection(player.getUniqueId().toString())) {
			ConfigurationSection section = players_c.getConfigurationSection(player.getUniqueId().toString());
			if (section.isSet("group") && section.isString("group")) {
				Group group = getGroup(section.getString("group"));
				if (group != null) all_players.put(player.getUniqueId(), group);
			}
		}

	}

	public static void unloadPlayer(Player player) {
		if (all_players == null) return;
		all_players.remove(player.getUniqueId());
	}

	public static List<Group> getAllGroups() {
		return all_groups;
	}

	public static Group getGroup(String name) {
		for (Group grp : getAllGroups()) {
			if (grp.getName().equalsIgnoreCase(name)) return grp;
		}
		return null;
	}

	public static Group getDefaultGroup() {
		return default_group;
	}

	public static Group getGroupInConfig(OfflinePlayer target) {
		Group grp = null;
		if (players_c.isConfigurationSection(target.getUniqueId().toString())) {
			ConfigurationSection section = players_c.getConfigurationSection(target.getUniqueId().toString());
			if (section.isSet("group") && section.isString("group")) {
				Group group = getGroup(section.getString("group"));
				if (group != null) grp = group;
			}
		}
		return grp;
	}

	public static void setGroup(OfflinePlayer target, Group group) {
		players_c.set(target.getUniqueId() + ".group", group.getName());
		if (savePlayers()) {
			if (target.isOnline()) {
				all_players.put(target.getUniqueId(), group);
				LeezPermissions.refreshPermissions(Bukkit.getPlayer(target.getUniqueId()));

				if (HooksManager.useNTE()) {
					HooksManager.nte().getApi().reloadNametag(Bukkit.getPlayer(target.getUniqueId()));
				}
			}
		}
	}

	public static void setGroupFromCS(OfflinePlayer target, Group group, CommandSender sender) {
		setGroup(target, group);
		for (Player notified : Bukkit.getOnlinePlayers()) {
			if (notified == sender || !notified.hasPermission("leezpermissions.notified")) continue;

			Locale.GROUP_CHANGED.send(notified, target.getName(), sender.getName(), group.getName());
		}
		Locale.GROUP_CHANGED.send(Bukkit.getConsoleSender(), target.getName(), sender.getName(), group.getName());
	}

	public static void addGroup(String g_name) {
		String path = g_name + ".";
		groups_c.set(path + "default", false);
		groups_c.set(path + "permissions", Lists.newArrayList());
		groups_c.set(path + "inheritance", Lists.newArrayList());
		groups_c.set(path + "info.prefix", "");
		groups_c.set(path + "info.suffix", "");
		if (saveGroups())
			LeezPermissions.reload();
	}

	public static void editGroup(String g_name, Group to) {
		String path = g_name + ".";
		groups_c.set(path + "permissions", to.getPermissions());
//		groups_c.set(path + "inheritance", Lists.newArrayList());
		groups_c.set(path + "info.prefix", to.getPrefix());
		groups_c.set(path + "info.suffix", to.getSuffix());
		if (saveGroups())
			LeezPermissions.reload();
	}

	public static void removeGroup(String g_name) {
		groups_c.set(g_name, null);
		if (saveGroups())
			LeezPermissions.reload();
	}

	public static boolean hasPlayerGroup(Player target) {
		if (all_players.containsKey(target.getUniqueId())) return true;
		return true;
	}

	public static Group getPlayerGroup(Player target) {
		if (hasPlayerGroup(target)) {
			return all_players.get(target.getUniqueId());
		}
		return null;
	}

	public static Group getPlayerGroupOrDefault(Player target) {
		Group group = getPlayerGroup(target);
		if (group == null) group = getDefaultGroup();
		return group;
	}

	public static Group getOfflinePlayerGroupOrDefault(OfflinePlayer target) {
		Group group = getGroupInConfig(target);
		if (group == null) group = getDefaultGroup();
		return group;
	}

}
