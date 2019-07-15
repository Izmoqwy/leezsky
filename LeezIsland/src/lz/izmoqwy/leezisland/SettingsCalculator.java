/*
 * That file is a part of [Leezsky] LeezIsland
 * Copyright Izmoqwy
 * You can edit for your personal use.
 * You're not allowed to redistribute it at your own.
 */

package lz.izmoqwy.leezisland;

import java.util.Arrays;
import java.util.List;

class SettingsCalculator {

	static List<TempPermission> visitorsPermissions = Arrays.asList(TempPermission.DOORS, TempPermission.LEVERS);
	
	public static void main(String[] args) {
		System.out.println("Doors: " + hasPermission(TempPermission.DOORS));
		System.out.println("Drop: " + hasPermission(TempPermission.DROP));
		System.out.println(((String)(System.currentTimeMillis() + "")).length());
		System.out.println(System.currentTimeMillis());
	}
	
	public static boolean hasPermission(TempPermission perm) {
		return visitorsPermissions.contains(perm);
	}
	
	enum TempPermission
	{
		
		DOORS('D'), GATES('G'), BUTTONS('B'), LEVERS('L'), PLATES('P'), ARMORSTANDS('A'), REDSTONE('R'), VILLAGERS('V'), DROP('d'), PICKUP('p');
		
		public char val;
		TempPermission(char val) { this.val = val; }
		
	}
	
}
