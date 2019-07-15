package me.izmoqwy.hardpermissions;

import lz.izmoqwy.core.CorePrinter;
import lz.izmoqwy.core.LeezCore;
import lz.izmoqwy.core.i18n.Locales;
import lz.izmoqwy.core.i18n.i18nLocale;
import org.bukkit.command.CommandSender;

import java.text.MessageFormat;

public enum Locale implements i18nLocale {

	PREFIX(LeezCore.PREFIX, false),

	RELOADED(1, "§aConfiguration rechargée pour LeezPermissions §2v{0}§a."),

	GROUPS_NOGROUP("§cIl n'éxiste aucun grade !"),
	GROUPS_LIST("§3Liste des grades éxistants:"),

	GROUP_CHANGED(3,"§3Le grade de §e{0} §3a été changé par §2{1} §3en §b{2}§3."),
	GROUP_ALRDEXISTS(1, "§cLe grade §6{0} §céxiste déjà !"),
	GROUP_DOESNOTEXISTS(1,"§cLe grade §6{0} §cn'éxiste pas !"),
	GROUP_HASPERM(2,"§cLe grade §b{0} §cpossède déjà la permission §a{1}§c."),
	GROUP_NOPERM(2,"§cLe grade §b{0} §cne possède pas la permission §a{1}§c."),

	GROUP_LISTPERMS_HEADER(1,"§3Liste des permissions du grade §b{0}§3:"),
	GROUP_LISTPERMS_NONE(1,"§cLe grade §6{0} §cn'a pas de permissions."),
	GROUP_LISTPERMS_INHERITANCES_LIST(1,"§3Ainsi que toutes les permissions des grades: {0}§3."),
	GROUP_LISTPERMS_INHERITANCES_COLOR("§b", false),

	GROUP_DELETED(1,"§3Le grade §9{0} §3a été §csupprimé§3."),
	GROUP_CREATED(1,"§3Le grade §9{0} §3a été §acréé§3."),
	GROUP_EDITED(3,"§3Le grade §b{0} §3a été edité, son §2{1} §3est désormais: §a{2}§3."),
	GROUP_PERMADDED(2, "§3Le grade §b{0} §3a été edité, une §apermission a été ajoutée§3: §a{1}§3."),
	GROUP_PERMREMOVED(2,"§3Le grade §b{0} §3a été edité, une §cpermission a été supprimée§3: §a{1}§3."),

	GROUP_GREATERPOWER(1, "§cVous ne pouvez pas attribuer le grade §6{0} §ccar il est supérieur au votre."),
	GROUP_SAMEPOWER(1, "§cVous ne pouvez pas attribuer le grade §6{0} §ccar il est au même niveau que le votre."),

	PLAYER_CANTCHANGE(1, "§cVous ne pouvez pas changer le grade de §6{0}§c."),
	PLAYER_ALRDHAS(2,"§cLe joueur §e{0} §cpossède déjà le grade §b{1}§c."),
	PLAYER_HASGROUP(2,"§3Le joueur §e{0} §3possède le grade §b{1}§3."),
	PLAYER_NOGROUP(1,"§cLe joueur §e{0} §cn'a pas de grade."),
	PLAYER_CHANGEDGROUP(2,"§3Le grade de §e{0} §3est désormais §b{1}§3."),
	PLAYER_NEVERPLAYED(1,"§cLe joueur §e{0} §cn'a jamais joué sur le serveur."),

	NOPERM_SUBCOMMAND("§cVous n'avez pas accès à cette sous-commande !");

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
