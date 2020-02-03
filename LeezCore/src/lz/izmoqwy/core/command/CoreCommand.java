package lz.izmoqwy.core.command;

import com.google.common.collect.Maps;
import lombok.Getter;
import lz.izmoqwy.core.self.LeezCore;
import lz.izmoqwy.core.utils.MathUtil;
import lz.izmoqwy.core.utils.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;

import java.util.Map;
import java.util.UUID;

public abstract class CoreCommand implements CommandExecutor {

	private final String name;

	private final String permission;
	private final boolean playerOnly, needsArg;

	private final int cooldown;
	private Map<UUID, Long> cooldownMap;

	public CoreCommand(String name, CommandOptions options) {
		this.name = name;

		this.permission = options.getPermission();
		this.playerOnly = options.isPlayerOnly();
		this.needsArg = options.isNeedsArg();

		this.cooldown = options.getCooldown();
		if (cooldown > 0)
			this.cooldownMap = Maps.newHashMap();
	}

	public String getName() {
		return name;
	}

	protected void send(CommandSender commandSender, String... message) {
		commandSender.sendMessage(message != null ? getPrefix() + ChatColor.translateAlternateColorCodes('&', String.join(" ", message)) : " ");
	}

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase(name)) {
			if (commandSender instanceof Player) {
				if (permission != null && !commandSender.hasPermission(permission)) {
					send(commandSender, "&cVous n'avez pas la permission requise pour faire cela.");
					return true;
				}

				if (cooldown > 0) {
					UUID uuid = ((Player) commandSender).getUniqueId();
					if (cooldownMap.containsKey(uuid)) {
						long remaining = (cooldown * 1000) - (System.currentTimeMillis() - cooldownMap.get(uuid));
						if (remaining > 0) {
							send(commandSender, "&cVeuillez attendre encore",
									"&e" + MathUtil.roundDecimal(remaining / 1000d, 1) + "s",
									"&cavant de pouvoir faire cette commande de nouveau.");
							return true;
						}
					}
					cooldownMap.put(uuid, System.currentTimeMillis());
				}

			}
			else if (!playerOnly) {
				send(commandSender, "&cCette commande réservée aux joueurs.");
				return true;
			}

			if (needsArg && args.length < 1) {
				send(commandSender, "&cCette commande requiert au minimum 1 argument.");
				return true;
			}

			try {
				execute(commandSender, label, args);
			}
			catch (CommandException ex) {
				send(commandSender, "&c" + ex.getMessage());
			}
			return true;
		}
		return false;
	}

	protected abstract void execute(CommandSender commandSender, String usedCommand, String[] args);

	protected String getPrefix() {
		return LeezCore.PREFIX;
	}

	/*
	Utils
	 */
	protected boolean match(String[] args, int index, String... values) {
		if (args.length <= index)
			return false;

		for (String value : values) {
			if (args[index].equalsIgnoreCase(value))
				return true;
		}
		return false;
	}

	@Contract("false, _ -> fail")
	protected void checkValid(boolean valid, String message) {
		if (!valid)
			throw new CommandException(message);
	}

	@Contract("null, _ -> fail")
	protected void checkNotNull(Object object, String message) {
		if (object == null)
			throw new CommandException(message);
	}

	protected void missingArg(String[] args, int index, String needed) {
		if (args.length <= index)
			throw new CommandException("Argument manquant en position " + (index + 1) + ": " + needed);
	}

	protected Player getTarget(String[] args, int index, String needed) {
		missingArg(args, index, needed);
		Player target = Bukkit.getPlayerExact(args[index]);
		if (target == null)
			throw new CommandException("Le joueur §6" + args[index] + " §cn'éxiste pas ou n'est pas en ligne.");
		return target;
	}

	protected OfflinePlayer getOfflineTarget(String[] args, int index, String needed) {
		missingArg(args, index, needed);
		OfflinePlayer target = PlayerUtil.getOfflinePlayer(args[index]);
		if (target == null)
			throw new CommandException("Le joueur §6" + args[index] + "§c n'éxite pas.");
		return target;
	}

	protected void checkPermission(CommandSender commandSender, String permission) {
		if (!(commandSender instanceof Player))
			return;

		if (!commandSender.hasPermission((this.permission != null ? this.permission : name.toLowerCase()) + "." + permission))
			throw new CommandException("Vous n'avez pas la permission requise pour faire cela.");
	}

}
