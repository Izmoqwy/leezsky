/*
 * That file is a part of [Leezsky] LeezSky
 * Copyright Izmoqwy
 * You can edit for your personal use.
 * You're not allowed to redistribute it at your own.
 */

package me.izmoqwy.leezsky.tasks;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import me.izmoqwy.leezsky.LeezSky;

public class Rebooter extends LeezTask {

	public Rebooter() {
		
		super(5 * 60, 60);
		
	}

	private final List<Integer> notif = Arrays.asList(45, 30, 15, 10, 5, 4, 3, 2, 1);
	private int time = 0;
	
	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		
		if(time == 0) {
			
			Calendar c = new GregorianCalendar(); Date current = c.getTime();
			if(current.getHours() == 3 && current.getMinutes() == 25) {
				
				time = 5;
			
			}	
			
		}
		
		if(time > 0) {
			
			if(time <= 5) {
				
				Bukkit.broadcastMessage("§4(§cRedémarrage§4) §cLe serveur va redémarrer automatiquement dans §6" + time + " " + (time > 1 ? "minutes" : "minute") + "§c.");
				time--;
				
			}
			
			if(time == 0) {
				
				new BukkitRunnable() {
					
					private int timer = 60;
					
					@Override
					public void run() {
						
						if(timer > 1) {
							
							timer--;
							if(notif.contains(timer)) {
								
								Bukkit.broadcastMessage("§4(§cRedémarrage§4) §cLe serveur va redémarrer automatiquement dans §e" + timer + " " + (timer > 1 ? "secondes" : "seconde") + "§c.");
								
							}
							
						}else {
							
							Bukkit.broadcastMessage( "§4(§cRedémarrage§4) §cLe serveur va redémarrer." );
							cancel();
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "reboot");
							
						}
						
					}
					
				}.runTaskTimer(LeezSky.getInstance(), 20, 20);
				cancel();
				
			}
			
		}
		
	}

}
