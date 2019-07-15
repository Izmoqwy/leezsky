package lz.izmoqwy.core.hooks;

import lz.izmoqwy.core.CorePrinter;
import lz.izmoqwy.core.helpers.PluginHelper;
import lz.izmoqwy.core.hooks.interfaces.ModerationHook;
import lz.izmoqwy.core.hooks.interfaces.WorldEditHook;
import lz.izmoqwy.core.hooks.moderation.LitebansHook;
import lz.izmoqwy.core.hooks.worldedit.DefaultHook;
import lz.izmoqwy.core.hooks.worldedit.FAWEHook;
import lz.izmoqwy.core.hooks.worldedit.WEHook;

public class HooksManager {

	private static NTEHook nteHook;
	private static ModerationHook moderationHook;
	private static WorldEditHook worldEditHook;

	public static void load() {
		if (PluginHelper.isLoaded("NametagEdit")) nteHook = new NTEHook();

		if (PluginHelper.isLoaded("LiteBans")) moderationHook = new LitebansHook();
		else CorePrinter.warn("No compatible moderation plugin found!");

		if (PluginHelper.isLoaded("FastAsyncWorldEdit") && PluginHelper.isLoaded("WorldEdit"))
			worldEditHook = new FAWEHook();
		else if (PluginHelper.isLoaded("WorldEdit"))
			worldEditHook = new WEHook();
		else
			worldEditHook = new DefaultHook();
	}

	public static boolean useNTE() {
		return nteHook != null;
	}

	public static NTEHook nte() {
		return nteHook;
	}

	public static boolean useModeration() {
		return moderationHook != null;
	}

	public static ModerationHook sanctions() {
		return moderationHook;
	}

	public static WorldEditHook worldedit() {
		return worldEditHook;
	}

}
