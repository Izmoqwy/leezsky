package lz.izmoqwy.market.blackmarket;

import com.google.common.collect.Lists;
import lz.izmoqwy.core.command.CommandOptions;
import lz.izmoqwy.core.command.CoreCommand;
import lz.izmoqwy.core.utils.LocationUtil;
import lz.izmoqwy.core.utils.TextUtil;
import lz.izmoqwy.market.Locale;
import lz.izmoqwy.market.blackmarket.illegal.ForbiddenArena;
import lz.izmoqwy.market.npc.NPC_v1_12_R1;
import lz.izmoqwy.market.rpg.RPGManager;
import lz.izmoqwy.market.rpg.RPGResource;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.List;

public class BlackMarketCommand extends CoreCommand {

	protected BlackMarketCommand() {
		super("blackmarket", CommandOptions.builder()
				.permission("blackmarket.command").playerOnly(true).needsArg(true)
				.build());
	}

	@Override
	protected void execute(CommandSender commandSender, String usedCommand, String[] args) {
		Player player = (Player) commandSender;

		switch (args[0].toLowerCase()) {
			case "movehere":
			case "tphere":
				checkPermission(player, "movehere");

				YamlConfiguration config = YamlConfiguration.loadConfiguration(BlackMarket.file);
				if (BlackMarket.NPC == null) {
					String useSkin = config.getString("skin.current", "default");
					BlackMarket.NPC = new NPC_v1_12_R1(BlackMarket.NPC_NAME, player.getLocation(), config.getString("skins." + useSkin + ".texture"), config.getString("skins." + useSkin + ".signature"));
					BlackMarket.NPC.spawn();
				}
				else {
					BlackMarket.NPC.move(player.getLocation());
				}
				Location location = player.getLocation();
				BlackMarket.spawnArmorStands(location);

				LocationUtil.saveInYaml(config, location, "npc");
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
				checkPermission(player, "setwarp");

				location = player.getLocation();
				config = YamlConfiguration.loadConfiguration(BlackMarket.file);
				LocationUtil.saveInYaml(config, location, "warp");
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
			case "arena":
				checkPermission(player, "arena");

				if (args.length < 2) {
					player.sendMessage(Locale.PREFIX + "§cArguments manquants ! §7[jon, setspawn, setpoint, removepoint, listpoints]");
					return;
				}

				final String path = "forbiddenarena.";
				switch (args[1].toLowerCase()) {
					case "join":
						checkPermission(player, "arena.join");

						if (ForbiddenArena.join(player, true)) {
							RPGManager.take(player, RPGResource.DARKMATTER, 1000);
						}
						break;
					case "setspawn":
						checkPermission(player, "arena.setspawn");

						location = player.getLocation();
						config = YamlConfiguration.loadConfiguration(BlackMarket.file);
						LocationUtil.saveInYaml(config, location, path + "spawn");
						try {
							config.save(BlackMarket.file);
							BlackMarket.refreshForbiddenArena();
							player.sendMessage(Locale.PREFIX + "§aLe spawnn de l'arène interdite vient d'être défini à votre position.");
						}
						catch (IOException e) {
							e.printStackTrace();
							player.sendMessage(Locale.PREFIX + "§cImpossible de sauvegarder la nouvelle position de l'arène interdite !");
						}
						break;
					case "setpoint":
						checkPermission(player, "arena.setpoint");

						if (args.length != 3 || ChatColor.stripColor(args[2].toLowerCase()).trim().isEmpty()) {
							player.sendMessage(Locale.PREFIX + "§cVeuillez spécifier le nom du point d'apparition.");
							return;
						}
						String point = ChatColor.stripColor(args[2].toLowerCase());

						location = player.getLocation();
						config = YamlConfiguration.loadConfiguration(BlackMarket.file);
						LocationUtil.saveInYaml(config, location, path + "points." + point);
						try {
							config.save(BlackMarket.file);
							BlackMarket.refreshForbiddenArena();
							player.sendMessage(Locale.PREFIX + "§aLe point d'apparition §2" + point + "§a de l'arène interdite vient d'être défini à votre position.");
						}
						catch (IOException e) {
							e.printStackTrace();
							player.sendMessage(Locale.PREFIX + "§cImpossible de sauvegarder ce nouveau point d'apparition !");
						}
						break;
					case "removepoint":
						checkPermission(player, "arena.setpoint");

						if (args.length != 3 || ChatColor.stripColor(args[2].toLowerCase()).trim().isEmpty()) {
							player.sendMessage(Locale.PREFIX + "§cVeuillez spécifier le nom du point d'apparition.");
							return;
						}
						point = ChatColor.stripColor(args[2].toLowerCase());

						config = YamlConfiguration.loadConfiguration(BlackMarket.file);
						config.set(path + "points." + point, null);
						try {
							config.save(BlackMarket.file);
							BlackMarket.refreshForbiddenArena();
							player.sendMessage(Locale.PREFIX + "§2Le point d'apparition §e" + point + "§2 de l'arène interdite vient d'être supprimé.");
						}
						catch (IOException e) {
							e.printStackTrace();
							player.sendMessage(Locale.PREFIX + "§cImpossible de supprimer ce point d'apparition !");
						}
						break;
					case "listpoints":
						checkPermission(player, "arena.listpoints");
						config = BlackMarket.config;

						List<String> points = Lists.newArrayList();
						ConfigurationSection section = config.getConfigurationSection(path + "points");
						if (section != null) {
							points.addAll(section.getKeys(false));
						}

						if (!points.isEmpty()) {
							player.sendMessage(Locale.PREFIX + "§3Points d'apparitions de l'arène interdite: " + TextUtil.iterate(points, p -> p, "§b", "§3, ") + "§3.");
						}
						else {
							player.sendMessage(Locale.PREFIX + "§cIl n'y a aucun point d'apparition pour l'arène interdite, le point de spawn sera utilisé.");
						}
						break;
				}
				break;
			case "warp":
			case "tp":
				checkPermission(player, "warp");
				if (BlackMarket.TELEPORT_POINT == null) {
					player.sendMessage(Locale.PREFIX + "§cIl n'y a pas de warp défini pour le marché noir !");
					return;
				}

				player.sendMessage(Locale.PREFIX + "§eTéléportation au marché noir...");
				player.teleport(BlackMarket.TELEPORT_POINT);
				break;
			case "fix":
				checkPermission(player, "fix");
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
				checkPermission(player, "reload");

				if (BlackMarket.NPC != null) {
					BlackMarket.NPC.despawn();
				}
				if (BlackMarket.reload()) {
					player.sendMessage(Locale.PREFIX + "§aLa configuration du marché noir a été rechargée.");
				}
				else {
					player.sendMessage(Locale.PREFIX + "§cLa configuration du marché noir n'a pas pu être rechargée !");
				}
				break;
			case "update":
				checkPermission(player, "update");

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
