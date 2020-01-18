/*
 * That file is a part of [MC] EventMaker
 * Copyright Izmoqwy
 * You can edit for your personal use.
 * You're not allowed to redistribute it at your own.
 */

package lz.izmoqwy.core.world;

import com.google.common.collect.Lists;
import org.bukkit.block.Block;

import java.util.List;

public class BlocksLister {

	public static List<Block> listBlocksInSquare(Block referer, int radius, int height) {
		final List<Block> blocks = Lists.newArrayList();
		for (int x = radius; x >= -radius; x--) {
			for (int y = height; y >= -height; y--) {
				for (int z = radius; z >= -radius; z--) {
					blocks.add(referer.getRelative(x, y, z));
				}
			}
		}
		return blocks;
	}

	public static List<Block> listBlocksInCircle(Block center, double radius, int height, boolean filled) {
		return listBlocksInCircle(center, radius, radius, height, filled);
	}

	public static List<Block> listBlocksInCircle(Block center, double radiusX, double radiusZ, int height, boolean filled) {
		final List<Block> blocks = Lists.newArrayList();

		radiusX += 0.5D;
		radiusZ += 0.5D;
		if (height == 0)
			return Lists.newArrayList();

		if (height < 0)
			height = -height;

		double invRadiusX = 1.0D / radiusX,
				invRadiusZ = 1.0D / radiusZ;
		int ceilRadiusX = (int) Math.ceil(radiusX),
				ceilRadiusZ = (int) Math.ceil(radiusZ);
		double nextXn = 0.0D;

		label0350:
		for (int x = 0; x <= ceilRadiusX; x++) {
			final double xn = nextXn;
			nextXn = (x + 1) * invRadiusX;
			double nextZn = 0D;
			int z = 0;
			while (z <= ceilRadiusZ) {
				final double zn = nextZn;
				nextZn = (z + 1) * invRadiusZ;
				final double distanceSq = lengthSq(xn, zn);
				if (distanceSq > 1.0) {
					if (z == 0)
						break label0350;
					break;
				}
				else {
					if (filled || lengthSq(nextXn, zn) > 1.0 || lengthSq(xn, nextZn) > 1.0) {
						for (int y = 0; y < height; y++) {
							blocks.add(center.getRelative(x, y, z));
							blocks.add(center.getRelative(-x, y, z));
							blocks.add(center.getRelative(x, y, -z));
							blocks.add(center.getRelative(-x, y, -z));
						}
					}
					z++;
				}
			}
		}
		return blocks;
	}

	private static double lengthSq(double x, double z) {
		return x * x + z * z;
	}

}
