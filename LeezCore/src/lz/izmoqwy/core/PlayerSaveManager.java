package lz.izmoqwy.core;

import lz.izmoqwy.core.api.PlayerBackup;
import lz.izmoqwy.core.self.LeezCore;
import lz.izmoqwy.core.utils.ServerUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;

public class PlayerSaveManager {

	public static void load(){
		ServerUtil.registerListeners(LeezCore.instance, new Listener() {
			// Call the event almost at the end due to message ordering
			@EventHandler(priority = EventPriority.HIGH)
			public void onPlayerJoin(PlayerJoinEvent event) {
				if (getFile(event.getPlayer()).exists()) {
					Player player = event.getPlayer();
					player.sendMessage(" ");
					player.sendMessage(" ");
					player.sendMessage(" ");
					player.sendMessage(LeezCore.PREFIX + "§5Vous n'avez pas récupérer votre inventaire correctement lors de la dernière sauvegarde, faîtes §d'/leez backup' §5pour récupérer votre inventaire.");
					player.sendMessage(" ");
				}
			}
		});
	}

	public static File getFile(OfflinePlayer player) {
		return new File(LeezCore.instance.getDataFolder(), "backups/" + player.getUniqueId().toString() + ".yml");
	}

	public static boolean restore(PlayerBackup backup, Player player, boolean teleport) {
		File file = getFile(player);
		backup.restore(player, teleport);
		return file.delete();
	}

}
