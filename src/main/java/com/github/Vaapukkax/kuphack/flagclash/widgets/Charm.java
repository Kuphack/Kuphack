package com.github.Vaapukkax.kuphack.flagclash.widgets;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.text.WordUtils;

import com.github.Vaapukkax.kuphack.Kuphack;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

@SuppressWarnings("deprecation")
public enum Charm implements Widget {

	ADREALINE_RUSH(1, Items.GLOWSTONE_DUST, "Gain a random positive status\neffect when you kill someone", 0, 0),
	BLESSING_OF_THE_WARRIOR(2, Items.TOTEM_OF_UNDYING, "Gives you 5 golden hearts every\ntime you kill someone", 1, 0),
	COLLOSAL_STRENGTH(4, Items.GOLDEN_SWORD, "Will massively Critical Hit\ndamage but all non Critical Hits will be weak", 2, 0),
	CRACKSHOT(2, Items.GOLDEN_PICKAXE, "Your projectiles will break blocks!", 3, 0),
	CRYSTAL_HIT(2, Items.AMETHYST_SHARD, "Deal extra damage on people with\nfull health", 4, 0),
	FADING_CRYSTAL(1, Items.POPPED_CHORUS_FRUIT, "Allows the bearers flag to be\nremoved more swiftly", 6, 0),
	FIGHTERS_EYE(2, Items.SPYGLASS, "Allows the bearer to see how much\nmuch health something has", 7, 0),
	FROST_CANNON(3, Items.IRON_HORSE_ARMOR, "Rightclick with your axe to\nshoot!", 8, 0),
	
	HEART_CLOVER(3, Items.RED_DYE, "Heal to your max health upon\nkilling someone", 0, 1),
	ICE_SMASH(1, Items.ICE, "Freeze whatever you're attacking when you crit with an Axe", 2, 1),
	JUGGLER(3, Items.COCOA_BEANS, "Heal every time you switch targets", 3, 1),
	LUCKY_START(1, Items.PEONY, "Gain a random positive status\neffect when you respawn", 4, 1),
	MUTED_BRACELET(2, Items.IRON_NUGGET, "When the bearer breaks a block\nonly they will hear the sound", 5, 1),
	POISON_DAGGER(1, Items.LIME_DYE, "Makes critical hit from your\nsword inflict poison", 6, 1),
	REWIND_WATCH(3, Items.CLOCK, "When you fall in the void with\nfull health, you get teleported\nback", 7, 1),
	SLOW_GO(2, Items.SNOWBALL, "Gives you regeneration when you\naren't moving quickly", 8, 1),
	
	SUGAR_CUBES(2, Items.WHITE_DYE, "Increases the users speed when\nthey have full health", 0, 2),
	SWIFT_REFILL(2, Items.HONEY_BOTTLE, "Decreases the time it takes for Flasks to refill", 1, 2),
	WINDBOUND_COMPASS(2, Items.COMPASS, "Follow the wind to find a nearby\nFlag", 2, 2);
	
	public final Item item;
	private final String description;
	private final int x, y;
	private final int notches;
	
	private Charm(int notches, Item item, String description, int x, int y) {
		this.item = item;
		this.description = description;
		this.x = x;
		this.y = y;
		this.notches = notches;
	}
	
	public List<Text> getDescription() {
		ArrayList<Text> list = new ArrayList<>();
		if (!description.isBlank()) {
			for (String line : description.split("\n")) {
				list.add(Kuphack.color(
					Text.literal(" "+line),
					Color.decode("#5c5a96")
				));
			}
		}
		list.add(Text.literal(" "));
		list.add(Kuphack.color(
			Text.literal(" Notches: "+notches),
			Color.decode("#5c5a96")
		));
		return list;//this.description;
	}
	
	public String toString() {
		return WordUtils.capitalize(name().replaceAll("_", " ").toLowerCase());
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public ItemStack getIcon() {
		ItemStack stack = new ItemStack(item);
		stack.setCustomName(Text.literal("§f"+toString()));
		return stack;
	}
	
}
