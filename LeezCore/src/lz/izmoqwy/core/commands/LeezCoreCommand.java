package lz.izmoqwy.core.commands;

import lz.izmoqwy.core.LeezCore;
import lz.izmoqwy.core.PlayerSaveManager;
import lz.izmoqwy.core.api.CommandNoPermissionException;
import lz.izmoqwy.core.api.CommandOptions;
import lz.izmoqwy.core.api.CoreCommand;
import lz.izmoqwy.core.api.PlayerBackup;
import lz.izmoqwy.core.i18n.LocaleManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

public class LeezCoreCommand extends CoreCommand {

	public LeezCoreCommand() {
		super("leezcore", new CommandOptions().withPermission("leezsky.core.command").needArg());
	}

	@Override
	protected void execute(CommandSender commandSender, String usedCommand, String[] args) throws CommandNoPermissionException {
		final boolean isPlayer = commandSender instanceof Player;
		switch(args[0].toLowerCase()) {
			case "backup":
				if (!isPlayer) {
					commandSender.sendMessage(LeezCore.PREFIX + "§cSeul un joueur peut restorer sa propre backup !");
					return;
				}
				File file = PlayerSaveManager.getFile((OfflinePlayer) commandSender);
				if (file.exists()) {
					PlayerBackup backup = PlayerBackup.fromYaml(YamlConfiguration.loadConfiguration(file), null);
					if (!file.delete()) {
						commandSender.sendMessage(LeezCore.PREFIX + "§cImpossible de supprimer le fichier de backup, pour prévenir tout bug vous ne pouvez pas restorer votre inventaire.");
						return;
					}
					backup.restore((Player) commandSender, true);
					commandSender.sendMessage(LeezCore.PREFIX + "§aBackup restorée. Nous sommes désolé pour la gêne occasionée.");
				}
				else {
					commandSender.sendMessage(LeezCore.PREFIX + "§2Vous n'avez aucune backup à restorer.");
				}
				break;
			case "reload":
			case "rl":
				permCheck(commandSender, "admin.reload");
				LocaleManager.reloadMessages();
				commandSender.sendMessage(LeezCore.PREFIX + "§aMessages rechargés.");
				break;
			case "help":
			case "?":
				commandSender.sendMessage(" ");
				commandSender.sendMessage(LeezCore.PREFIX + "§3Aide pour la commande /leezcore:");
				if (isPlayer) {
					commandSender.sendMessage("§6/leezcore §ebackup §8- §eRestorer votre inventaire en cas de problème avec un event");
				}
				if (!isPlayer || commandSender.hasPermission("leezsky.core.command.admin")) {
					if (isPlayer)
						commandSender.sendMessage(" ");
					commandSender.sendMessage("§6/leezcore §ereload §8- §eRecharger les messages des plugins utilisant LeezCore.");
				}
				commandSender.sendMessage(" ");
				break;
			default:
				commandSender.sendMessage(LeezCore.PREFIX + "§cSous-commande inconnue. Faîtes '/leezcore help' pour obtenir la liste des commandes.");
		}
	}
}
