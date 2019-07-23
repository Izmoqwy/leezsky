package lz.izmoqwy.market;

import lz.izmoqwy.core.CorePrinter;
import lz.izmoqwy.core.LeezCore;
import lz.izmoqwy.core.i18n.Locales;
import lz.izmoqwy.core.i18n.i18nLocale;
import org.bukkit.command.CommandSender;

import java.text.MessageFormat;

public enum Locale implements i18nLocale {

	PREFIX(LeezCore.PREFIX, false),

	;

	final String defaultTr;
	final boolean needPrefix;
	final int neededArgs;

	String saveable;
	String message;

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

	@Override
	public void set(String newMessage) {
		this.saveable = newMessage;
		this.message = ((needPrefix ? PREFIX : "") + newMessage).replace("'", "''");
	}

	@Override
	public String toString() {
		return this.message;
	}

	@Override
	public Locales getDefaultLocale() {
		return Locales.FRENCH;
	}

	@Override
	public String getSavableMessage() {
		return this.saveable == null ? this.defaultTr : this.saveable;
	}

	@Override
	public String getEnumName() {
		return super.toString();
	}

}
