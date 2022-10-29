package com.github.Vaapukkax.kuphack.flagclash.sheets.widgets;

import static com.github.Vaapukkax.kuphack.flagclash.sheets.widgets.ShopLayoutItem.*;

public class LayoutTree extends Tree<ShopLayoutItem> {

	public static final LayoutTree ROOT = new LayoutTree(ShopLayoutItem.ROOT,
		new LayoutTree(BLUE_KEYCARD,
			new LayoutTree(RED_KEYCARD,
				new LayoutTree(RED_SOUP),
				new LayoutTree(SHATTERKELP),
				new LayoutTree(LIFE_ANCHOR)
			),
			new LayoutTree(CYAN_KEYCARD,
				new LayoutTree(SHROOMLIGHT),
				new LayoutTree(ENDER_PEARL),
				new LayoutTree(TURRET_BONE),
				new LayoutTree(SOUL_ANCHOR)
			),
			new LayoutTree(WHITE_KEYCARD,
				new LayoutTree(GLASS_BOTTLE)
			),
			new LayoutTree(SKULL_CRUSHER),
			new LayoutTree(IRON_PICKAXE),
			new LayoutTree(LEATHER_LEGGINGS),
			new LayoutTree(LEATHER_BOOTS),
			new LayoutTree(CHAINMAIL_BOOTS),
			
			new LayoutTree(CHEAPER_HARDEST_CLAY),
			new LayoutTree(HARDEST_CLAY),
			new LayoutTree(IRON_AXE),
			new LayoutTree(RELOCATION_WAND),
			new LayoutTree(THROWABLE_TNT)
		)
	);

	public LayoutTree(ShopLayoutItem item, LayoutTree... paths) {
		super(item, paths);
	}

}
