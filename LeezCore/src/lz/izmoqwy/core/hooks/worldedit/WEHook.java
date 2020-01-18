package lz.izmoqwy.core.hooks.worldedit;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.schematic.MCEditSchematicFormat;
import com.sk89q.worldedit.world.DataException;
import lz.izmoqwy.core.hooks.interfaces.WorldEditHook;
import lz.izmoqwy.core.world.Cuboid;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Biome;

import java.io.File;
import java.io.IOException;

public class WEHook implements WorldEditHook {

	@Override
	public boolean loadSchematic(File file, Location location) {
		WorldEditPlugin wePlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
		EditSession editSession = wePlugin.getWorldEdit().getEditSessionFactory().getEditSession(new BukkitWorld(location.getWorld()), Integer.MAX_VALUE);

		try {
			CuboidClipboard cc = MCEditSchematicFormat.getFormat(file).load(file);
			cc.paste(editSession, new Vector(location.getX(), location.getY(), location.getZ()), false);
			return true;
		}
		catch (DataException | IOException | MaxChangedBlocksException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean setBiome(Cuboid cuboid, Biome biome) {
		return false;
	}

	@Override
	public boolean setBiome(Location lowerNE, Location upperSW, Biome biome) {
		return false;
	}
}
