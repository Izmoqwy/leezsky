package me.izmoqwy.leezsky.managers;

import lombok.AllArgsConstructor;
import lz.izmoqwy.core.PlayerDataStorage;
import lz.izmoqwy.core.api.ItemBuilder;
import lz.izmoqwy.core.gui.MinecraftGUIListener;
import lz.izmoqwy.core.gui.UniqueMinecraftGUI;
import me.izmoqwy.leezsky.objectives.ObjectiveListener;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class SettingsManager {

	private static final String PATH = "settings.";

	public static final Setting<SimpleToggle>
			SCOREBOARD = new Setting<>("display_scoreboard", ScoreboardManager.INSTANCE, SimpleToggle.class, SimpleToggle.ON, Material.EMPTY_MAP,
			"Scoreboard",
			"Désactivez pour cacher le scoreboard"),
			OBJECTIVE_BOSSBAR = new Setting<>("display_objectives", ObjectiveListener.INSTANCE, SimpleToggle.class, SimpleToggle.ON, Material.EXP_BOTTLE,
					"Affichage des objectifs",
					"Désactivez pour cacher la barre des", "objectifs"),

			RECEIVE_AUTOMESSAGES = new Setting<>("receive_automessages", SimpleToggle.class, SimpleToggle.ON, Material.PAPER,
					"Messages automatiques",
					"Désactivez pour ne plus recevoir", "les messages et astuces automatiques"),
			RECEIVE_CHALLENGES = new Setting<>("receive_challenges", SimpleToggle.class, SimpleToggle.ON, Material.DRAGON_EGG,
					"Annonces de défis",
					"Désactivez pour ne plus recevoir les", "annonces de défis des autres joueurs", "(vous verrez toujours les votre)");

	public static final Setting<ChatSetting>
			CHAT_MESSAGES = new Setting<>("send_chat", ChatSetting.class, ChatSetting.EXTRA, Material.BOOK_AND_QUILL, "Messages du chat",
			"Passez en §eNormaux §7si vous avez", "des problèmes relatifs au chat");

	private static final Map<Integer, Setting<?>> SETTING_MAP = new HashMap<Integer, Setting<?>>() {{
		put(10, SCOREBOARD);
		put(11, OBJECTIVE_BOSSBAR);

		put(14, RECEIVE_CHALLENGES);
		put(15, RECEIVE_AUTOMESSAGES);
		put(16, CHAT_MESSAGES);
	}};

	public static void openSettings(Player player) {
		new SettingsGUI(player).open();
	}

	/*
		Settings "kinds"
	 */
	@AllArgsConstructor
	public enum SimpleToggle {
		ON(ChatColor.GREEN + "activé"), OFF(ChatColor.RED + "désactivé");

		private String s;

		@Override
		public String toString() {
			return this.s;
		}
	}

	@AllArgsConstructor
	public enum ChatSetting {
		EXTRA(ChatColor.LIGHT_PURPLE + "Avancés"), FLAT(ChatColor.YELLOW + "Normaux"), OFF(ChatColor.RED + "§cDésactivés");

		private String s;

		@Override
		public String toString() {
			return this.s;
		}
	}

	/*
		Classes definition
	 */
	public static final class Setting<T extends Enum<?>> {

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
			return clazz.getEnumConstants()[PlayerDataStorage.get(player, PATH + path, def.ordinal())];
		}

	}

	public interface SettingUser {

		void onSettingUpdate(Player player, Setting<?> setting, Enum<?> value);

	}

	private static class SettingsGUI extends UniqueMinecraftGUI implements MinecraftGUIListener {

		public SettingsGUI(Player player) {
			super(null, "§7Paramètres", player);

			SETTING_MAP.forEach((slot, setting) -> refresh(slot, setting, player));
			setItem(31, new ItemBuilder(SkullType.PLAYER)
					.name("§a" + player.getName()).setSkullOwner(player)
					.toItemStack(), true);

			addListener(this);
		}

		private void refresh(int slot, Setting<?> setting, Player player) {
			setItem(slot, new ItemBuilder(setting.icon)
					.name("§e" + setting.iconName).appendLore("§6État: §e" + setting.getState(player).toString()).appendLore(ChatColor.GRAY, setting.description)
					.toItemStack());
		}

		@Override
		public void onClick(Player player, ItemStack clickedItem, int slot) {
			if (!SETTING_MAP.containsKey(slot))
				return;

			Setting<?> setting = SETTING_MAP.get(slot);
			Enum<?>[] values = setting.clazz.getEnumConstants();
			if (values.length < 2)
				return;

			int index = ArrayUtils.indexOf(values, setting.getState(player));
			Enum<?> newValue = values[++index == values.length ? 0 : index];

			PlayerDataStorage.set(player, PATH + setting.path, newValue.ordinal());
			PlayerDataStorage.save(player);
			refresh(slot, setting, player);

			if (setting.listener != null)
				setting.listener.onSettingUpdate(player, setting, newValue);
		}

	}

}
