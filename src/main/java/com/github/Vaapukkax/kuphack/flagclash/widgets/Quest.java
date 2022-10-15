package com.github.Vaapukkax.kuphack.flagclash.widgets;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.text.WordUtils;

import com.github.Vaapukkax.kuphack.Kuphack;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

@SuppressWarnings("deprecation")
public enum Quest implements Widget {

	COLLECTOR("Collect some resources", 3),
	KILLER("Get 5 kills", 1),
	RAIDER("Break 5 unique flags", 4),
	JUMPER("Jump a lot", 1),
	SLAYER("Get 20 unique kills", 5);
//	private static Random random;
//	private static Random getRandom() {
//		if (random == null) random = new Random(69);
//		return random;
//	}
	
	private static final String FILLED_STAR = "\u2605", EMPTY_STAR = "\u2606";
	
	public final int difficulty;
	
	private final String description;
	private int x = -1;
	
	private Quest(String description, int difficulty) {
		this.description = description;
		this.difficulty = difficulty;
	}

	@Override
	public String toString() {
		return WordUtils.capitalize(name().replaceAll("_", " ").toLowerCase());
	}
	
	private static final ArrayList<String> unknownQuests = new ArrayList<>();

	@Override
	public int getX() {
		if (this.x == -1) this.x = Arrays.asList(values()).indexOf(this);
		return this.x;
	}

	@Override
	public int getY() {
		return 0;
	}

	@Override
	public ItemStack getIcon() {
		ItemStack stack = new ItemStack(Items.FILLED_MAP);
		MutableText text = Kuphack.color(Text.literal(toString()), new Color(143, 172, 205));
		stack.setCustomName(text.append(" "+getDifficultyAsString()));
		return stack;
	}

	public String getDifficultyAsString() {
	    StringBuilder sb = new StringBuilder();
	    for (int i = 0; i < 5; i++) {
	    	if (i < difficulty) sb.append(FILLED_STAR);
	    	else sb.append(EMPTY_STAR);
	    }
	    return sb.toString();
	}

	@Override
	public List<Text> getDescription() {
		return Arrays.asList(
			Kuphack.color(Text.literal(" "+description), Color.decode("#5c5a96"))
		);
	}
	
	public static int getCurrentProcentage() {
		for (Text line : Kuphack.getScoreboard()) {
			if (line.getString().chars().filter(ch -> ch == '|').count() >= 15) {
				return getProcentage(line.getSiblings()) * 5;
			}
		}
		return 0;
	}
	
	public static Quest fromName(String name) {
		for (Quest quest : values()) {
			if (quest.toString().equalsIgnoreCase(name)) return quest;
		}
		if (!unknownQuests.contains(name)) {
			Kuphack.LOGGER.info("[Kuphack] Unknown quests: '"+name+"' [Kuphack doesn't recognize this quest, please inform us]");
			unknownQuests.add(name);
		}
		return null;
	}
	
	private static int getProcentage(List<Text> siblings) {
		int procentage = 0;
		for (Text sibling : siblings) {
			if (sibling != null) {
				
				TextColor textColor = sibling.getStyle().getColor();
    			if (textColor != null) {
    				Color color = new Color(textColor.getRgb());
    				if (color.getRed() > 40 && color.getRed() < 120 && color.getGreen() > 230 && color.getBlue() > 230){
    					procentage += sibling.getString().length();
    				}
    			}
    			procentage += getProcentage(sibling.getSiblings());
			}
		}
		return procentage;
	}
	
}
