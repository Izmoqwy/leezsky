package lz.izmoqwy.market.blackmarket.stuff.titane;

import lombok.Getter;
import lz.izmoqwy.core.utils.ItemUtil;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.BlockState;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TitaneShield extends TitaneStuffBase {

	@Getter
	private ItemStack item;

	public TitaneShield(int cost) {
		super(cost);

		ItemStack item = ItemUtil.createItem(Material.SHIELD, prefix() + "Bouclier en titane", new HashMap<>(), ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_ATTRIBUTES);
		BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
		BlockState state = meta.getBlockState();
		Banner bannerState = (Banner) state;
		bannerState.setBaseColor(DyeColor.BLUE);

		List<Pattern> patterns = new ArrayList<>();
		patterns.add(new Pattern(DyeColor.GREEN, PatternType.GRADIENT_UP));
		patterns.add(new Pattern(DyeColor.WHITE, PatternType.GRADIENT_UP));
		patterns.add(new Pattern(DyeColor.BLACK, PatternType.CURLY_BORDER));
		patterns.add(new Pattern(DyeColor.BLACK, PatternType.CIRCLE_MIDDLE));
		patterns.add(new Pattern(DyeColor.BLACK, PatternType.STRIPE_DOWNLEFT));
		patterns.add(new Pattern(DyeColor.BLACK, PatternType.STRIPE_SMALL));
		bannerState.setPatterns(patterns);

		bannerState.update();
		meta.setBlockState(bannerState);
		item.setItemMeta(meta);
		this.item = item;
	}

}
