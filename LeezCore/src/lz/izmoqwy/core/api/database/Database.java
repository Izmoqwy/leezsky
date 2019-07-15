/*
 * That file is a part of [HB] API
 * Copyright Izmoqwy
 * Created the 14 août 2018
 * You can edit for your personal use.
 * You're not allowed to redistribute it at your own.
 */

package lz.izmoqwy.core.api.database;

import org.bukkit.plugin.Plugin;

public interface Database {
	
	public boolean connect();
	public boolean disconnect();
	
	public boolean isConnected();
	
	public Plugin getPlugin();
	
}
