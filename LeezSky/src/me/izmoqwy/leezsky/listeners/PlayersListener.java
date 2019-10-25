package me.izmoqwy.leezsky.listeners;

import com.google.common.collect.Lists;
import lz.izmoqwy.core.crosshooks.CrosshooksManager;
import lz.izmoqwy.core.crosshooks.interfaces.Group;
import lz.izmoqwy.core.crosshooks.interfaces.IslandInfo;
import lz.izmoqwy.core.crosshooks.interfaces.LeezIslandCH;
import lz.izmoqwy.core.crosshooks.interfaces.LeezPermissionsCH;
import lz.izmoqwy.core.nms.NmsAPI;
import lz.izmoqwy.core.utils.TitleUtil;
import me.izmoqwy.leezsky.LeezSky;
import me.izmoqwy.leezsky.managers.SettingsManager;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.text.MessageFormat;
import java.util.List;

public class PlayersListener implements Listener {

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		NmsAPI.packet.sendTablist(player, LeezSky.TAB_HEADER, LeezSky.TAB_FOOTER);
		if (player.hasPlayedBefore())
			TitleUtil.sendTitle(player, "§aBon retour", "§2parmi nous ;)", 5);
		else {
			player.sendMessage("");

			String helper = "§4[OP] §cLeezSky §8-> §emoi§8: ";
			player.sendMessage(helper + "§6BIP BIP BIP ! §eIl semblerait que tu sois nouveau ici, laisse moi te donner quelques informations.");
			player.sendMessage(helper + "§eTu es actuellement sur un serveur skyblock simple. Un skyblock simple est un skyblock où les joueurs progressent plus rapidement que la normale.");
			player.sendMessage(helper + "§eUtilise la commande §6/settings §epour configurer tes paramètres de base et §6/help §epour obtenir de l'aide sur le serveur et/ou les commandes.");
			player.sendMessage("§d§oBesoin de plus d'informations sur le skyblock en général ? Utilise §5/help skyblock§d§o.");

			player.sendMessage("");
		}
		LeezSky.createScoreboard(player);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onAsyncChat(AsyncPlayerChatEvent event) {
		event.setCancelled(true);

		Player player = event.getPlayer();
		if (SettingsManager.CHAT_MESSAGES.getState(player) == SettingsManager.ChatSetting.OFF) {
			player.sendMessage(LeezSky.PREFIX + "§cVous avez désactivé les messges du chat, vous devez les réactiver pour pouvoir y parler. §7(/settings)");
			return;
		}

		String message = event.getMessage();
		if (player.hasPermission("leezsky.chat.color")) {
			message = message.replaceAll("&([\\da-f])", "§$1");
		}

		String displayName = player.getDisplayName();
		ChatColor chatcolor = ChatColor.DARK_GRAY;
		int power = 0;
		if (CrosshooksManager.isPluginRegistred("LeezPermissions")) {
			LeezPermissionsCH permissions = CrosshooksManager.get("LeezPermissions", LeezPermissionsCH.class);
			if (permissions != null) {
				Group group = permissions.getGroup(player);

				displayName = ChatColor.translateAlternateColorCodes('&', group.getPrefix() + player.getName() + group.getSuffix());
				chatcolor = group.getChatColor();
				power = group.getPower();
			}
		}

		if (!message.startsWith("§"))
			message = chatcolor + message;

		String flatMessage = MessageFormat.format("{0} {1}➟ {2}", displayName, ChatColor.DARK_GRAY, message);
		ComponentBuilder componentBuilder = new ComponentBuilder("");

		LeezIslandCH CH = CrosshooksManager.isPluginRegistred("LeezIsland") ? CrosshooksManager.get("LeezIsland", LeezIslandCH.class) : null;
		if (CH != null) {
			IslandInfo islandInfo = CH.getIslandInfo(player);
			if (islandInfo != null) {
				List<String> islandHover = Lists.newArrayList();
				islandHover.add("§6Ile: §e" + islandInfo.getName());
				islandHover.add("§6Role: " + islandInfo.getRoleName(player.getUniqueId(), true));

				HoverEvent islandHoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, fromLegacy(String.join("\n", islandHover)));
				componentBuilder.event(islandHoverEvent)
						.append("(").color(net.md_5.bungee.api.ChatColor.DARK_GRAY)
						.append(Integer.toString(islandInfo.getLevel())).color(net.md_5.bungee.api.ChatColor.GREEN)
						.append(")").color(net.md_5.bungee.api.ChatColor.DARK_GRAY);
				componentBuilder.append(fromLegacy(" "));
				flatMessage = "§8(§a" + islandInfo.getLevel() + "§8) " + flatMessage;
			}
		}

		BaseComponent[] displayNameComps = TextComponent.fromLegacyText(displayName);
		List<String> displayNameHover = Lists.newArrayList();
		if (power >= 5) {
			displayNameHover.add("§2✔ §aMembre du staff validé.");
		}
		HoverEvent displayNameHoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, fromLegacy(String.join("\n", displayNameHover)));
		for (BaseComponent displayNameComp : displayNameComps) {
			displayNameComp.setHoverEvent(displayNameHoverEvent);
		}
		componentBuilder.append(displayNameComps);
		componentBuilder.append(fromLegacy("§8 ➟ ")).append(TextComponent.fromLegacyText(message));

		final BaseComponent[] finalMessage = componentBuilder.create();

		for (Player online : Bukkit.getOnlinePlayers()) {
			SettingsManager.ChatSetting chatSetting = (SettingsManager.ChatSetting) SettingsManager.CHAT_MESSAGES.getState(online);
			if (chatSetting == SettingsManager.ChatSetting.EXTRA)
				online.spigot().sendMessage(finalMessage);
			else if (chatSetting == SettingsManager.ChatSetting.FLAT)
				online.sendMessage(flatMessage);
		}
	}

	private BaseComponent[] fromLegacy(String legacy) {
		return new ComponentBuilder("").append(TextComponent.fromLegacyText(legacy)).create();
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		LeezSky.scoreboardMap.remove(event.getPlayer().getUniqueId());
	}

}
