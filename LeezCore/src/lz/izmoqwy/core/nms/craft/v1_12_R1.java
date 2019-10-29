package lz.izmoqwy.core.nms.craft;

import net.minecraft.server.v1_12_R1.EntityPlayer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class v1_12_R1 implements NMSCraft {

	@Override
	public boolean hasInventoryOpened(Player player) {
		EntityPlayer entityPlayer = getEntityHuman(player);
		return entityPlayer.activeContainer != entityPlayer.defaultContainer;
	}

	private EntityPlayer getEntityHuman(Player player) {
		return cp(player).getHandle();
	}

	private CraftPlayer cp(Player player) {
		return (CraftPlayer) player;
	}

}
