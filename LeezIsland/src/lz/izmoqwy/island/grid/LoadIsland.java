package lz.izmoqwy.island.grid;

class LoadIsland {
	private double midX, midZ;
	private int range = (Grid.INCR / 2 + Grid.INCR_o / 2) - 1;

	LoadIsland(double midX, double midZ) {
		this.midX = midX;
		this.midZ = midZ;
	}

	double getMinX() {
		return this.midX < 0 ? this.midX - (this.range - 1) : this.midX - (this.range + 1);
	}

	double getMinZ() {
		return this.midZ < 0 ? this.midZ - (this.range - 1) : this.midZ - (this.range + 1);
	}

	double getMaxX() {
		return getMinX() + (this.range * 2) + (getMinX() < 0 ? -1 : 1);
	}

	double getMaxZ() {
		return getMinZ() + (this.range * 2) + (getMinZ() < 0 ? -1 : 1);
	}

	double getLowerX() {
		return Math.min(getMinX(), getMaxX());
	}

	double getLowerZ() {
		return Math.min(getMinZ(), getMaxZ());
	}

}
