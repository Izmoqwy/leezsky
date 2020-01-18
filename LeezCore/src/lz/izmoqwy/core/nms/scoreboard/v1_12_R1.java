package lz.izmoqwy.core.nms.scoreboard;

import net.minecraft.server.v1_12_R1.*;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zyuiop
 * Updated by troplolBE
 */
public class v1_12_R1 extends NMSScoreboard {

	private boolean created = false;
	private final VirtualTeam[] lines = new VirtualTeam[15];

	public v1_12_R1(Player player, String objectiveName) {
		super(player, objectiveName);
	}

	/**
	 * Send the initial creation packets for this scoreboard sign. Must be called at least once.
	 */
	public void create() {
		if (created)
			return;

		PlayerConnection player = getCon();
		player.sendPacket(createObjectivePacket(0, objectiveName));
		player.sendPacket(setObjectiveSlot());
		int i = 0;
		while (i < lines.length)
			sendLine(i++);

		created = true;
	}

	/**
	 * Send the packets to remove this scoreboard sign. A destroyed scoreboard sign must be recreated using {@link #create()} in order
	 * to be used again
	 */
	public void destroy() {
		if (!created)
			return;

		getCon().sendPacket(createObjectivePacket(1, null));
		for (VirtualTeam team : lines)
			if (team != null)
				getCon().sendPacket(team.removeTeam());

		created = false;
	}

	/**
	 * Change the name of the objective. The name is displayed at the top of the scoreboard.
	 *
	 * @param name the name of the objective, max 32 char
	 */
	public void setObjectiveName(String name) {
		this.objectiveName = name;
		if (created)
			getCon().sendPacket(createObjectivePacket(2, name));
	}

	/**
	 * Change a scoreboard line and send the packets to the player. Can be called async.
	 *
	 * @param line  the number of the line (0 <= line < 15)
	 * @param value the new value for the scoreboard line
	 */
	public void setLine(int line, String value) {
		VirtualTeam team = getOrCreateTeam(line);
		String old = team.getCurrentPlayer();

		if (old != null && created)
			getCon().sendPacket(removeLine(old));

		team.setValue(value);
		sendLine(line);
	}

	/**
	 * Remove a given scoreboard line
	 *
	 * @param line the line to remove
	 */
	public void removeLine(int line) {
		VirtualTeam team = getOrCreateTeam(line);
		String old = team.getCurrentPlayer();

		if (old != null && created) {
			getCon().sendPacket(removeLine(old));
			getCon().sendPacket(team.removeTeam());
		}

		lines[line] = null;
	}

	/**
	 * Get the current value for a line
	 *
	 * @param line the line
	 * @return the content of the line
	 */
	public String getLine(int line) {
		if (line > 14)
			return null;
		if (line < 0)
			return null;
		return getOrCreateTeam(line).getValue();
	}

	/**
	 * Get the team assigned to a line
	 *
	 * @return the {@link VirtualTeam} used to display this line
	 */
	public VirtualTeam getTeam(int line) {
		if (line > 14)
			return null;
		if (line < 0)
			return null;
		return getOrCreateTeam(line);
	}

	private PlayerConnection getCon() {
		return ((CraftPlayer) player).getHandle().playerConnection;
	}

	@SuppressWarnings("rawtypes")
	private void sendLine(int line) {
		if (line > 14)
			return;
		if (line < 0)
			return;
		if (!created)
			return;

		//int score = (15 - line);
		VirtualTeam val = getOrCreateTeam(line);
		for (Packet packet : val.sendLine())
			getCon().sendPacket(packet);
		getCon().sendPacket(sendScore(val.getCurrentPlayer(), line));
		val.reset();
	}

	private VirtualTeam getOrCreateTeam(int line) {
		if (lines[line] == null)
			lines[line] = new VirtualTeam("__fakeScore" + line);

		return lines[line];
	}

	/*
		Factories
		 */
	private PacketPlayOutScoreboardObjective createObjectivePacket(int mode, String displayName) {
		PacketPlayOutScoreboardObjective packet = new PacketPlayOutScoreboardObjective();
		// Nom de l'objectif
		setField(packet, "a", player.getName());

		// Mode
		// 0 : créer
		// 1 : Supprimer
		// 2 : Mettre à jour
		setField(packet, "d", mode);

		if (mode == 0 || mode == 2) {
			setField(packet, "b", displayName);
			setField(packet, "c", IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER);
		}

		return packet;
	}

	private PacketPlayOutScoreboardDisplayObjective setObjectiveSlot() {
		PacketPlayOutScoreboardDisplayObjective packet = new PacketPlayOutScoreboardDisplayObjective();
		// Slot
		setField(packet, "a", 1);
		setField(packet, "b", player.getName());

		return packet;
	}

	private PacketPlayOutScoreboardScore sendScore(String line, int score) {
		PacketPlayOutScoreboardScore packet = new PacketPlayOutScoreboardScore(line);
		setField(packet, "b", player.getName());
		setField(packet, "c", score);
		setField(packet, "d", PacketPlayOutScoreboardScore.EnumScoreboardAction.CHANGE);

		return packet;
	}

	private PacketPlayOutScoreboardScore removeLine(String line) {
		return new PacketPlayOutScoreboardScore(line);
	}

	/**
	 * This class is used to manage the content of a line. Advanced users can use it as they want, but they are encouraged to read and understand the
	 * code before doing so. Use these methods at your own risk.
	 */
	public class VirtualTeam {

		private final String name;
		private String prefix;
		private String suffix;
		private String currentPlayer;
		private String oldPlayer;

		private boolean prefixChanged, suffixChanged, playerChanged = false;
		private boolean first = true;

		private VirtualTeam(String name, String prefix, String suffix) {
			this.name = name;
			this.prefix = prefix;
			this.suffix = suffix;
		}

		private VirtualTeam(String name) {
			this(name, "", "");
		}

		public String getName() {
			return name;
		}

		public String getPrefix() {
			return prefix;
		}

		public void setPrefix(String prefix) {
			if (this.prefix == null || !this.prefix.equals(prefix))
				this.prefixChanged = true;
			this.prefix = prefix;
		}

		public String getSuffix() {
			return suffix;
		}

		public void setSuffix(String suffix) {
			if (this.suffix == null || !this.suffix.equals(prefix))
				this.suffixChanged = true;
			this.suffix = suffix;
		}

		private PacketPlayOutScoreboardTeam createPacket(int mode) {
			PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();
			setField(packet, "a", name);
			setField(packet, "b", "");
			setField(packet, "c", prefix);
			setField(packet, "d", suffix);
			setField(packet, "i", 0);
			setField(packet, "e", "always");
			setField(packet, "g", 0);
			setField(packet, "i", mode);

			return packet;
		}

		public PacketPlayOutScoreboardTeam createTeam() {
			return createPacket(0);
		}

		public PacketPlayOutScoreboardTeam updateTeam() {
			return createPacket(2);
		}

		public PacketPlayOutScoreboardTeam removeTeam() {
			PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();
			setField(packet, "a", name);
			setField(packet, "i", 1);
			first = true;
			return packet;
		}

		public void setPlayer(String name) {
			if (this.currentPlayer == null || !this.currentPlayer.equals(name))
				this.playerChanged = true;
			this.oldPlayer = this.currentPlayer;
			this.currentPlayer = name;
		}

		public Iterable<PacketPlayOutScoreboardTeam> sendLine() {
			List<PacketPlayOutScoreboardTeam> packets = new ArrayList<>();

			if (first) {
				packets.add(createTeam());
			}
			else if (prefixChanged || suffixChanged) {
				packets.add(updateTeam());
			}

			if (first || playerChanged) {
				if (oldPlayer != null)                                        // remove these two lines ?
					packets.add(addOrRemovePlayer(4, oldPlayer));    //
				packets.add(changePlayer());
			}

			if (first)
				first = false;

			return packets;
		}

		public void reset() {
			prefixChanged = false;
			suffixChanged = false;
			playerChanged = false;
			oldPlayer = null;
		}

		public PacketPlayOutScoreboardTeam changePlayer() {
			return addOrRemovePlayer(3, currentPlayer);
		}

		@SuppressWarnings("unchecked")
		public PacketPlayOutScoreboardTeam addOrRemovePlayer(int mode, String playerName) {
			PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();
			setField(packet, "a", name);
			setField(packet, "i", mode);

			try {
				Field f = packet.getClass().getDeclaredField("h");
				f.setAccessible(true);
				((List<String>) f.get(packet)).add(playerName);
			}
			catch (NoSuchFieldException | IllegalAccessException e) {
				e.printStackTrace();
			}

			return packet;
		}

		public String getCurrentPlayer() {
			return currentPlayer;
		}

		public String getValue() {
			return getPrefix() + getCurrentPlayer() + getSuffix();
		}

		public void setValue(String value) {
			if (value.length() <= 16) {
				setPrefix("");
				setSuffix("");
				setPlayer(value);
			}
			else if (value.length() <= 32) {
				setPrefix(value.substring(0, 16));
				setPlayer(value.substring(16));
				setSuffix("");
			}
			else if (value.length() <= 48) {
				setPrefix(value.substring(0, 16));
				setPlayer(value.substring(16, 32));
				setSuffix(value.substring(32));
			}
			else {
				throw new IllegalArgumentException("Too long value ! Max 48 characters, value was " + value.length() + " !");
			}
		}

	}

	private static void setField(Object edit, String fieldName, Object value) {
		try {
			Field field = edit.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(edit, value);
		}
		catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

}