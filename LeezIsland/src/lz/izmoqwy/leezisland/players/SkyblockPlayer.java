/*
 * That file is a part of [Leezsky] LeezIsland
 * Copyright Izmoqwy
 * You can edit for your personal use.
 * You're not allowed to redistribute it at your own.
 */

package lz.izmoqwy.leezisland.players;

import lz.izmoqwy.leezisland.island.Island;
import org.bukkit.entity.Player;
import org.bukkit.entity.Player.Spigot;

public interface SkyblockPlayer extends OfflineSkyblockPlayer {

	Island getIsland();

	boolean isOwner();

	Player bukkit();
	Spigot spigot();
	
	void sendMessage(String message);
	void sendRawMessage(String raw);
	
}
