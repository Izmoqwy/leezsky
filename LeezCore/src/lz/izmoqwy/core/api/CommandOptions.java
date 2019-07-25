package lz.izmoqwy.core.api;

public class CommandOptions {

	String permission;
	boolean playerOnly = false;
	boolean needsArg = false;

	int cooldown = 0;

	public CommandOptions withPermission(String permission) {
		this.permission = permission;
		return this;
	}

	public CommandOptions playerOnly() {
		this.playerOnly = true;
		return this;
	}

	public CommandOptions needArg() {
		this.needsArg = true;
		return this;
	}

	public CommandOptions withCooldown(int cooldown) {
		this.cooldown = cooldown;
		return this;
	}

}
