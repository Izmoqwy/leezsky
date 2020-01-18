package lz.izmoqwy.market;

import lz.izmoqwy.core.i18n.i18nLocale;
import lz.izmoqwy.core.self.CorePrinter;
import lz.izmoqwy.core.self.LeezCore;
import lz.izmoqwy.market.rpg.RPGPlayer;
import org.bukkit.command.CommandSender;

import java.text.MessageFormat;

public enum Locale implements i18nLocale {

	PREFIX(LeezCore.PREFIX, false),
	RPG_PREFIX("§3RPG §8» ", false),

	RPG_NO_ENERGY("§cVous n'avez plus d'énergie, revenez dans quelques temps.");

	private final String defaultTr;
	private final boolean needPrefix;
	private final int neededArgs;

	private String writable, message;

	Locale(int neededArgs, String s, boolean needPrefix) {
		this.defaultTr = s;
		this.needPrefix = needPrefix;
		this.neededArgs = neededArgs;

		this.message = ((needPrefix ? LeezCore.PREFIX : "") + this.defaultTr).replace("'", "''");
	}

	Locale(String s) {
		this(0, s, true);
	}

	Locale(String s, boolean needPrefix) {
		this(0, s, needPrefix);
	}

	Locale(int neededArgs, String s) {
		this(neededArgs, s, true);
	}

	public void send(CommandSender sender, Object... arguments) {
		if (neededArgs != arguments.length)
			CorePrinter.warn("Missing arguments on message {0}", getEnumName());
		sender.sendMessage(MessageFormat.format(message, arguments));
	}

	public void send(RPGPlayer player, Object... arguments) {
		if (!player.getBase().isOnline())
			return;
		send(player.getBase().getPlayer(), arguments);
	}

	@Override
	public void set(String newMessage) {
		this.writable = newMessage;
		this.message = ((needPrefix ? (name().startsWith("RPG_") ? RPG_PREFIX : PREFIX) : "") + newMessage).replace("'", "''");
	}

	@Override
	public String toString() {
		return this.message;
	}

	@Override
	public String getWritableMessage() {
		return this.writable == null ? this.defaultTr : this.writable;
	}

	@Override
	public String getEnumName() {
		return super.toString();
	}

}
