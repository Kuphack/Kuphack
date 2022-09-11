package com.github.Vaapukkax.kuphack.flagclash.widgets;

import java.awt.Color;
import java.util.Objects;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;

@Deprecated
public class ItemModifier {

	private final String key;
	private final Color color;
	private final Object value;
	
	@Deprecated
	private ItemModifier(String key, Object value) {
		this(key, Color.RED, value);
	}
	
	private ItemModifier(String key, Color color, Object value) {
		this.key = key;
		this.color = color;
		this.value = value;
	}
	
	public String getKey() {
		return key;
	}
	
	public int getRGB() {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), 0).getRGB();
	}
	
	public Object getValue() {
		return value;
	}
	
	@Deprecated
	public String toString() {
		return toText().getString();
	}
	
	public static final int LORE_COLOR = new Color(92, 90, 150, 0).getRGB();
	
	public Text toText() {
		String valueString = Objects.toString(value);
		if (value instanceof Double) {
			if (((double)value) % 1 == 0) valueString = Objects.toString(((Double)value).intValue());
			valueString = valueString.replaceAll("Infinity", "\u221E");
		}
		if (key.isEmpty()) {
			MutableText text = Text.of(valueString).copy();
			text.setStyle(text.getStyle().withColor(LORE_COLOR));
			return text;
		}

		MutableText valueText = Text.of("").copy();
		if (value instanceof Number) {
			int bar = ((Number)value).intValue();
			for (int i = 0; i < 15; i++) {
				int rgb = i <= bar ? getRGB() : LORE_COLOR;
				MutableText barLine = Text.of("||").copy();
				barLine.setStyle(barLine.getStyle().withColor(rgb));
				valueText = valueText.append(barLine);
			}
			
			MutableText number = Text.of(" "+valueString).copy();
			if (bar >= 15) number.setStyle(number.getStyle().withColor(getRGB()));
			valueText = valueText.append(number);
		} else valueText = Text.of(valueString).copy();
		
		MutableText baseText = Text.of(key+": ").copy();
		baseText.setStyle(baseText.getStyle().withColor(LORE_COLOR));
		baseText.append(valueText);
		return baseText;
	}
	
	public static ItemModifier damage(double value) {
		return new ItemModifier("Damage", new Color(224, 15, 72), checkInfinite(value));
	}
	public static ItemModifier armor(double value) {
		return new ItemModifier("Armor", new Color(204, 99, 55), checkInfinite(value));
	}
	
	public static ItemModifier knockback(double value) {
		return new ItemModifier("Knockback", checkInfinite(value));
	}
	
	public static ItemModifier power(double value) {
		return new ItemModifier("Power", checkInfinite(value));
	}
	
	public static ItemModifier rate(double value) {
		return new ItemModifier("Rate", new Color(125, 230, 70), value);
	}
	public static ItemModifier infiniteRate() {
		return new ItemModifier("Rate", new Color(125, 230, 70), checkInfinite(999));
	}
	public static ItemModifier efficiency(double value) {
		return new ItemModifier("Efficiency", checkInfinite(value));
	}
	
	public static ItemModifier range(double value) {
		return new ItemModifier("Range", checkInfinite(value));
	}
	
	public static ItemModifier accuracy(double value) {
		return new ItemModifier("Accuracy", checkInfinite(value));
	}
	
	public static ItemModifier reload(double value) {
		return new ItemModifier("Reload", checkInfinite(value));
	}

	public static ItemModifier dash(double value) {
		return new ItemModifier("Dash", checkInfinite(value));
	}
	
	public static ItemModifier multishot(double value) {
		return new ItemModifier("Multishot", checkInfinite(value));
	}
	
	public static ItemModifier cooldown(double value) {
		return new ItemModifier("Cooldown", checkInfinite(value));
	}
	
	public static ItemModifier swiftness(double value) {
		return new ItemModifier("Swiftness", new Color(25, 210, 145), checkInfinite(value));
	}
	public static ItemModifier fireAspect(int value) {
		return new ItemModifier("Fire Aspect", value);
	}
	
	public static ItemModifier health(double value) {
		return new ItemModifier("Health", checkInfinite(value));
	}
	
	
	// SHIELD
	public static ItemModifier sturdiness(double value) {
		return new ItemModifier("Sturdiness", checkInfinite(value));
	}
	public static ItemModifier fragmenting(double value) {
		return new ItemModifier("Fragmenting", checkInfinite(value));
	}
	public static ItemModifier color(Color color) {
		return new ItemModifier("Color", color, color);
	}
	public static ItemModifier color(DyeColor color) {
		return new ItemModifier("Color", new Color(color.getSignColor()), color);
	}
	
	
	public static ItemModifier custom(String value) {
		return new ItemModifier("", value);
	}
	
	private static Object checkInfinite(double value) {
		return value > 500 ? Double.POSITIVE_INFINITY : value;
	}

	public boolean isCustom() {
		return key.equals("");
	}
	
}
