package me.izmoqwy.hardpermissions;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class Group {

	@Getter
	private String name, chatcolor;
	@Getter @Setter
	private String prefix, suffix;

	@Getter
	private int power;

	@Getter
	private final ArrayList<String> basePermissions = Lists.newArrayList();
	@Getter @Setter
	private List<String> permissions;
	@Getter @Setter
	private List<Group> inheritances;

	public Group(String name, int power, String prefix, String suffix, List<String> permissions, String chatcolor) {
		this.name = name;
		this.chatcolor = chatcolor;
		this.power = power;

		this.prefix = prefix;
		this.suffix = suffix;

		basePermissions.addAll(permissions);
		this.permissions = permissions;
		inheritances = null;
	}

	public boolean hasInheritances() {
		return inheritances != null;
	}

	private void calculatePermissions() {
		if (inheritances == null) return;
		List<String> perms = permissions;
		for (Group group : inheritances) {
			for (String permission : group.getBasePermissions()) {
				if (permission.startsWith("ntag.")) continue;
				if (!perms.contains(permission)) perms.add(permission);
			}
		}
		setPermissions(perms);
	}

}
