/*
 * That file is a part of [Leezsky] LeezSky
 * Copyright Izmoqwy
 * You can edit for your personal use.
 * You're not allowed to redistribute it at your own.
 */

package me.izmoqwy.leezsky.tasks;

import org.bukkit.scheduler.BukkitRunnable;

import me.izmoqwy.leezsky.LeezSky;

public abstract class LeezTask extends BukkitRunnable {
	
	private final long start, repeat;
	
	protected LeezTask(long start, long repeat, boolean inSeconds) {
		
		this.start = inSeconds ? start * 20 : start;
		this.repeat = inSeconds ? repeat * 20 : repeat;
		
	}
	
	protected LeezTask(long start, long repeat) {
		
		this(start, repeat, true);
		
	}
	
	public void start() {
		
		this.runTaskTimer(LeezSky.getInstance(), start, repeat);
		
	}
	
	public void startAsync() {
		
		this.runTaskTimerAsynchronously(LeezSky.getInstance(), start, repeat);
		
	}

}
