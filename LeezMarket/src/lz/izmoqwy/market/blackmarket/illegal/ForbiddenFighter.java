package lz.izmoqwy.market.blackmarket.illegal;

import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftIronGolem;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.reflect.Field;
import java.util.Set;

public class ForbiddenFighter extends EntityIronGolem {

	public ForbiddenFighter(World world) {
		super(world);
		bukkitEntity = new CraftIronGolem((CraftServer) Bukkit.getServer(), this);

		Set goalB = (Set) getPrivateField("b", goalSelector);
		goalB.clear();
		Set goalC = (Set) getPrivateField("c", goalSelector);
		goalC.clear();
		Set targetB = (Set) getPrivateField("b", targetSelector);
		targetB.clear();
		Set targetC = (Set) getPrivateField("c", targetSelector);
		targetC.clear();

		goalSelector.a(0, new PathfinderGoalFloat(this));
		goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(this, 1.0D)); // The goal to move
		goalSelector.a(7, new PathfinderGoalRandomStroll(this, 1.0D)); // The goal to walk around
//		goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 0.0F)); // The goal to look at players
		goalSelector.a(8, new PathfinderGoalRandomLookaround(this)); // The goal to look around

		goalSelector.a(2, new PathfinderGoalMeleeAttack(this, 1.0, true)); // Adds melee attack to the mob
		targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, 0, true, false, null));

		AttributeInstance followRange = getAttributeInstance(GenericAttributes.FOLLOW_RANGE);
		followRange.setValue(80);
	}

	public void spawn(Location loc) {
		setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
		world.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
	}

	public void setSpeed(float speed) {
		getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
	}

	private static Object getPrivateField(String fieldName, Object object) {
		Field field;
		Object o = null;

		try {
			field = PathfinderGoalSelector.class.getDeclaredField(fieldName);
			field.setAccessible(true);
			o = field.get(object);
		}
		catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}

		return o;
	}

}
