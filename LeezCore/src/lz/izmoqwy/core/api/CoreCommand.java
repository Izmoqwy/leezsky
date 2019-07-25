package lz.izmoqwy.core.api;

import com.google.common.collect.Maps;
import lz.izmoqwy.core.LeezCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public abstract class CoreCommand implements CommandExecutor {

	public static final String PREFIX = LeezCore.PREFIX;

	private String name;
	private String permission;
	private boolean playerOnly;
	private boolean needArg;

	private int cooldown;
	private Map<UUID, Long> cooldowns = Maps.newHashMap();

	public CoreCommand(String name, CommandOptions options) {
		this.name = name;
		this.permission = options.permission;
		this.playerOnly = options.playerOnly;
		this.needArg = options.needsArg;

		this.cooldown = options.cooldown;
	}

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String usedCommand, String[] args) {
		if (command.getName().equalsIgnoreCase(name)) {
			if (commandSender instanceof Player) {
				if (permission != null && !commandSender.hasPermission(permission)) {
					commandSender.sendMessage(LeezCore.PREFIX + "§cVous n'avez pas la permission requise pour faire cela.");
				}
				else {
					UUID uuid = ((Player) commandSender).getUniqueId();
					if (cooldowns.containsKey(uuid)) {
						long remaining = (cooldown * 1000) - (System.currentTimeMillis() - cooldowns.get(uuid));
						if (remaining > 0) {
							commandSender.sendMessage(LeezCore.PREFIX + "§cVeuillez attendre encore §e" + (Math.floor(remaining / 100) / 10) + "s §cavant de faire cette commande à nouveau.");
						}
						else {
							run(commandSender, usedCommand, args);
							cooldowns.replace(uuid, System.currentTimeMillis());
						}
					}
					else {
						run(commandSender, usedCommand, args);
						cooldowns.put(uuid, System.currentTimeMillis());
					}
				}
			}
			else {
				if (!playerOnly)
					run(commandSender, usedCommand, args);
				else
					commandSender.sendMessage("§cCommande reservée aux joueurs !");
			}
		}
		return false;
	}

	private void run(CommandSender commandSender, String usedCommand, String[] args) {
		if (needArg && args.length < 1) {
			commandSender.sendMessage(PREFIX + "§cArgument manquant. Essayez l'argument 'help' si vous ne connaissez pas les arguments possibles.");
			return;
		}
		try {
			execute(commandSender, usedCommand, args);
		}
		catch (CommandNoPermissionException ex) {
			commandSender.sendMessage(PREFIX + "§cVous n'avez pas la permission de faire cette commande !");
		}
	}

	protected abstract void execute(CommandSender commandSender, String usedCommand, String[] args) throws CommandNoPermissionException;

	protected void permCheck(CommandSender commandSender, String permission) throws CommandNoPermissionException {
		permCheck(commandSender, permission, false);
	}

	protected void permCheck(CommandSender commandSender, String permission, boolean full) throws CommandNoPermissionException {
		if (!(commandSender instanceof Player))
			return;

		if (!full)
			permission = (this.permission != null ? this.permission : name.toLowerCase()) + "." + permission;
		if(!commandSender.hasPermission(permission))
			throw new CommandNoPermissionException();
	}

	protected static boolean c(String arg, String... possibilites) {
		for (String s : possibilites) {
			if(arg.equalsIgnoreCase(s))
				return true;
		}
		return false;
	}

}
