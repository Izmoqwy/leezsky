package lz.izmoqwy.core.api;

public class CompleteOptions {

	String permission;
	boolean playerOnly = false;

	public CompleteOptions withPermission(String permission) {
		this.permission = permission;
		return this;
	}

	public CompleteOptions playerOnly() {
		this.playerOnly = true;
		return this;
	}

}
