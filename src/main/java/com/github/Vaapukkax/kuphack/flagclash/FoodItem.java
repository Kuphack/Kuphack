package com.github.Vaapukkax.kuphack.flagclash;

import static java.util.Map.entry;

import java.awt.Color;
import java.util.Map;

import com.github.Vaapukkax.kuphack.Kuphack;

import net.minecraft.item.ItemStack;

public enum FoodItem {

	BUDGET_SPAGHETTI(entry("Silver Boost", new Color(172, 174, 176)), entry("Money Boost", Color.YELLOW)),
	FRENCH_FRIES(entry("Money Boost", Color.YELLOW)),
	
	SUGAR_CAKE(entry("Silver Boost", new Color(172, 174, 176))),
	BEANY_BEANS(entry("Double Silver", new Color(192, 192, 192))),
	
	STACKED_SALAD(entry("+100% XP-boost", Color.GREEN)),
	CRABBY_CRAB(entry("+50% XP-boost", Color.GREEN)),

	BLUEBERRY_MUFFIN(entry("+50% Starite", Color.BLUE));
	
	private final Map<String, Color> map;
	
	@SafeVarargs
	private FoodItem(Map.Entry<String, Color>... effects) {
		this.map = Map.ofEntries(effects);
	}
	
	public Map<String, Color> getEffects() {
		return this.map;
	}
	
	public static FoodItem of(ItemStack stack) {
		if (stack == null) return null;
		try {
			return valueOf(Kuphack.stripColor(stack.getName()).replace(' ', '_').toUpperCase());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	
	
}
