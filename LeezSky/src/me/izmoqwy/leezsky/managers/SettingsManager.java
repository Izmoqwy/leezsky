package me.izmoqwy.leezsky.managers;

import lz.izmoqwy.core.GUIManager;
import lz.izmoqwy.core.GUIValueChangedEvent;
import lz.izmoqwy.core.PlayerDataStorage;
import lz.izmoqwy.core.builder.ItemBuilder;
import me.izmoqwy.leezsky.objectives.ObjectiveListener;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class SettingsManager implements GUIManager.GUIValueChanger {

	private static final String PATH = "settings.";

	private static final SettingsManager INSTANCE = new SettingsManager();
	private static final String INVENTORY_NAME = "§7Paramètres";

	public static final Setting
			SCOREBOARD = new Setting<>("scoreboard", SimpleToggle.class, SimpleToggle.ON, Material.EMPTY_MAP, "Scoreboard", "Désactivez pour cacher le scoreboard"),
			OBJECTIVE_BOSSBAR = new Setting<>("objectivebb", ObjectiveListener.INSTANCE, SimpleToggle.class, SimpleToggle.ON, Material.EXP_BOTTLE, "Affichage des objectifs", "Désactivez pour cacher la bossbar des", "objectifs"),

			CHAT_MESSAGES = new Setting<>("chatmsgs", ChatSetting.class, ChatSetting.EXTRA, Material.BOOK_AND_QUILL, "Mesages du chat", "Passez en §eNormaux §7si vous avez des", "problèmes relatifs au chat");
			//TOD: PRIVATE_MESSAGES = new Setting<>("privatemsgs", ChatSetting.class, ChatSetting.EXTRA, Material.PAPER, "Mesages privés", "Decidez de qui peut vous envoyez des messages privés ou non");

	private static final Map<Integer, Setting> SETTING_MAP = new HashMap<Integer, Setting>() {
		{
			put(10, SCOREBOARD);
			put(11, OBJECTIVE_BOSSBAR);

			put(16, CHAT_MESSAGES);
		}
	};

	public static Inventory bakeInventory(Player player) {
		Inventory inventory = Bukkit.createInventory(null, 9 * 4, INVENTORY_NAME);
		SETTING_MAP.forEach((slot, setting) -> inventory.setItem(slot, bake(setting)));

		ItemBuilder playerInfo = new ItemBuilder(SkullType.PLAYER);
		playerInfo.name("§a" + player.getName()).skullOwner(player.getName());
		inventory.setItem(31, playerInfo.toItemStack());
		return inventory;
	}

	private static ItemStack bake(Setting setting) {
		ItemBuilder itemBuilder = new ItemBuilder(setting.icon).name("§e" + setting.iconName);
		if (setting.description.length > 0) {
			itemBuilder.appendLore(ChatColor.RED, "Erreur de chargement!");
			itemBuilder.appendLore(ChatColor.GRAY, setting.description);
		}
		return itemBuilder.toItemStack();
	}

	public static void load() {
		GUIManager.GUIActionsBuilder settingsGuiBuilder = new GUIManager.GUIActionsBuilder();

		//noinspection unchecked
		SETTING_MAP.forEach((slot, setting) -> settingsGuiBuilder.valueChanger(INSTANCE, slot, player -> get(player, setting.path, setting.clazz, setting.def), true, (Enum[]) setting.clazz.getEnumConstants()));

		GUIManager.registerInventory(INVENTORY_NAME, settingsGuiBuilder.build());
	}

	@Override
	public void onChange(GUIValueChangedEvent event) {
		if (event.getInventoryName().equals(INVENTORY_NAME)) {
			if (SETTING_MAP.containsKey(event.getClickedSlot())) {
				Setting setting = SETTING_MAP.get(event.getClickedSlot());
				if (event.getNewValue().getClass() != setting.clazz) {
					event.setCancelled(true);
				}
				PlayerDataStorage.set(event.getPlayer(), PATH + setting.path, event.getNewValue().ordinal());
				PlayerDataStorage.saveNoThrow(event.getPlayer());
				if (setting.listener != null)
					setting.listener.onSettingUpdate(event.getPlayer(), setting, event.getNewValue());
			}
		}
	}


	private static <T extends Enum> T get(OfflinePlayer player, String path, Class<? extends T> clazz, T def) {
		return clazz.getEnumConstants()[PlayerDataStorage.get(player, PATH + path, def.ordinal())];
	}

	public interface SettingUser {
		void onSettingUpdate(Player player, Setting setting, Enum value);
	}

	/*
		All settings enums
	 */
	public enum SimpleToggle {
		ON("§aactivé"), OFF("§cdésactivé");

		private String s;
		SimpleToggle(String s) {
			this.s = s;
		}

		@Override
		public String toString() {
			return this.s;
		}
	}

	public enum ChatSetting {
		EXTRA("§dAvancés"), FLAT("§eNormaux"), OFF("§cDésactivés");

		private String s;
		ChatSetting(String s) {
			this.s = s;
		}

		@Override
		public String toString() {
			return this.s;
		}
	}

	/*
		Setting class
	 */

	public static final class Setting <T extends Enum> {
		private final String path;
		private final SettingUser listener;

		private final Class<? extends T> clazz;
		private final T def;

		private final Material icon;
		private final String iconName;
		private final String[] description;

		public Setting(String path, Class<? extends T> clazz, T def, Material icon, String iconName, String... description) {
			this(path, null, clazz, def, icon, iconName, description);
		}

		public Setting(String path, SettingUser listener, Class<? extends T> clazz, T def, Material icon, String iconName, String... description) {
			this.path = path;
			this.listener = listener;
			this.clazz = clazz;
			this.def = def;
			this.icon = icon;
			this.iconName = iconName;
			this.description = description;
		}

		public T getState(OfflinePlayer player) {
			return get(player, path, clazz, def);
		}
	}
}
