package com.github.vaapukkax.kuphack.flagclash.sheets.widgets;

public class ItemModifier {

	private final String icon;
	private final int value;
	
	private ItemModifier(String icon, int value) {
		this.icon = icon;
		this.value = value;
	}
	
	public String getKey() {
		return icon;
	}
	
	public int getValue() {
		return value;
	}
	
	public static ItemModifier attack(int value) {
		return new ItemModifier("Attack", value);
	}
	
	public static ItemModifier speed(int value) {
		return new ItemModifier("Speed", value);
	}
	
	public static ItemModifier crit(int value) {
		return new ItemModifier("Crit", value);
	}
	
}
