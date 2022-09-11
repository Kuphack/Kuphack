package com.github.Vaapukkax.kuphack.flagclash.widgets;

import java.math.BigInteger;
import java.util.List;

import org.apache.commons.lang3.text.WordUtils;

import com.github.Vaapukkax.kuphack.flagclash.FlagClash;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemStack.TooltipSection;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

@Deprecated
public enum Weapon implements Widget {

	OLD_STICK(Items.STICK, 5, ItemModifier.damage(3), ItemModifier.infiniteRate()),
	
	// [ROOT]
	WOODEN_SWORD(-10, Items.WOODEN_SWORD, 200, ItemModifier.damage(3.5), ItemModifier.infiniteRate()),
	WOODEN_SPEAR(0, Items.WOODEN_SHOVEL, 320, ItemModifier.damage(3), ItemModifier.infiniteRate(), ItemModifier.dash(0.9), ItemModifier.cooldown(8)),
	COBBLE_CRUSHER(6, Items.STONE_AXE, 50_000, ItemModifier.damage(7), ItemModifier.rate(1.5), ItemModifier.swiftness(-1)),
	SLINGSHOT(8, Items.LEAD, 250_000),
	
	// > Wooden Sword
	COBBLE_DAGGER(-2, Items.STONE_SWORD, 25_000, ItemModifier.damage(4.5), ItemModifier.infiniteRate(), ItemModifier.custom("Reduced range"), ItemModifier.custom("Inflicts bleeding")),
	WORMWOOD_HOE(0, Items.WOODEN_HOE, new BigInteger("100_000_000_000".replaceAll("_", "")), ItemModifier.damage(4.5), ItemModifier.infiniteRate()),
	ROCKY_SWORD(2, Items.STONE_SWORD, 8_000, ItemModifier.damage(4), ItemModifier.infiniteRate()),
	
	// > Wooden Sword > Cobble Dagger
	KITCHEN_KNIFE(-1, Items.IRON_SWORD, 400_000, ItemModifier.damage(5.5), ItemModifier.infiniteRate(), ItemModifier.custom("Reduced range"), ItemModifier.custom("Inflicts bleeding"), ItemModifier.custom("Doesn't do knockback")),
	SHARPEND_DAGGER(Items.IRON_SWORD, 225_000, ItemModifier.damage(5.75), ItemModifier.infiniteRate(), ItemModifier.custom("Reduced range"), ItemModifier.custom("Inflicts bleeding")),
	
	// > Wooden Sword > Cobble Dagger > Kitchen Knife
	SUPER_SHINY_KNIFE(Items.DIAMOND_SWORD, 8_000_000, ItemModifier.damage(6.5), ItemModifier.infiniteRate(), ItemModifier.custom("Reduced range"), ItemModifier.custom("Inflicts bleeding"), ItemModifier.custom("Doesn't do knockback")),
	BLOODSTAINED_KNIFE(Items.DIAMOND_SWORD, 4_000_000, ItemModifier.damage(6), ItemModifier.infiniteRate(), ItemModifier.custom("Reduced range"), ItemModifier.custom("Inflicts bleeding"), ItemModifier.custom("Doesn't do knockback"), ItemModifier.custom("Bleed for longer!")),
	CARROT_BLADE(Items.CARROT, 80_000_000, ItemModifier.damage(4.5), ItemModifier.infiniteRate()),

	// > Wood Sword > Cobble Dagger > Kitchen Knife > Carrot Blade
	HOLY_CARROT_BLADE(Items.GOLDEN_CARROT, new BigInteger("80_000_000_000".replaceAll("_", "")), ItemModifier.damage(6), ItemModifier.infiniteRate()),
	
	// > Wood Sword > Wormwood Scythe
	THE_SOUL_RIPPER(0, Items.NETHERITE_HOE, new BigInteger("100_000_000_000_000_000".replaceAll("_", "")), ItemModifier.damage(7), ItemModifier.infiniteRate()),
	PURE_SCYTHE(0, Items.GOLDEN_HOE, new BigInteger("400_000_000_000_000".replaceAll("_", "")), ItemModifier.damage(6), ItemModifier.infiniteRate()),
	SHADOW_SCYTHE(0, Items.STONE_HOE, FlagClash.toRealValue("280.00q"), ItemModifier.damage(5), ItemModifier.infiniteRate()),
	
	// > Wooden Sword > Rocky Sword
	CHILLED_SWORD(Items.IRON_SWORD, 600_000, ItemModifier.damage(4), ItemModifier.infiniteRate(), ItemModifier.custom("Inflicts Slowness I")),
	IRON_NAIL(Items.IRON_SWORD, 700_000, ItemModifier.damage(7), ItemModifier.rate(1.4), ItemModifier.custom("Has recoil")),
	
	// > Wooden Sword > Rocky Sword > Chilled Sword
	FROSTBITE_SWORD(Items.DIAMOND_SWORD, 12_000_000, ItemModifier.damage(5.5), ItemModifier.infiniteRate(), ItemModifier.custom("Inflicts Slowness II")),
	
	// > Wooden Sword > Rocky Sword > Iron Nail
	DREAM_NAIL(Items.DIAMOND_SWORD, 75_000_000, ItemModifier.damage(8), ItemModifier.rate(1.4), ItemModifier.custom("Has recoil")),
	LIFEBLOOD_NAIL(Items.DIAMOND_SWORD, 10_000_000, ItemModifier.health(4), ItemModifier.damage(8), ItemModifier.rate(1.4), ItemModifier.custom("Has recoil")),
	
	// > Wooden Sword > Rocky Sword > Iron Nail > Dream Nail
	AWOKEN_NAIL(Items.GOLDEN_SWORD, 342_000_000, ItemModifier.damage(9), ItemModifier.rate(1.4), ItemModifier.custom("Has recoil")),
	NIGHT_NAIL(Items.GOLDEN_SWORD, 145_000_000, ItemModifier.damage(8), ItemModifier.rate(1.4), ItemModifier.custom("Has recoil")),
	
	// > Slingshot
	SIMPLE_BOW(Items.BOW, 1_000_000, ItemModifier.range(-1), ItemModifier.power(4), ItemModifier.accuracy(1), ItemModifier.reload(2)),
	MOLTEN_SLINGSHOT(1, Items.LEAD, 25_000_000),
	
	// > Slingshot > Simple Bow
	SNAPSTICK_BOW(Items.BOW, new BigInteger("8_000_000_000".replaceAll("_", "")), ItemModifier.range(0), ItemModifier.power(5), ItemModifier.accuracy(3), ItemModifier.reload(3)),
	HARDWOORD_BOW(Items.BOW, 120_000_000, ItemModifier.range(1), ItemModifier.power(7), ItemModifier.accuracy(4), ItemModifier.reload(7)),
	
	// > Slingshot > Simple Bow > Snapstick Bow
	QUICKSHOT_BOW(Items.BOW, new BigInteger("800_000_000_000".replaceAll("_", "")), ItemModifier.range(1), ItemModifier.power(5), ItemModifier.accuracy(1), ItemModifier.reload(0.1)),
	
	// > Slingshot > Simple Bow > Hardwood Bow
	CURSED_BOW(Items.BOW, new BigInteger("480_000_000_000".replaceAll("_", "")), ItemModifier.range(1), ItemModifier.power(9), ItemModifier.accuracy(-1.5), ItemModifier.reload(1)),
	SILVERSTRIKE_BOW(3, Items.BOW, new BigInteger("25_000_000_000".replaceAll("_", "")), ItemModifier.range(1), ItemModifier.power(7), ItemModifier.accuracy(2), ItemModifier.reload(5), ItemModifier.multishot(2)),

	// > Slingshot > Simple Bow > Hardwood Bow > Cursed Bow
	BLIGHTED_BOW(Items.BOW, new BigInteger("28_000_000_000_000".replaceAll("_", "")), ItemModifier.range(1), ItemModifier.power(7), ItemModifier.accuracy(-1.5), ItemModifier.reload(8), ItemModifier.multishot(10)),
	MATTERBENDER_BOW(Items.BOW, new BigInteger("19_000_000_000_000".replaceAll("_", "")), ItemModifier.range(1), ItemModifier.power(4), ItemModifier.accuracy(1), ItemModifier.reload(3), ItemModifier.multishot(1)),

	// > Slingshot > Simple Bow > Hardwood Bow > Silverstrike Bow
	ANGLE_BOW(Items.BOW, new BigInteger("28_000_000_000_000".replaceAll("_", "")), ItemModifier.range(0), ItemModifier.power(7), ItemModifier.accuracy(3), ItemModifier.reload(6)),
	UNBREAKABLE_BOW(Items.BOW, new BigInteger("200_000_000_000".replaceAll("_", "")), ItemModifier.range(1), ItemModifier.power(4), ItemModifier.accuracy(2), ItemModifier.reload(5), ItemModifier.custom("Unbreakable")),
	TELEBOW(Items.BOW, new BigInteger("1_000_000_000_000".replaceAll("_", "")), ItemModifier.range(-1), ItemModifier.power(7.5), ItemModifier.accuracy(4), ItemModifier.reload(7)),
	
	// > Slingshot > Simple Bow > Hardwood Bow > Silverstrike Bow > Unbreakable Bow
	UNBREAKABLE_CODEX(Items.BOOK, new BigInteger("1_000_000_000_000".replaceAll("_", "")), ItemModifier.custom("II is written on the cover, seems useless")),

	// > Slingshot > Simple Bow > Hardwood Bow > Silverstrike Bow > Unbreakable Bow > Unbreakable Codex
	FORGOTTEN_LEXICON(Items.BOOK, 0),
	
	// > Slingshot > Simple Bow > Hardwood Bow > Silverstrike Bow > Unbreakable Bow > Unbreakable Codex > Forgotten Lexicon
	TOME_OF_FLAME(Items.BOOK, FlagClash.toRealValue("200.00t"), ItemModifier.custom("Filled with very hot imagery!\n\n(About fire)")),
	SPARK_TOME(Items.ENCHANTED_BOOK, FlagClash.toRealValue("150.00t")),
	
	// > Slingshot > Simple Bow > Hardwood Bow > Silverstrike Bow > Unbreakable Bow > Unbreakable Codex > Forgotten Lexicon > Blast Fire
	BLASTFIRE_TOME(Items.BOOK, FlagClash.toRealValue("600.00q"), ItemModifier.custom("Magical Kaboom")),

	// > Slingshot > Simple Bow > Hardwood Bow > Silverstrike Bow > Unbreakable Bow > Unbreakable Codex > Forgotten Lexicon > Spark Tome
	POISONWAVE_TOME(Items.ENCHANTED_BOOK, FlagClash.toRealValue("550.00q")),
	
	// > Slingshot > Molten Slingshot
	RAPPID_SLINGSHOT(Items.LEAD, 2_000_000_000),
	EXPLOSIVE_SLINGSHOT(Items.LEAD, new BigInteger("3_000_000_000".replaceAll("_", ""))),
	
	// > Wooden Spear
	STONEPOKE_SPEAR(-2, Items.STONE_SHOVEL, 50_000, ItemModifier.damage(4), ItemModifier.infiniteRate(), ItemModifier.dash(0.8), ItemModifier.cooldown(8)),
	CALCITE_SPEAR(1, Items.STONE_SHOVEL, 70_000, ItemModifier.damage(3.5), ItemModifier.infiniteRate(), ItemModifier.dash(1), ItemModifier.cooldown(4)),

	// > Wooden Spear > Stonepoke Spear
	SPIKED_SPEAR(-1, Items.IRON_SHOVEL, 1_000_000, ItemModifier.damage(4.5), ItemModifier.infiniteRate(), ItemModifier.dash(0.8), ItemModifier.cooldown(8)),
	PITCHFORK(Items.TRIDENT, FlagClash.toRealValue("50.00e"), ItemModifier.damage(6), ItemModifier.infiniteRate(), ItemModifier.dash(0.8), ItemModifier.cooldown(4)),

	// > Wooden Spear > Stonepoke Spear > Spiked Spear
	QUICK_SPEAR(Items.DIAMOND_SHOVEL, new BigInteger("5_000_000_000".replace("_", "")), ItemModifier.damage(4), ItemModifier.infiniteRate(), ItemModifier.dash(0.8), ItemModifier.cooldown(8)),
	WITHERTIPPED_SPEAR(Items.DIAMOND_SHOVEL, new BigInteger("6_000_000_000".replace("_", "")), ItemModifier.damage(5), ItemModifier.infiniteRate(), ItemModifier.dash(0.8), ItemModifier.cooldown(8)),

	// > Wooden Spear > Stonepoke Spear > Pitchfork
	TRIFORK(Items.TRIDENT, FlagClash.toRealValue("2.00o"), ItemModifier.damage(9), ItemModifier.cooldown(4), ItemModifier.rate(999)),
	THE_TRIDENT(Items.TRIDENT, FlagClash.toRealValue("100.00e"), ItemModifier.damage(7), ItemModifier.rate(999), ItemModifier.cooldown(8)),
	
	// > Wooden Spear > Calcite Spear
	PYRITE_SPEAR(Items.IRON_SHOVEL, 990_000, ItemModifier.damage(4), ItemModifier.infiniteRate(), ItemModifier.dash(1.2), ItemModifier.cooldown(4)),
	SILVER_SPEAR(3, Items.IRON_SHOVEL, 12_000_000, ItemModifier.damage(4), ItemModifier.infiniteRate(), ItemModifier.swiftness(1), ItemModifier.dash(1), ItemModifier.cooldown(4)),

	// > Wooden Spear > Calcite Spear > Pyrite Spear
	AMETHYST_SPEAR(Items.DIAMOND_SHOVEL, 600_000_000, ItemModifier.damage(4.5), ItemModifier.infiniteRate(), ItemModifier.dash(1.3), ItemModifier.cooldown(3), ItemModifier.custom("Regenerate in the light")),
	OFFCURSE_SPEAR(Items.GOLDEN_SHOVEL, 1_000_000_000,ItemModifier.damage(5.5), ItemModifier.infiniteRate(), ItemModifier.dash(1.2), ItemModifier.cooldown(4)),

	// > Wooden Spear > Calcite Spear > Pyrite Spear > Amethyst Spear
	LIGHTBOUND_SPEAR(Items.DIAMOND_SHOVEL, new BigInteger("60_000_000_000".replaceAll("_", "")),ItemModifier.damage(5), ItemModifier.infiniteRate(), ItemModifier.dash(1.3), ItemModifier.cooldown(3), ItemModifier.custom("Regenerate in the light")),
	CELESTRIAL_SPEAR(Items.GOLDEN_SHOVEL, new BigInteger("80_000_000_000".replaceAll("_", "")), ItemModifier.damage(5), ItemModifier.infiniteRate(), ItemModifier.dash(1.5), ItemModifier.cooldown(1.5)),
	
	// > Wooden Spear > Calcite Spear > Pyrite Spear > Amethyst Spear > Celestrial Spear
	SPEAR_OF_THE_MOUNTAIN(Items.NETHERITE_SHOVEL, new BigInteger("150_000_000_000_000".replaceAll("_", "")), ItemModifier.damage(5), ItemModifier.infiniteRate(), ItemModifier.dash(1.5), ItemModifier.cooldown(1.4)),
	
	// > Wooden Spear > Calcite Spear > Silver Spear
	SOUNDSPEED_SPEAR(Items.DIAMOND_SHOVEL, new BigInteger("30_000_000_000".replaceAll("_", "")),ItemModifier.damage(4.5), ItemModifier.infiniteRate(), ItemModifier.swiftness(2),ItemModifier.dash(1), ItemModifier.cooldown(4)),
	SOULHEART_SPEAR(Items.DIAMOND_SHOVEL, new BigInteger("30_000_000_000".replaceAll("_", "")), ItemModifier.damage(4.5), ItemModifier.infiniteRate(), ItemModifier.health(4),   ItemModifier.swiftness(1), ItemModifier.dash(1), ItemModifier.cooldown(4)),
	ROSEGOLD_SPEAR(Items.GOLDEN_SHOVEL, new BigInteger("120_000_000".replaceAll("_", "")),      ItemModifier.damage(5.5), ItemModifier.infiniteRate(), ItemModifier.health(-4),  ItemModifier.swiftness(1), ItemModifier.dash(1), ItemModifier.cooldown(4)),
	
	// > Wooden Spear > Calcite Spear > Silver Spear > Soundspeed Spear
	LIGHTSPEED_SPEAR(Items.GOLDEN_SHOVEL, new BigInteger("15_000_000_000_000".replaceAll("_", "")), ItemModifier.damage(5), ItemModifier.infiniteRate(), ItemModifier.swiftness(3), ItemModifier.dash(1), ItemModifier.cooldown(4)),

	// > Wooden Spear > Calcite Spear > Silver Spear > Soundspeed Spear > Lightspeed Spear
	TRUE_LIGHTSPEED_SPEAR(Items.GOLDEN_SHOVEL, FlagClash.toRealValue("150p"), ItemModifier.damage(5), ItemModifier.infiniteRate(), ItemModifier.swiftness(4), ItemModifier.dash(-1), ItemModifier.cooldown(4)),
	
	// > Wooden Spear > Calcite Spear > Silver Spear > Soulheart Spear
	PURE_SPEAR(Items.GOLDEN_SHOVEL, new BigInteger("30_000_000_000_000".replaceAll("_", "")), ItemModifier.damage(5), ItemModifier.infiniteRate(), ItemModifier.health(5), ItemModifier.swiftness(1), ItemModifier.dash(1), ItemModifier.cooldown(4)),
	SWORD_OF_LOVE(Items.NETHERITE_SWORD, new BigInteger("700_000_000_000_000".replaceAll("_", "")), ItemModifier.damage(1), ItemModifier.infiniteRate(), ItemModifier.health(-4), ItemModifier.swiftness(-1)),
	
	// > Cobble Crusher
	GIANTS_AXE(Items.IRON_AXE, 1_000_000, ItemModifier.damage(10), ItemModifier.rate(0.5), ItemModifier.knockback(1), ItemModifier.swiftness(-2)),
	SWIFTORE_AXE(1, Items.IRON_AXE, 800_000, ItemModifier.damage(8), ItemModifier.rate(1.8)),
	
	// > Cobble Crusher > Giant's Axe
	THE_OBLITERATOR(Items.DIAMOND_AXE, new BigInteger("270_000_000_000".replaceAll("_", "")), ItemModifier.damage(15), ItemModifier.rate(0.2), ItemModifier.knockback(1), ItemModifier.swiftness(-3.5)),
	GREAT_BATTLEAXE(Items.IRON_AXE, new BigInteger("80_000_000_000".replaceAll("_", "")), ItemModifier.damage(11), ItemModifier.rate(0.5), ItemModifier.knockback(1), ItemModifier.swiftness(-2.5)),

	// > Cobble Crusher > Swiftore Axe
	QUICKAXE(Items.DIAMOND_AXE, new BigInteger("50_000_000_000".replaceAll("_", "")), ItemModifier.damage(8.5), ItemModifier.rate(1.8), ItemModifier.efficiency(1)),
	ENDER_AXE(Items.DIAMOND_AXE, new BigInteger("160_000_000".replaceAll("_", "")), ItemModifier.damage(8.5), ItemModifier.rate(0.5)),
	
	// > Cobble Crusher > Swiftore Axe > Quickaxe
	LUCY_REPLICA(Items.GOLDEN_AXE, new BigInteger("360_000_000_000".replaceAll("_", "")), ItemModifier.damage(8.5), ItemModifier.rate(1.8), ItemModifier.efficiency(2)),
	GODLY_AXE(Items.GOLDEN_AXE, new BigInteger("360_000_000_000".replaceAll("_", "")), ItemModifier.damage(9), ItemModifier.knockback(1), ItemModifier.fireAspect(1));
	
	public final Item item;
	private final BigInteger cost;
	
	private final ItemModifier[] modifiers;
	
	private int x = -1, y = -666;
	private final int yOffset;
	
	private Weapon(Item item, long cost, ItemModifier... modifiers) {
		this(0, item, BigInteger.valueOf(cost), modifiers);
	}
	
	private Weapon(Item item, BigInteger cost, ItemModifier... modifiers) {
		this(0, item, cost, modifiers);
	}
	
	private Weapon(int yOffset, Item item, long cost, ItemModifier... modifiers) {
		this(yOffset, item, BigInteger.valueOf(cost), modifiers);
	}
	private Weapon(int yOffset, Item item, BigInteger cost, ItemModifier... modifiers) {
		this.item = item;
		this.cost = cost;
		this.modifiers = modifiers;
		this.yOffset = yOffset;
	}
	
	public BigInteger getCost() {
		return cost;
	}
	
	public String toString() {
		return WordUtils.capitalize(name().replaceAll("_", " ").toLowerCase());
	}

	@Override
	public int getX() {
		if (x == -1) {
			x = WeaponTree.find(this).getPathsGoneThrough()*2;
		}
		return x;
	}

	@Override
	public int getY() {
//		y = -666;
		if (y == -666) {
			WeaponTree parent = WeaponTree.find(this).getParent();
			if (parent != null) {
				int length = parent.getPaths().length;
				
				int offset = parent.indexOf(this)*length-((length*Math.max(1, length-1)/2));
				
				y = parent.getItem().getY()+offset;
			} else y = 0;
		}
		return y+yOffset;
	}

	public Object getModifier(String key) {
		for (ItemModifier modifier : modifiers) {
			if (modifier.getKey().equals(key)) {
				return modifier.getValue();
			}
		}
		return null;
	}
	
	@Override
	public ItemStack getIcon() {
		ItemStack stack = new ItemStack(item);
		stack.setCustomName(Text.literal(toString()+" "+FlagClash.toVisualValue(cost)));
		
		NbtList loreTag = new NbtList();
		if (getModifier("") != null) {		
			MutableText text = Text.of(" ").copy().append(getModifier("").toString());
			text.setStyle(text.getStyle().withColor(ItemModifier.LORE_COLOR).withItalic(false));
			loreTag.add(NbtString.of(Text.Serializer.toJson(text)));
			
			loreTag.add(NbtString.of(Text.Serializer.toJson(Text.of(" "))));
		}
		
		loreTag.add(NbtString.of(Text.Serializer.toJson(Text.of(" \u00a77Properties:"))));
		for (ItemModifier modifier : modifiers) {
			if (!modifier.isCustom()) {
				MutableText text = Text.of("  ").copy().append(modifier.toText());
				text.setStyle(text.getStyle().withItalic(false));
				loreTag.add(NbtString.of(Text.Serializer.toJson(text)));
			}
		}
//		loreTag.add(NbtString.of(Text.Serializer.toJson(Text.literal("\u00a75 Price: "+FlagClash.toVisualValue(cost)))));
		stack.getOrCreateSubNbt(ItemStack.DISPLAY_KEY).put(ItemStack.LORE_KEY, loreTag);
		
		Object efficiency = getModifier("Efficiency");
		if (efficiency != null) stack.addEnchantment(Enchantments.EFFICIENCY, ((Number)efficiency).intValue());
		
		for (TooltipSection section : TooltipSection.values())
			stack.addHideFlag(section);
		
		return stack;
	}

	@Override
	public List<Text> getDescription() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
