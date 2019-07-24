package lz.izmoqwy.market.blackmarket;

import lz.izmoqwy.core.api.CommandNoPermissionException;
import lz.izmoqwy.core.api.CommandOptions;
import lz.izmoqwy.core.api.CoreCommand;
import lz.izmoqwy.core.utils.LocationUtil;
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
				BlackMarket.spawnArmorStands(location);

				YamlConfiguration config = YamlConfiguration.loadConfiguration(BlackMarket.file);
				LocationUtil.yamlFullSave(config, location, "npc");
				try {
					config.save(BlackMarket.file);
					player.sendMessage(Locale.PREFIX + "§aLe PNJ du marché noir vient d'être déplacé.");
				}
				catch (IOException e) {
					e.printStackTrace();
					player.sendMessage(Locale.PREFIX + "§cImpossible de sauvegarder la nouvelle position du PNJ !");
				}
				break;
			case "setwarp":
				permCheck(player, "command.setspawn");

				location = player.getLocation();
				config = YamlConfiguration.loadConfiguration(BlackMarket.file);
				LocationUtil.yamlFullSave(config, location, "warp");
				try {
					config.save(BlackMarket.file);
					BlackMarket.TELEPORT_POINT = location;
					player.sendMessage(Locale.PREFIX + "§aLe warp du marché noir vient d'être défini à votre position.");
				}
				catch (IOException e) {
					e.printStackTrace();
					player.sendMessage(Locale.PREFIX + "§cImpossible de sauvegarder la nouvelle position du warp !");
				}
				break;
			case "warp":
			case "tp":
				permCheck(player, "command.warp");
				if (BlackMarket.TELEPORT_POINT == null) {
					player.sendMessage(Locale.PREFIX + "§cIl n'y a pas de warp défini pour le marché noir !");
					return;
				}

				player.sendMessage(Locale.PREFIX + "§eTéléportation au marché noir...");
				player.teleport(BlackMarket.TELEPORT_POINT);
				break;
			case "fix":
				permCheck(player, "command.fix");
				if (BlackMarket.NPC == null) {
					player.sendMessage(Locale.PREFIX + "§cLe PNJ du marché noir n'est pas présent.");
					return;
				}

				location = BlackMarket.NPC.getLocation();
				BlackMarket.forceRemoveArmorStands(location);
				BlackMarket.armorStands = null;
				BlackMarket.armorStandsIds = null;
				BlackMarket.spawnArmorStands(location);

				player.sendMessage(Locale.PREFIX + "§eLe PNJ devrait désormais être cliquable à nouveau.");
				break;
			case "reload":
				permCheck(player, "command.reload");

				if (BlackMarket.reload()) {
					player.sendMessage(Locale.PREFIX + "§aLa configuration du marché noir a été rechargée.");
				}
				else {
					player.sendMessage(Locale.PREFIX + "§cLa configuration du marché noir n'a pas pu être rechargée !");
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
