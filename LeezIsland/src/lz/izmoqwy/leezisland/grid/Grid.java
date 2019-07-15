/*
 * That file is a part of [Leezsky] LeezIsland
 * Copyright Izmoqwy
 * You can edit for your personal use.
 * You're not allowed to redistribute it at your own.
 */

package lz.izmoqwy.leezisland.grid;

import com.google.common.collect.Maps;

import java.util.Map.Entry;

class Grid {
	
	/*
	 * Décalage de 600 blocs à chaque fois 
	 * Une île compte 200 blocs
	 * Cela fait donc 400 blocs de séparation
	 */
	final static int INCR = 600, INCR_o = 200;
	// curr A.K.A line
	private static int curr = 0, pos = 0;
	private static double x = 0, z = 0;
	
	Grid(int curr, int pos, double x, double z) {
		Grid.curr = curr;
		Grid.pos = pos;
		Grid.x = x;
		Grid.z = z;
	}
	
	@Override
	public String toString() {
		return curr + "|" + pos + "|" + x + ":" + z;
	}
	
	private int getPL() {
		return 3 + ((curr - 1) * 2);
	}

	private int getTotalOnCurr() {
		int var = getPL();
		return var * 2 + (var - 2) * 2;
	}

	private int getRestOnCurr() {
		if(curr == 0)
			return 0;
		return getTotalOnCurr() - pos;
	}

	private boolean canNext() {
		return getRestOnCurr() > 0;
	}
	
	Entry<Double, Double> next() {
		if(canNext()) {
			int pl = getPL();
			if( pos < pl ) {
				// Aller à droite
				// ---> En haut
				x += INCR;
			}
			else if( pos <= (pl + (pl - 2)) ) {
				// Aller en bas
				// ---> A droite
				z -= INCR;
			}
			else if( pos < 2 * pl + (pl - 2) ) {
				// Aller à gauche
				// ---> En bas
				x -= INCR;
			}
			else if( pos < 2 * pl + 2 * (pl - 2) ) {
				// Aller en haut
				// ---> A gauche
				z += INCR;
			}
			pos++;
		}
		else  {
			curr++;
			x -= INCR;
			z += INCR * (curr == 1 ? 1 : 2);
			pos = 1;
		}

		return Maps.immutableEntry(x, z);
	}

	String getCurrentID() {
		return "A" + ((int)Math.floor(Math.pow(curr, pos) % 1000)) + "-" + curr + ":" + pos;
	}
	
//	String getCurrentID() {
//		String str = (Base64.getEncoder().encodeToString(Base64.getEncoder().encodeToString((curr + "0" + pos + "LeezSky").getBytes()).getBytes()).substring(0, 6) + curr);
//		return "A-" + (str.length() > 10 ? str.substring(0, 10) : str);
//	}
	
	Entry<Double, Double> getMiddle(double x, double z) {
		return Maps.immutableEntry(x + (((x + INCR_o) - x)/2), z + (((z + INCR_o) - z)/2));
	}
	
	public static void main(String[] args)
	{
		/*
		 * Prepare methods to static
		 */
		Grid grid = new Grid(0, 0, 0, 0);
		for(int i = 0; i < 9; i++)
		{
			
			grid.next();
			Entry<Double, Double> middle = grid.getMiddle(x, z);
			System.out.println( "Current: " + curr + " | Pos: " + pos );
			System.out.println( "> x: " + x + " z: " + z );
			double mx = middle.getKey() < 0 ? middle.getKey() - 1.5 : middle.getKey() + 1.5,
					mz = middle.getValue() < 0 ? middle.getValue() - 1.5 : middle.getValue() + 1.5;
			System.out.println( "M> x: " + mx + " z: " + mz );
			int range = 100;
			double mX = mx < 0 ? mx - (range-1) : mx - (range+1), mZ = mz < 0 ? mz - (range-1) : mz - (range+1);
			System.out.println( "Réobtenu: minX: " + mX + " minZ: " + mZ);
			System.out.println("maxX: " + (mX + (range*2) + (mX < 0 ? -1 : 1)) + " maxZ:" + (mZ + (range*2) + (mZ < 0 ? -1 : 1)));
			
		}
	}
	
}
