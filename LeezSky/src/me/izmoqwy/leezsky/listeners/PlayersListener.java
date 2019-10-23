package me.izmoqwy.leezsky.listeners;

import lz.izmoqwy.core.crosshooks.CrosshooksManager;
import lz.izmoqwy.core.crosshooks.interfaces.Group;
import lz.izmoqwy.core.crosshooks.interfaces.LeezIslandCH;
import lz.izmoqwy.core.crosshooks.interfaces.LeezPermissionsCH;
import lz.izmoqwy.core.nms.NmsAPI;
import lz.izmoqwy.core.utils.TitleUtil;
import me.izmoqwy.leezsky.LeezSky;
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
			player.sendMessage(helper + "§eTu te trouve actuellement sur un serveur skyblock simple. Un skyblock simple est un skyblock où les joueurs progresse plus rapidement que la normale.");
			player.sendMessage(helper + "§eUtilise la commande §6/settings §epour configurer tes paramètres de base et §6/help §epour obtenir de l'aide sur le serveur et/ou les commandes.");
			player.sendMessage("§d§oBesoin de plus d'informations sur le skyblock en général ? Fais §5/help skyblock§d§o.");

			player.sendMessage("");
		}
		LeezSky.createScoreboard(player);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onAsyncChat(AsyncPlayerChatEvent event) {
		event.setCancelled(true);

		Player player = event.getPlayer();
		String message = event.getMessage();
		if (player.hasPermission("leezsky.chat.color")) {
			message = message.replaceAll("&([\\da-f])", "§$1");
		}

		String displayName = player.getDisplayName();
		ChatColor chatcolor = ChatColor.DARK_GRAY;
		if (CrosshooksManager.isPluginRegistred("LeezPermissions")) {
			LeezPermissionsCH permissions = CrosshooksManager.get("LeezPermissions", LeezPermissionsCH.class);
			if (permissions != null) {
				Group group = permissions.getGroup(player);

				displayName = ChatColor.translateAlternateColorCodes('&', group.getPrefix() + player.getName() + group.getSuffix());
				chatcolor = group.getChatColor();
			}
		}

		if (!message.startsWith("§"))
			message = chatcolor + message;

		LeezIslandCH CH = CrosshooksManager.isPluginRegistred("LeezIsland") ? CrosshooksManager.get("LeezIsland", LeezIslandCH.class) : null;
		int islandLevel = !player.hasPermission("leezsky.chat.nolevel") && CH != null ? CH.getIslandLevel(player) : -1;
		Bukkit.broadcastMessage(MessageFormat.format("{0}{1} {2}➟ {3}", islandLevel != -1 ? "§8(§a" + islandLevel + "§8) " : "", displayName, ChatColor.DARK_GRAY, message));
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		LeezSky.scoreboardMap.remove(event.getPlayer().getUniqueId());
	}

}
