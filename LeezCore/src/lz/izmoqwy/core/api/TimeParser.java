/*
 * That file is a part of [HB] API
 * Copyright Izmoqwy
 * Created the 16 août 2018
 * You can edit for your personal use.
 * You're not allowed to redistribute it at your own.
 */

package lz.izmoqwy.core.api;

import java.util.concurrent.TimeUnit;

public class TimeParser {
	
	private static TimeParser parse = new TimeParser();
	
	public static HTime parse(String arg) {
		
		try {
			
			int index = 0;
			while(arg.length() > index && Character.isDigit(arg.charAt(index))) 
				index++;
			String time = arg.substring(index--);
			TimeUnit unit = null;
			if(match(time, "d", "day")) unit = TimeUnit.DAYS;
			else if(match(time, "h", "hour")) unit = TimeUnit.HOURS;
			else if(match(time, "m", "min")) unit = TimeUnit.MINUTES;
			else if(match(time, "s", "sec")) unit = TimeUnit.SECONDS;
			if(unit == null) return null;
			return parse.new HTime(unit, Long.parseLong(arg.substring(index, arg.length()-1)));
			
		}catch(Exception ex) {
			
			return null;
			
		}
		
	}
	
	private static boolean match(String testFor, String arg1, String arg2) {
		
		if(testFor.equalsIgnoreCase(arg1)) return true;
		return testFor.startsWith(arg2);
		
	}
	
	public class HTime {
		
		private TimeUnit unit;
		private long many;
		
		protected HTime(TimeUnit unit, long many) {
			
			this.unit = unit;
			this.many = many;
			
		}
		
		public long getMillis() {
			
			return unit.toMillis(many);
			
		}
		
		public long getMany() {
			
			return many;
			
		}
		
		public TimeUnit getUnit() {
			
			return unit;
			
		}
		
		public String toFrench() {
			
			String s;
			switch(unit) {
			
			case DAYS:
				s = "jour";
				break;
				
			case HOURS:
				s = "heure";
				break;
				
			case MINUTES:
				s = "minute";
				break;
				
			case SECONDS:
				s = "seconde";
				break;
				
			default:
				s = unit.name().toLowerCase().substring(unit.name().length()-2);
				break;
			
			}
			return many > 1 ? many + " " + s + "s" : many + " " + s;
			
		}
		
	}

}
