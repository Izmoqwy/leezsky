package lz.izmoqwy.leezisland;

import lz.izmoqwy.core.CorePrinter;
import lz.izmoqwy.core.LeezCore;
import lz.izmoqwy.core.i18n.Locales;
import lz.izmoqwy.core.i18n.i18nLocale;
import lz.izmoqwy.leezisland.players.SkyblockPlayer;
import org.bukkit.command.CommandSender;

import java.text.MessageFormat;

public enum Locale implements i18nLocale {

	PREFIX(LeezCore.PREFIX, false),

	COMMAND_UNKOWN("§cCommande inconnue. Avez-vous essayé l'argument 'help'?"),
	COMMAND_INVALID("§cArgument(s) invalide(s) ou incorrect(s) !"),
	COMMAND_PLAYERONLY("§cCette commande est reservée aux joueurs."),
	COMMAND_ERROR("§4ERR: §cUne erreur est survenue pendant l'éxécution de cette commande"),
	COMMAND_NOPERM("§cVous n'avez pas la permission d'utiliser cette sous-commande !"),

	COMMAND_ARGUMENTS_TOOFEW(1, "§cArgument manquant ({0})"),

	ARGUMENT_NOTINT("§cMerci de spéficier un nombre entier valide."),
	ARGUMENT_PLAYER("§cVeuilez spécifier un joueur."),

	ADMIN_BYPASS_TOGGLEDON("§aVous pouvez désormais intéragir sur les îles joueurs."),
	ADMIN_BYPASS_TOGGLEDOFF("§2Vous ne pouvez plus intéragir sur les îles joueurs."),

	ACTION_CONFIRM_DONE("§aAction confirmée, traitement en cours..."),
	ACTION_CANCEL_DONE("§2Action annulée."),
	ACTION_CONFIRM_NONE("§cVous n'avez aucune action à confirmer."),
	ACTION_CANCEL_NONE("§cVous n'avez aucune action à annuler."),
	ACTION_SENDER_DISCONNECTED("§4ERR: §cLa personne vous ayant envoyé cela n'est plus connectée."),

	PLAYER_NOTINWORLD("§cVous n'êtes pas dans le monde skyblock !"),
	PLAYER_NOTONISLAND("§cIl n'y a pas d'île là où vous êtes."),

	PLAYER_ISLAND_CREATE_ALREADYHAS("§cVous avez déjà une île."),
	PLAYER_ISLAND_CREATE_WAITING("§6Votre île est en attente de création, veuillez patienter...."),
	PLAYER_ISLAND_CREATE_STARTING("§eVotre île est en préparation, veuillez patienter..."),
	PLAYER_ISLAND_CREATE_FILLINGCHEST("§eRemplissage de votre coffre de départ..."),
	PLAYER_ISLAND_CREATE_NOSCHEMATIC("§cUne erreur est survenue lors du chargemetn des blocs de votre île ! Contactez un administrateur, il sera en mesure de vous les faire apparaitre manuellement."),
	PLAYER_ISLAND_CREATE_FINISHED("§aVotre île est enfin prête !"),

	PLAYER_ISLAND_NONE("§6Vous n'avez pas encore d'île, faîtes '/is create' pour en créer une."),
	PLAYER_ISLAND_TELEPORT("§eTéléporation sur votre île..."),
	PLAYER_ISLAND_NOTONISLAND("§cVous n'êtes sur votre île !"),

	PLAYER_ISLAND_LEVEL_STARTING("§eCalcul du niveau de votre île..."),
	PLAYER_ISLAND_LEVEL_FINISHED(3, "§aVotre île est niveau §2{0}{1}§a. §7(Vous êtes à §8{2}xp §7du prochain niveau)"),
	PLAYER_ISLAND_LEVEL_RANK(1, "§3Votre île est §b#{0} §3du classement."),
	PLAYER_ISLAND_LEVEL_INDICATION_INCREASED("§2⬆", false),
	PLAYER_ISLAND_LEVEL_INDICATION_DECREASED("§4⬇", false),
	PLAYER_ISLAND_LEVEL_INDICATION_SAME("§8⬍", false),

	PLAYER_ISLAND_RANK_TOOLOW(1, "§cVotre rang d'île est trop bas pour faire cela ! Il vous faut au minimum le rang {0}."),

	BAN_TARGET_ISADMIN("§cVous n'avez pas la permission de bannir ce joueur !"),

	TARGET_YOUSELF("§cVous ne pouvez pas vous cibler vous-même !"),
	TARGET_BYPASSING("§cCe joueur est en mode contournement, vous n'avez pas le droit de faire cela !"),
	TARGET_NOTEXISTS("§cCe joueur n'éxiste pas !"),
	TARGET_DISCONNECTED("§cCe joueur n'est pas connecté."),
	TARGET_NEORDISCONNECT("§cCe joueur n'éxiste pas et/ou n'est pas connecté !"),

	TARGET_NOISLAND("§cCe joueur ne possède pas d'île."),
	TARGET_HASISLAND("§cCe joueur est déjà dans une île."),
	TARGET_NOTONMYISLAND("§cCe joueur n'est pas présent sur votre île !"),

	TEAM_JOIN_JOINED_NAMED(1, "§aVous avez rejoint l'île §2{0}§a."),
	TEAM_JOIN_JOINED_NONAME(1, "§aVous avez rejoint l'île de §2{0}§a."),

	TEAM_TARGET_ISMEMBER("§cCe joueur est un membre de votre île !"),
	TEAM_TARGET_NOTMEMBER("§cCe joueur n'est pas un membre de votre île !"),
	TEAM_TARGET_LEADER("§cCe joueur est le chef de votre île !"),
	TEAM_TARGET_SUPERIOR("§cCe joueur possède un rôle supérieur ou égal au votre !"),

	GUARD_LOCKED("§cCette île est fermée aux joueurs."),
	GUARD_BANNED("§cVous êtes banni de cette île !"),
	GUARD_ENTER_NONAME(1, "§3Vous entrez sur l'île de §b{0}§3."),
	GUARD_ENTER_NAMED(1, "§3Vous entrez sur l'île §b{0}§3."),
	GUARD_ISLAND_NOFLY("§cVoler est interdit sur cette île, votre fly a été désactivé.");

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

	public void send(SkyblockPlayer player, Object... arguments) {
		this.send(player.bukkit(), arguments);
	}

	@Override
	public void set(String newMessage) {
		this.saveable = newMessage;
		this.message = ((needPrefix ? LeezCore.PREFIX : "") + newMessage).replace("'", "''");
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
