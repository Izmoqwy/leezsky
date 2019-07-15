package lz.izmoqwy.core.api;

public class CommandOptions {

	String permission;
	boolean playerOnly = false;

	int cooldown = 0;

	public CommandOptions withPermission(String permission) {
		this.permission = permission;
		return this;
	}

	public CommandOptions playerOnly() {
		return this;
	}

	public CommandOptions withCooldown(int cooldown) {
		this.cooldown = cooldown;
		return this;
	}

}
