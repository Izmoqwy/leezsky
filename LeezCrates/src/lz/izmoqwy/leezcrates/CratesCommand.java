package lz.izmoqwy.leezcrates;

import lz.izmoqwy.core.api.CommandNoPermissionException;
import lz.izmoqwy.core.api.CommandOptions;
import lz.izmoqwy.core.api.CoreCommand;
import lz.izmoqwy.core.utils.TextUtil;
import lz.izmoqwy.leezcrates.objects.Crate;
import lz.izmoqwy.leezcrates.objects.CrateType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;

public class CratesCommand extends CoreCommand {

	final String PREFIX = LeezCrates.PREFIX;

	public CratesCommand() {
		super("leezcrates", new CommandOptions().playerOnly().withPermission("leezcrates.admin").needArg());
	}

	@Override
	protected void execute(CommandSender commandSender, String usedCommand, String[] args) throws CommandNoPermissionException {
		Player player = (Player) commandSender;
		switch(args[0].toLowerCase()) {
			case "types":
				permCheck(commandSender, "types");
				player.sendMessage(PREFIX + "§3Types de box disponibles: §b" + TextUtil.iterate(LeezCrates.getCrateTypes(), CrateType::getDisplayName, "§b", "§3, ") + "§3.");
				break;
			case "create":
				permCheck(commandSender, "create");
				if (args.length < 2) {
					player.sendMessage(PREFIX + "§cArgument requis: Type.");
					return;
				}

				CrateType crateType = LeezCrates.fromStringType(args[1]);
				if (crateType == null) {
					player.sendMessage(PREFIX + "§cType de box invalide !");
					return;
				}

				LeezCrates.createCrate(null, crateType, player.getLocation(), true);
				player.sendMessage(PREFIX + "§aUne box §2" + crateType.getName() + " §aa été ajoutée.");
				break;
			case "remove":
				permCheck(commandSender, "remove");

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
						player.sendMessage(PREFIX + "§aLa box §2#" + crate.getId() + " §avient d'être §2supprimée§a.");
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}
				else {
					player.sendMessage(PREFIX + "§cVouz devez regarder une box (A 10 blocs de vous maximum) !");
				}

				break;
			default:
				commandSender.sendMessage(PREFIX + "§cArgument invalide. §7[types, create, remove]");
				break;
		}
	}
}
