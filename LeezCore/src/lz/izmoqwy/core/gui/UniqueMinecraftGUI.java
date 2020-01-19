package lz.izmoqwy.core.gui;

import io.netty.util.internal.UnstableApi;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public abstract class UniqueMinecraftGUI extends MinecraftGUI {

	private Player player;

	public UniqueMinecraftGUI(MinecraftGUI parent, String title, Player player) {
		super(parent, title, true);
		this.player = player;

		addListener(new UniqueMinecraftGUIProtector());
	}

	@UnstableApi
	@Override
	public void open(Player player) {
		throw new UnsupportedOperationException("Trying to open inventory for player in unique GUI.");
	}

	public void open() {
		super.open(player);
	}

	private class UniqueMinecraftGUIProtector implements MinecraftGUIListener {

		@Override
		public boolean canOpen(Player player) {
			return player.equals(UniqueMinecraftGUI.this.player);
		}

	}

}
