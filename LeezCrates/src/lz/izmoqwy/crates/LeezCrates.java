package lz.izmoqwy.crates;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lz.izmoqwy.core.self.CorePrinter;
import lz.izmoqwy.core.utils.ItemUtil;
import lz.izmoqwy.core.utils.LocationUtil;
import lz.izmoqwy.core.utils.ServerUtil;
import lz.izmoqwy.crates.listeners.CrateListener;
import lz.izmoqwy.crates.objects.Crate;
import lz.izmoqwy.crates.objects.CrateType;
import lz.izmoqwy.crates.objects.Hologram;
import lz.izmoqwy.crates.objects.Reward;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.DirectionalContainer;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LeezCrates extends JavaPlugin {

	public static final String PREFIX = "§6Crates §8» ";
	public static final Random RANDOM = new Random();
	private static final String GUI_TITLE = "§aOuverture de box";
	public static final String PREVIEW_TITLE = "§6Box §8» §e";

	@Getter
	private static LeezCrates instance;

	@Getter
	private static List<CrateType> crateTypes = Collections.emptyList();

	@Getter
	private static Map<Location, Crate> crates = Maps.newHashMap();
	@Getter
	private static File cratesFile;

	private final Pattern materialPattern = Pattern.compile("^(\\w+)(?:\\((\\d+)\\))?$");

	@Override
	public void onEnable() {
		instance = this;

		saveDefaultConfig();
		loadConfig();
		loadCrates();

		ServerUtil.registerCommand("leezcrates", new CratesCommand());
		ServerUtil.registerListeners(this, new CrateListener());
	}

	@SuppressWarnings({"deprecation", "ResultOfMethodCallIgnored"})
	protected void loadConfig() {
		File cratesFile = new File(getDataFolder(), "crates.yml");
		if (!cratesFile.exists()) {
			if (!cratesFile.getParentFile().exists())
				cratesFile.getParentFile().mkdirs();
			try {
				cratesFile.createNewFile();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		LeezCrates.cratesFile = cratesFile;

		FileConfiguration config = getConfig();

		ConfigurationSection typesSection = config.getConfigurationSection("types");
		List<CrateType> crateTypes = Lists.newArrayList();
		if (typesSection != null) {
			for (String type : typesSection.getKeys(false)) {
				ConfigurationSection typeSection = typesSection.getConfigurationSection(type);
				if (typeSection == null) continue;

				String fullMaterial = typeSection.getString("material", "CHEST");
				Matcher matcher = materialPattern.matcher(fullMaterial);

				MaterialData materialData = null;
				if (matcher.find()) {
					String strMaterial = matcher.group(1);
					if (strMaterial != null) {
						try {
							Material material = Material.valueOf(strMaterial.toUpperCase());
							if (matcher.group(2) != null) {
								byte data = Byte.parseByte(matcher.group(2));
								materialData = new MaterialData(material, data);
							}
							else {
								materialData = new MaterialData(material);
							}
						}
						catch (Exception ex) {
							CorePrinter.warn("Crate {0} seems to have an invalid material. Format: NAME or NAME(data)", type);
						}
					}
				}
				if (materialData == null)
					materialData = new MaterialData(Material.CHEST);

				List<Reward> rewards = Lists.newArrayList();
				ConfigurationSection rewardsSection = typeSection.getConfigurationSection("rewards");
				if (rewardsSection != null) {
					for (String key : rewardsSection.getKeys(false)) {
						ConfigurationSection rewardSection = rewardsSection.getConfigurationSection(key);
						if (rewardSection == null)
							continue;

						String fullRewardIconMaterial = rewardSection.getString("icon", "STONE");
						Matcher m = materialPattern.matcher(fullRewardIconMaterial);

						MaterialData mdata = null;
						if (m.find()) {
							String strMaterial = m.group(1);
							if (strMaterial != null) {
								try {
									Material material = Material.valueOf(strMaterial.toUpperCase());
									if (m.group(2) != null) {
										byte data = Byte.parseByte(m.group(2));
										mdata = new MaterialData(material, data);
									}
									else {
										mdata = new MaterialData(material);
									}
								}
								catch (Exception ex) {
									CorePrinter.warn("Crate reward {0} seems to have an invalid material ({1}). Format: NAME or NAME(data)", type, strMaterial);
								}
							}
						}

						if (mdata == null)
							mdata = new MaterialData(Material.CHEST);

						Reward reward = new Reward(mdata, ChatColor.translateAlternateColorCodes('&', rewardSection.getString("displayname", "§cSans nom")), rewardSection.getString("command", null), rewardSection.getInt("chance", 0));
						rewards.add(reward);
					}
				}
				crateTypes.add(new CrateType(type, ChatColor.translateAlternateColorCodes('&', typeSection.getString("displayname", type)), ChatColor.translateAlternateColorCodes('&', typeSection.getString("lore")), typeSection.getBoolean("broadcast", false), materialData, rewards));
			}
		}
		LeezCrates.crateTypes = crateTypes;
	}

	protected void loadCrates() {
		YamlConfiguration crates = YamlConfiguration.loadConfiguration(cratesFile);
		for (String key : crates.getKeys(false)) {
			ConfigurationSection crate = crates.getConfigurationSection(key);

			CrateType type = fromStringType(crate.getString("type"));
			if (type == null) {
				getLogger().warning(MessageFormat.format("Unknown crate type: {0}", crate.getString("type")));
				continue;
			}

			Location location = LocationUtil.loadFromYaml(crates, key + ".location");
			if (location == null) {
				getLogger().warning(MessageFormat.format("Crate {0} as an invalid location!", key));
				continue;
			}

			createCrate(key, type, location, false);
		}
	}

	protected static CrateType fromStringType(String strType) {
		List<CrateType> collect = LeezCrates.getCrateTypes().stream().filter(crateType -> strType.equalsIgnoreCase(crateType.getName())).collect(Collectors.toList());
		if (!collect.isEmpty())
			return collect.get(0);
		return null;
	}

	private static final byte[] axis = { 3, 4, 2, 5 }; // Reminder: reversed

	@SuppressWarnings("deprecation")
	protected static void createCrate(String id, CrateType type, @NotNull Location location, boolean save) {
		if (location.getWorld() == null)
			return;

		location.getWorld().getChunkAt(location);

		Block block = location.getBlock();
		block.setTypeIdAndData(type.getMaterialData().getItemTypeId(), type.getMaterialData().getData(), true);
		BlockState blockState = location.getBlock().getState();
		MaterialData data;
		if (blockState.getData() instanceof DirectionalContainer) {
			data = new MaterialData(type.getMaterialData().getItemType(), axis[Math.round(location.getYaw() / 90) & 0x3]);
		}
		else
			data = type.getMaterialData();
		blockState.setData(data);
		blockState.update();

		if (id == null) {
			if (save) {
				YamlConfiguration crates = YamlConfiguration.loadConfiguration(cratesFile);
				int i = 1;
				while (crates.isSet(Integer.toString(i)))
					i++;
				id = Integer.toString(i);
			}
			else
				id = Integer.toString(crates.size());
		}

		List<String> text = Lists.newArrayList(type.getDisplayName().startsWith("§") ? type.getDisplayName() : "§5✱ §d" + type.getDisplayName() + " §5✱");
		if (type.getLore() != null) {
			text.addAll(Arrays.asList(type.getLore().split("\\n")));
		}
		Hologram hologram = new Hologram(block.getLocation().add(.5D, -1.25D, .5D), text);
		Crate crate = new Crate(id, type, location, null, hologram);
		hologram.spawn();
		crates.put(block.getLocation(), crate);

		if (save) {
			YamlConfiguration crates = YamlConfiguration.loadConfiguration(cratesFile);
			crates.set(id + ".type", type.getName());
			LocationUtil.saveInYaml(crates, location, id + ".location");
			try {
				crates.save(cratesFile);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void preview(Crate crate, Player player) {
		int size = (int) Math.ceil(crate.getType().getRewards().size() / 9D);
		if (size > 6) {
			player.sendMessage(PREFIX + "§cCette box contient trop de recompenses pour afficher un apercu!");
			return;
		}
		if (size == 0) {
			player.sendMessage(PREFIX + "§cCette box ne contient pas de recompenses!");
			return;
		}

		Inventory previewInventory = Bukkit.createInventory(null, size * 9, PREVIEW_TITLE + crate.getType().getName());
		List<Reward> rewards = crate.getType().getRewards();
		for (int i = 0; i < rewards.size(); i++) {
			previewInventory.setItem(i, rewards.get(i).getItem());
		}
		player.openInventory(previewInventory);
	}

	@Getter
	private static List<Player> openingPlayers = Lists.newArrayList();

	public static void open(final Crate crate, final Player player) {
		final Inventory inventory = Bukkit.createInventory(null, 3 * 9, GUI_TITLE);
		setupBranding(inventory, false);
		player.openInventory(inventory);
		openingPlayers.add(player);

		/*
			Catching any potential unhandled error to prevent bug with player still in the openingPlayers list
				after an error occured
		 */
		try {
			new BukkitRunnable() {
				private int roll = 0;

				@Override
				public void run() {
					if (!player.isOnline()) {
						cancel();
						openingPlayers.remove(player);
						CorePrinter.warn("A player left after launching a crate opening ({0})", player.getName());
						return;
					}

					if (player.getOpenInventory() == null || !player.getOpenInventory().getTopInventory().equals(inventory)) {
						cancel();
						openingPlayers.remove(player);
						player.sendMessage(PREFIX + "§cVous avez annulé l'ouverture.");
						return;
					}

					if (roll == 15) {
						cancel();
						ItemStack rewardIcon = inventory.getItem(13);
						if (rewardIcon == null)
							return;

						Reward reward = crate.getType().getRewards().stream().filter(r -> r.getItem().isSimilar(rewardIcon)).collect(Collectors.toList()).get(0);
						if (reward.getCommand() != null)
							ServerUtil.performCommand(reward.getCommand().replace("%p", player.getName()).replace("%r", reward.getDisplayName()));
						if (crate.getType().isBroadcast()) {
							Bukkit.broadcastMessage(PREFIX + "§e" + player.getName() + " §7a ouvert une box §6" + (crate.getType().getDisplayName() != null ? crate.getType().getDisplayName() : crate.getType().getName()));
						}

						setBrand(inventory, 10, 16, null);
						inventory.setItem(13, rewardIcon);
						player.playSound(player.getLocation(), Sound.BLOCK_NOTE_CHIME, 1, 1);

						Bukkit.getScheduler().runTaskLater(instance, () -> {
							player.closeInventory();
							openingPlayers.remove(player);
							player.playSound(player.getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, 1, 1);
						}, 15);
					}
					else {
						player.playSound(player.getLocation(), Sound.BLOCK_NOTE_XYLOPHONE, 1, 1);
						roll(inventory, crate);
						roll++;
					}
				}
			}.runTaskTimer(instance, 0, 3);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			openingPlayers.remove(player);
		}
	}

	private static void roll(Inventory inventory, Crate crate) {
		for (int i = 10; i <= 15; i++) {
			ItemStack next = inventory.getItem(i + 1);
			if (next == null)
				continue;
			inventory.setItem(i, next);
		}
		inventory.setItem(16, crate.getRandomReward());
		setupBranding(inventory, true);
	}

	private static void setupBranding(Inventory inventory, boolean random) {
		if (random) {
			setBrandRandomGlass(inventory, 0, 9);
			setBrandRandomGlass(inventory, 17, 21);
			setBrandRandomGlass(inventory, 23, 26);
		}
		else {
			setBrand(inventory, 0, 9);
			setBrand(inventory, 17, 21);
			setBrand(inventory, 23, 26);
			inventory.setItem(22, ItemUtil.createItem(Material.REDSTONE_TORCH_ON, "§e..."));
		}
	}

	@SuppressWarnings("deprecation")
	private static void setBrandRandomGlass(Inventory inventory, int from, int to) {
		for (int i = from; i <= to; i++) {
			int random = RANDOM.nextInt(16);
			inventory.setItem(i,  ItemUtil.createItem(new MaterialData(Material.STAINED_GLASS_PANE, (byte) random), getChatColor(random) + "◕‿◕"));
		}
	}

	private static ChatColor getChatColor(int color) {
		switch(color) {
			case 1:
				return ChatColor.GOLD;
			case 2:
			case 6:
				return ChatColor.LIGHT_PURPLE;
			case 3:
				return ChatColor.AQUA;
			case 4:
				return ChatColor.YELLOW;
			case 5:
				return ChatColor.GREEN;
			case 7:
			case 12:
				return ChatColor.DARK_GRAY;
			case 8:
				return ChatColor.GRAY;
			case 9:
				return ChatColor.DARK_AQUA;
			case 10:
				return ChatColor.DARK_PURPLE;
			case 11:
				return ChatColor.BLUE;
			case 13:
				return ChatColor.DARK_GREEN;
			case 14:
				return ChatColor.RED;
			case 15:
				return ChatColor.BLACK;
			default:
				return ChatColor.WHITE;
		}
	}

	private static void setBrand(Inventory inventory, int from, int to) {
		setBrand(inventory, from, to, ItemUtil.createItem(Material.STAINED_GLASS_PANE, "§8◕‿◕"));
	}

	private static void setBrand(Inventory inventory, int from, int to, ItemStack item) {
		for (int i = from; i <= to; i++) {
			inventory.setItem(i, item);
		}
	}

}
