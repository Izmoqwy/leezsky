package lz.izmoqwy.core.nms.scoreboard;

import org.bukkit.entity.Player;

public abstract class NMSScoreboard {

	protected final Player player;
	protected String objectiveName;

	public NMSScoreboard(Player player, String objectiveName) {
		this.player = player;
		this.objectiveName = objectiveName;
	}

	public abstract void create();

	public abstract void destroy();

	public abstract void setObjectiveName(String name);

	public abstract void setLine(int line, String value);

	public abstract void removeLine(int line);

	public abstract String getLine(int line);
}
