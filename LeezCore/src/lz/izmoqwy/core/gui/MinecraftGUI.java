package lz.izmoqwy.core.gui;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Objects;

@Getter
public abstract class MinecraftGUI {

	private static int lastInternalId;

	@Getter(AccessLevel.NONE)
	private final int internalId;
	@Getter(AccessLevel.NONE)
	private final InternalGUIHolder holder;

	private final MinecraftGUI parent;
	private MinecraftGUIListener[] listeners = new MinecraftGUIListener[0];

	private String title;
	private boolean shared;

	private int rows = 3;
	private InventoryType inventoryType = InventoryType.CHEST;
	private boolean cancelClicks = true;

	private Map<Integer, ItemStack> slots;
	private Inventory bukkitInventory;

	public MinecraftGUI(MinecraftGUI parent, String title, boolean shared) {
		this.internalId = ++lastInternalId;
		this.parent = parent;

		this.title = title.length() > 32 ? title.substring(0, 32) : title;
		this.shared = shared;

		this.holder = new InternalGUIHolder(this);
		this.slots = Maps.newHashMap();
	}

	public void setRows(int rows) {
		Preconditions.checkArgument(rows > 0 && rows < 7);

		breakBukkitInventory();
		this.rows = rows;
	}

	public void setInventoryType(InventoryType inventoryType) {
		breakBukkitInventory();
		this.inventoryType = inventoryType != null ? inventoryType : InventoryType.CHEST;
	}

	public void setCancelClicks(boolean cancelClicks) {
		this.cancelClicks = cancelClicks;
	}

	public void addListener(MinecraftGUIListener listener) {
		if (listener == null)
			return;

		breakBukkitInventory();
		listeners = (MinecraftGUIListener[]) ArrayUtils.add(listeners, listener);
	}

	public void setItem(int slot, ItemStack itemStack) {
		if (inventoryType == InventoryType.CHEST && slot >= getRows() * 9) {
			Bukkit.getLogger().warning("Setting slot before setting current amount of rows !");
		}

		slots.put(slot, itemStack);
		if (bukkitInventory != null && shared)
			bukkitInventory.setItem(slot, itemStack);
	}

	public void setItem(int slot, ItemStack itemStack, boolean adaptive) {
		if (adaptive && slot >= getRows() * 9) {
			int newRows = (int) Math.ceil((slot > 0 ? slot : 1) / 9f);
			setRows(Math.min(newRows, 6));
		}

		setItem(slot, itemStack);
	}

	public void breakBukkitInventory() {
		if (bukkitInventory == null)
			return;

		if (!bukkitInventory.getViewers().isEmpty())
			bukkitInventory.getViewers().forEach(HumanEntity::closeInventory);
		bukkitInventory = null;
	}

	public Inventory toBukkitInventory() {
		if (bukkitInventory == null) {
			if (inventoryType == InventoryType.CHEST) bukkitInventory = Bukkit.createInventory(holder, rows * 9, title);
			else bukkitInventory = Bukkit.createInventory(holder, inventoryType, title);
			slots.forEach(bukkitInventory::setItem);
		}

		if (bukkitInventory != null) {
			if (shared)
				return bukkitInventory;
			else return cloneBukkitInventory(bukkitInventory);
		}
		return null;
	}

	public void open(Player player) {
		Preconditions.checkNotNull(player);
		Inventory inventory = toBukkitInventory();
		if (inventory == null)
			return;
		player.openInventory(inventory);
	}

	private Inventory cloneBukkitInventory(Inventory original) {
		Inventory cloned = original.getType() != InventoryType.CHEST ?
				Bukkit.createInventory(original.getHolder(), original.getType(), original.getTitle()) :
				Bukkit.createInventory(original.getHolder(), original.getSize(), original.getTitle());
		cloned.setContents(original.getContents());
		cloned.setMaxStackSize(original.getMaxStackSize());
		return cloned;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof MinecraftGUI)) return false;
		MinecraftGUI that = (MinecraftGUI) o;
		return internalId == that.internalId &&
				Objects.equals(parent, that.parent);
	}

	@Override
	public int hashCode() {
		return Objects.hash(internalId, parent);
	}

}
