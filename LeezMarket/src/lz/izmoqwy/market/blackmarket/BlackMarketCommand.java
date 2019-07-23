package lz.izmoqwy.market.blackmarket;

import lz.izmoqwy.core.api.CommandNoPermissionException;
import lz.izmoqwy.core.api.CommandOptions;
import lz.izmoqwy.core.api.CoreCommand;
import lz.izmoqwy.market.Locale;
import lz.izmoqwy.market.npc.NPC_v1_12_R1;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;

public class BlackMarketCommand extends CoreCommand {

	protected BlackMarketCommand() {
		super("blackmarket", new CommandOptions().playerOnly());
	}

	@Override
	protected void execute(CommandSender commandSender, String usedCommand, String[] args) throws CommandNoPermissionException {
		Player player = (Player) commandSender;

		if (args.length < 1) {
			player.sendMessage(Locale.PREFIX + "§cArgument manquant !");
			return;
		}

		switch (args[0].toLowerCase()) {
			case "movehere":
			case "tphere":
				permCheck(player, "command.movehere");
				if (BlackMarket.NPC == null) {
					BlackMarket.NPC = new NPC_v1_12_R1(BlackMarket.NPC_NAME, player.getLocation(), BlackMarket.config.getString("skin.texture"), BlackMarket.config.getString("skin.signature"));
					BlackMarket.NPC.spawn();
				}
				else {
					BlackMarket.NPC.move(player.getLocation());
				}
				Location location = player.getLocation();
				BlackMarket.spawnArmorStand(location);

				YamlConfiguration config = YamlConfiguration.loadConfiguration(BlackMarket.file);
				config.set("npc.world", location.getWorld().getName());
				config.set("npc.x", Math.floor(location.getX() * 1000) / 1000);
				config.set("npc.y", Math.floor(location.getY() * 1000) / 1000);
				config.set("npc.z", Math.floor(location.getZ() * 1000) / 1000);
				config.set("npc.yaw", Math.floor(location.getYaw() * 10) / 10);
				config.set("npc.pitch", Math.floor(location.getPitch() * 10) / 10);
				try {
					config.save(BlackMarket.file);
					player.sendMessage(Locale.PREFIX + "§aLe PNJ du marché noir vient d'être déplacé.");
				}
				catch (IOException e) {
					e.printStackTrace();
					player.sendMessage(Locale.PREFIX + "§cImpossible de sauvegarder la nouvelle position du PNJ !");
				}
				break;
			case "update":
				permCheck(player, "command.update");
				// Reload the skin of the BM for the commandSender
				if (BlackMarket.NPC != null) {
					BlackMarket.NPC.updateSkin(player);
					player.sendMessage(Locale.PREFIX + "§aVous devriez désormais voir le skin du PNJ du marché noir.");
				}
				else
					player.sendMessage(Locale.PREFIX + "§cLe PNJ du marché noir n'est pas présent.");
				break;
			default:
				player.sendMessage(Locale.PREFIX + "Commande inconnue. Avez-vous essayé l'argument 'help' ?");
				break;
		}
	}
}
