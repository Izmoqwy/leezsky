package lz.izmoqwy.core.api;

import lombok.Getter;
import lz.izmoqwy.core.utils.LocationUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Getter
public class PlayerBackup {

	private final Location from;
	private final ItemStack[] contents, armorContents;
	private final float[] fullExp, fullFood;
	private final double health;
	private final boolean[] fullFly;
	private final GameMode gameMode;

	protected PlayerBackup(Location from, ItemStack[] contents, ItemStack[] armorContents, float[] xp, double health, float[] food, boolean[] fly, GameMode gameMode) {
		this.from = from;
		this.contents = contents;
		this.armorContents = armorContents;
		this.fullExp = xp;
		this.health = health;
		this.fullFood = food;
		if (fly == null)
			this.fullFly = new boolean[0];
		else this.fullFly = fly;
		this.gameMode = gameMode;
	}

	public void restore(Player onPlayer, boolean teleport) {
		if (onPlayer.isDead())
			onPlayer.spigot().respawn();
		else {
			onPlayer.setFallDistance(0f);
		}

		onPlayer.closeInventory();
		clearBukkitPlayer(onPlayer, false);

		onPlayer.setGameMode(this.gameMode);
		onPlayer.getInventory().setContents(this.contents);
		onPlayer.getInventory().setArmorContents(this.armorContents);
		onPlayer.updateInventory();

		onPlayer.setLevel((int) this.fullExp[0]);
		onPlayer.setExp(this.fullExp[1]);
		onPlayer.setTotalExperience((int) this.fullExp[2]);

		onPlayer.setHealth(this.health);
		onPlayer.setFoodLevel((int) this.fullFood[0]);
		onPlayer.setSaturation(this.fullFood[1]);

		onPlayer.setAllowFlight(this.fullFly[0]);
		onPlayer.setFlying(this.fullFly[1] && this.fullFly[0]);

		if (teleport)
			onPlayer.teleport(this.from);
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	public void save(File file, String path, boolean append) throws IOException {
		if (path == null || path.equals("."))
			path = "";
		else if (!path.endsWith("."))
			path += ".";

		if (!file.exists()) {
			if (!file.getParentFile().exists())
				file.getParentFile().mkdirs();
			file.createNewFile();
		}
		YamlConfiguration yaml = append ? YamlConfiguration.loadConfiguration(file) : new YamlConfiguration();
		LocationUtil.saveInYaml(yaml, this.from, path + "location");
		yaml.set(path + "contents", this.contents);
		yaml.set(path + "armorcontents", this.armorContents);

		yaml.set(path + "fullExp.level", this.fullExp[0]);
		yaml.set(path + "fullExp.fullExp", this.fullExp[1]);
		yaml.set(path + "fullExp.total", this.fullExp[2]);

		yaml.set(path + "health", this.health);
		yaml.set(path + "fullFood.level", this.fullFood[0]);
		yaml.set(path + "fullFood.saturation", this.fullFood[1]);

		if (this.fullFly.length == 2) {
			yaml.set(path + "fullFly.allow", this.fullFly[0]);
			yaml.set(path + "fullFly.flying", this.fullFly[1]);
		}

		yaml.set(path + "gamemode", this.gameMode.ordinal());
		yaml.save(file);
	}

	public static PlayerBackup fromBukkitPlayer(Player player) {
		return new PlayerBackup(player.getLocation(),
				player.getInventory().getContents(), player.getInventory().getArmorContents(),
				new float[]{player.getLevel(), player.getExp(), player.getTotalExperience()},
				player.getHealth(),
				new float[]{player.getFoodLevel(), player.getSaturation()},
				new boolean[]{player.getAllowFlight(), player.isFlying()},
				player.getGameMode());
	}

	@SuppressWarnings("unchecked")
	public static PlayerBackup fromYaml(YamlConfiguration yaml, String path) {
		if (path == null || path.equals("."))
			path = "";
		else if (!path.endsWith("."))
			path += ".";

		Location location = LocationUtil.loadFromYaml(yaml, path + "location");
		ItemStack[] contents = ((List<ItemStack>) yaml.get(path + "contents")).toArray(new ItemStack[0]);
		ItemStack[] armorContents = ((List<ItemStack>) yaml.get(path + "armorcontents")).toArray(new ItemStack[0]);

		float[] exp = new float[]{(float) yaml.getDouble(path + "fullExp.level"), (float) yaml.getDouble(path + "fullExp.fullExp"), (float) yaml.getDouble(path + "fullExp.total")};

		double health = yaml.getDouble(path + "health");
		float[] food = new float[]{(float) yaml.getDouble(path + "fullFood.level"), (float) yaml.getDouble(path + "fullFood.saturation")};

		boolean[] fly;
		if (yaml.isSet(path + "fullFly.allow") && yaml.isSet(path + "fullFly.flying"))
			fly = new boolean[]{yaml.getBoolean(path + "fullFly.allow", false), yaml.getBoolean(path + "fullFly.flying", false)};
		else
			fly = new boolean[0];

		return new PlayerBackup(location, contents, armorContents, exp, health, food, fly, GameMode.values()[yaml.getInt(path + "gamemode")]);
	}

	public static void clearBukkitPlayer(Player player) {
		clearBukkitPlayer(player, true, GameMode.SURVIVAL);
	}

	public static void clearBukkitPlayer(Player player, boolean regen) {
		clearBukkitPlayer(player, regen, player.getGameMode());
	}

	public static void clearBukkitPlayer(Player player, boolean regen, GameMode gameMode) {
		player.getInventory().clear();
		player.getEquipment().clear();
		player.updateInventory();

		player.setLevel(0);
		player.setExp(0.001F);
		player.setTotalExperience(0);
		if (regen) {
			player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
			player.setFoodLevel(20);
			player.setSaturation(20.F);
			player.setExhaustion(0.F);
		}
		player.setGameMode(gameMode);
	}

}
