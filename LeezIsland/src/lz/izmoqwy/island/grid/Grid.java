/*
 * That file is a part of [Leezsky] LeezIsland
 * Copyright Izmoqwy
 * You can edit for your personal use.
 * You're not allowed to redistribute it at your own.
 */

package lz.izmoqwy.island.grid;

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
	private static int x = 0, z = 0;
	
	Grid(int curr, int pos, int x, int z) {
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
	
	Entry<Integer, Integer> next() {
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
	
	Entry<Integer, Integer> getMiddle(int x, int z) {
		return Maps.immutableEntry(x + (((x + INCR_o) - x)/2), z + (((z + INCR_o) - z)/2));
	}
	
}
