package lz.izmoqwy.core.hooks.worldedit;

import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.bukkit.v1_12.BukkitQueue_1_12;
import com.boydti.fawe.util.EditSessionBuilder;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.ClipboardFormats;
import com.sk89q.worldedit.function.FlatRegionFunction;
import com.sk89q.worldedit.function.FlatRegionMaskingFilter;
import com.sk89q.worldedit.function.biome.BiomeReplace;
import com.sk89q.worldedit.function.mask.Mask2D;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.visitor.FlatRegionVisitor;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Regions;
import com.sk89q.worldedit.world.biome.BaseBiome;
import lz.izmoqwy.core.CorePrinter;
import lz.izmoqwy.core.hooks.interfaces.WorldEditHook;
import lz.izmoqwy.core.objects.Cuboid;
import org.bukkit.Location;
import org.bukkit.block.Biome;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class FAWEHook implements WorldEditHook {

	private static final boolean ABLE_TO_USE_FAWE = true;

	@Override
	public boolean loadSchematic(File file, Location loc) {
		if (file == null || !file.exists())
			return false;

		BlockVector to = new BlockVector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		EditSession editSession = new EditSessionBuilder(new BukkitWorld(loc.getWorld())).fastmode(true).build();
		try {
			Objects.requireNonNull(ClipboardFormats
					.findByFile(file))
					.load(file)
					.paste(editSession, to, false);
			editSession.flushQueue();
			return true;
		}
		catch (IOException | NullPointerException ex) {
			ex.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean setBiome(Cuboid cuboid, Biome biome) {
		return setBiome(cuboid.getLowerNE(), cuboid.getUpperSW(), biome);
	}

	@Override
	public boolean setBiome(Location lowerNE, Location upperSW, Biome biome) {
		if (lowerNE == null || upperSW == null)
			return false;

		if (lowerNE.getWorld() != upperSW.getWorld()) {
			CorePrinter.warn("Trying to change biome of a region with coordinates in different worlds!");
			return false;
		}

		EditSession editSession = new EditSessionBuilder(FaweAPI.getWorld(lowerNE.getWorld().getName())).fastmode(true).build();
		final BaseBiome baseBiome = new BaseBiome(BukkitQueue_1_12.getAdapter().getBiomeId(biome));

		CuboidRegion region = new CuboidRegion(BukkitUtil.toVector(lowerNE), BukkitUtil.toVector(upperSW));

		Mask2D mask2D = editSession.getMask() != null ? editSession.getMask().toMask2D() : null;
		FlatRegionFunction replaceFunc = new BiomeReplace(editSession, baseBiome);
		if (mask2D != null) replaceFunc = new FlatRegionMaskingFilter(mask2D, replaceFunc);

		FlatRegionVisitor regionVisitor = new FlatRegionVisitor(Regions.asFlatRegion(region), replaceFunc);

		try {
			Operations.completeLegacy(regionVisitor);
			return true;
		}
		catch (MaxChangedBlocksException e) {
			e.printStackTrace();
		}
		return false;
	}

}
