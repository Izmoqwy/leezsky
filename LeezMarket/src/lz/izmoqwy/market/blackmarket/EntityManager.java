package lz.izmoqwy.market.blackmarket;

import lz.izmoqwy.market.blackmarket.illegal.ForbiddenFighter;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityTypes;
import net.minecraft.server.v1_12_R1.MinecraftKey;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

public class EntityManager {

	@SuppressWarnings("RedundantCollectionOperation")
	public static void registerEntity(String name, int id, Class<? extends Entity> customClass) {
		MinecraftKey key = new MinecraftKey(name);
		EntityTypes.b.a(id, key, customClass);
		if (!EntityTypes.d.contains(key)) {
			EntityTypes.d.add(key);
		}
	}

	public static ForbiddenFighter spawnGolem(Location at) {
		ForbiddenFighter fighter = new ForbiddenFighter(((CraftWorld) at.getWorld()).getHandle());
		fighter.spawn(at);
		return fighter;
	}

}
