package me.izmoqwy.leezsky.objectives;

import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

import java.util.Arrays;
import java.util.List;

public enum LeezObjective {

	BREAK_LOG("Couper une bûche", ObjectiveAction.BREAK, Material.LOG, Material.LOG_2),
	CRAFTING_TABLE("Fabriquer un établi", ObjectiveAction.CRAFT, Material.WORKBENCH),
	PLACE_WATER("Poser une source d'eau", ObjectiveAction.BUCKET, Material.WATER_BUCKET),
	BREAK_COBBLE("Casser une pierre", ObjectiveAction.BREAK, Material.COBBLESTONE),
	FURNACE("Fabriquer un four", ObjectiveAction.CRAFT, Material.FURNACE),
	SMELT_IRON("Faire cuir 3 minerais de fer", 3, ObjectiveAction.SMELT, Material.IRON_INGOT),
	FIRST_MOBS("Tuer 10 monstres hostiles", 10, ObjectiveAction.KILL, MobType.HOSTILE);

	@Getter
	final String name;
	@Getter
	final ObjectiveAction action;
	@Getter
	final int due;

	LeezObjective(String name, int due, ObjectiveAction action) {
		this.name = name;
		this.due = due;
		this.action = action;
	}

	List<MaterialData> toBreak;

	LeezObjective(String name, ObjectiveAction action, MaterialData... toBreak) {
		this(name, 1, action);
		this.toBreak = Arrays.asList(toBreak);
	}

	LeezObjective(String name, ObjectiveAction action, Material... toBreak) {
		this(name, 1, action);

		List<MaterialData> data = Lists.newArrayList();
		for (Material material : toBreak) {
			data.add(new MaterialData(material));
		}
		this.toBreak = data;
	}

	LeezObjective(String name, int due, ObjectiveAction action, MaterialData... toBreak) {
		this(name, due, action);
		this.toBreak = Arrays.asList(toBreak);
	}

	LeezObjective(String name, int due, ObjectiveAction action, Material... toBreak) {
		this(name, due, action);

		List<MaterialData> data = Lists.newArrayList();
		for (Material material : toBreak) {
			data.add(new MaterialData(material));
		}
		this.toBreak = data;
	}

	MobType mobType;

	LeezObjective(String name, int due, ObjectiveAction action, MobType mobType) {
		this(name, due, action);
		this.mobType = mobType;
	}

}
