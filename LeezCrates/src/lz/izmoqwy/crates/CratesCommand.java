package lz.izmoqwy.crates;

import lz.izmoqwy.core.command.CommandOptions;
import lz.izmoqwy.core.command.CoreCommand;
import lz.izmoqwy.core.utils.TextUtil;
import lz.izmoqwy.crates.objects.Crate;
import lz.izmoqwy.crates.objects.CrateType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;

public class CratesCommand extends CoreCommand {

	public CratesCommand() {
		super("leezcrates", CommandOptions.builder()
				.permission("leezcrates.command").playerOnly(true).needsArg(true)
				.build());
	}

	@Override
	protected void execute(CommandSender commandSender, String usedCommand, String[] args) {
		Player player = (Player) commandSender;
		switch (args[0].toLowerCase()) {
			case "types":
				checkPermission(commandSender, "types");

				send(player, "§3Types de box disponibles: §b" +
						TextUtil.iterate(LeezCrates.getCrateTypes(), CrateType::getDisplayName, "§b", "§3, ")
						+ "§3.");
				break;
			case "create":
				checkPermission(commandSender, "create");

				if (args.length < 2) {
					player.sendMessage(getPrefix() + "§cArgument requis: Type.");
					return;
				}

				CrateType crateType = LeezCrates.fromStringType(args[1]);
				checkNotNull(crateType, "Type de box invalide !");

				LeezCrates.createCrate(null, crateType, player.getLocation(), true);
				send(player, "§aLa box §2" + crateType.getName() + " §aa été créée.");
				break;
			case "remove":
				checkPermission(commandSender, "remove");

				Block block = player.getTargetBlock(null, 10);
				if (block != null && LeezCrates.getCrates().containsKey(block.getLocation())) {
					final Crate crate = LeezCrates.getCrates().get(block.getLocation());
					LeezCrates.getCrates().remove(block.getLocation());

					crate.getHologram().removeCurrentAS();
					block.setType(Material.AIR);

					YamlConfiguration yaml = YamlConfiguration.loadConfiguration(LeezCrates.getCratesFile());
					yaml.set(crate.getId(), null);
					try {
						yaml.save(LeezCrates.getCratesFile());
						player.sendMessage(getPrefix() + "§aLa box §2#" + crate.getId() + " §avient d'être §2supprimée§a.");
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}
				else {
					player.sendMessage(getPrefix() + "§cVous devez regarder une box (à 10 blocs de vous maximum) !");
				}

				break;
			default:
				commandSender.sendMessage(getPrefix() + "§cArgument invalide. §7(types, create, remove)");
				break;
		}
	}

	@Override
	protected String getPrefix() {
		return LeezCrates.PREFIX;
	}

}
